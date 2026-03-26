package org.landm.service.impl;

import org.landm.dto.promotion.*;
import org.landm.entity.*;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.PromotionType;
import org.landm.entity.Enums.TransactionType;
import org.landm.repository.*;
import org.landm.service.PromotionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final AdPromotionRepository adPromotionRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    public PromotionServiceImpl(AdRepository adRepository,
                                UserRepository userRepository,
                                AdPromotionRepository adPromotionRepository,
                                CreditTransactionRepository creditTransactionRepository) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.adPromotionRepository = adPromotionRepository;
        this.creditTransactionRepository = creditTransactionRepository;
    }

    @Override
    public List<PromotionPackageDto> getPackages() {
        return List.of(
            new PromotionPackageDto(PromotionType.FEATURED,
                "Uvek na 1. mestu u rezultatima pretrage i kategoriji. Do 10x više pregleda."),
            new PromotionPackageDto(PromotionType.PRIORITY,
                "Ispred standardnih oglasa u pretrazi i kategoriji. Do 5x više pregleda."),
            new PromotionPackageDto(PromotionType.HIGHLIGHTED,
                "Oglas se vizuelno ističe bojom kartice. Obnova oglasa po isteku promocije.")
        );
    }

    @Override
    @Transactional
    public ActivePromotionDto activate(Long adId, PromotionType type, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Oglas nije pronađen."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Korisnik nije pronađen."));

        if (!ad.getOwner().getId().equals(userId)) {
            throw new SecurityException("Možete promovišati samo sopstvene oglase.");
        }
        if (ad.getAdStatus() == AdStatus.DELETED || ad.getAdStatus() == AdStatus.SUSPENDED_BY_ADMIN) {
            throw new IllegalStateException("Oglas nije dostupan za promociju.");
        }

        BigDecimal price = BigDecimal.valueOf(type.getPriceRsd());
        if (user.getCredit().compareTo(price) < 0) {
            throw new IllegalStateException("Nedovoljno kredita. Potrebno: " + price + " RSD.");
        }

        // Skini kredit
        user.setCredit(user.getCredit().subtract(price));
        userRepository.save(user);

        // Zabeleži transakciju
        CreditTransaction tx = new CreditTransaction(
                user, price.negate(), TransactionType.PROMOTION_PURCHASE,
                type.getDisplayName() + " — oglas #" + adId, adId);
        creditTransactionRepository.save(tx);

        // Kreiraj zapis u istoriji promocija
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(type.getDurationDays());
        AdPromotion promotion = new AdPromotion(ad, user, type, now, expiresAt, price);
        adPromotionRepository.save(promotion);

        // Ažuriraj denormalizovana polja na oglasu (uzima viši rank ako već ima promociju)
        if (type.getRank() > ad.getPromotionRank()) {
            ad.setPromotionType(type);
            ad.setPromotionRank(type.getRank());
            ad.setPromotionExpiresAt(expiresAt);
        }
        // HIGHLIGHTED ne utiče na rank (rank=0), ali se čuva za vizuelni prikaz
        if (type == PromotionType.HIGHLIGHTED && ad.getPromotionType() == null) {
            ad.setPromotionType(PromotionType.HIGHLIGHTED);
            ad.setPromotionExpiresAt(expiresAt);
        }
        // Aktiviraj oglas ako je bio istekao
        if (ad.getAdStatus() == AdStatus.ARCHIVED) {
            ad.setAdStatus(AdStatus.ACTIVE);
            ad.setExpiresAt(LocalDateTime.now().plusDays(30));
        }
        adRepository.save(ad);

        return new ActivePromotionDto(promotion.getId(), type, now, expiresAt, price);
    }

    @Override
    public List<ActivePromotionDto> getActivePromotions(Long adId) {
        return adPromotionRepository.findActiveByAdId(adId, LocalDateTime.now())
                .stream()
                .map(p -> new ActivePromotionDto(p.getId(), p.getPromotionType(),
                        p.getStartsAt(), p.getExpiresAt(), p.getPricePaid()))
                .toList();
    }

    @Override
    @Transactional
    public void renewAd(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Oglas nije pronađen."));
        if (!ad.getOwner().getId().equals(userId)) {
            throw new SecurityException("Možete obnoviti samo sopstvene oglase.");
        }
        if (ad.getAdStatus() == AdStatus.DELETED || ad.getAdStatus() == AdStatus.SUSPENDED_BY_ADMIN) {
            throw new IllegalStateException("Oglas ne može biti obnovljen.");
        }
        ad.setExpiresAt(LocalDateTime.now().plusDays(30));
        if (ad.getAdStatus() == AdStatus.ARCHIVED) {
            ad.setAdStatus(AdStatus.ACTIVE);
        }
        adRepository.save(ad);
    }

    @Override
    @Transactional
    public void addCredit(Long userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Iznos mora biti pozitivan.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Korisnik nije pronađen."));
        user.setCredit(user.getCredit().add(amount));
        userRepository.save(user);

        CreditTransaction tx = new CreditTransaction(
                user, amount, TransactionType.TOPUP_ADMIN, description, null);
        creditTransactionRepository.save(tx);
    }

    @Override
    public BigDecimal getCreditBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Korisnik nije pronađen."));
        return user.getCredit();
    }

    @Override
    public Page<CreditTransactionDto> getCreditHistory(Long userId, Pageable pageable) {
        return creditTransactionRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(CreditTransactionDto::from);
    }

    /**
     * Svakih sat vremena proveri promocije kojima je isteklo trajanje.
     * Resetuje promotionType/promotionRank na oglasu ako nema druge aktivne promocije.
     */
    @Override
    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void expirePromotions() {
        LocalDateTime now = LocalDateTime.now();
        List<Ad> adsWithExpiredPromo = adRepository.findAll().stream()
                .filter(a -> a.getPromotionExpiresAt() != null && a.getPromotionExpiresAt().isBefore(now))
                .toList();

        for (Ad ad : adsWithExpiredPromo) {
            // Proveri da li ima neka druga aktivna promocija višeg ranka
            List<AdPromotion> active = adPromotionRepository.findActiveByAdId(ad.getId(), now);
            if (active.isEmpty()) {
                ad.setPromotionType(null);
                ad.setPromotionRank(0);
                ad.setPromotionExpiresAt(null);
            } else {
                // Uzmi aktivnu sa najvišim rankom
                AdPromotion best = active.stream()
                        .max((a1, a2) -> Integer.compare(a1.getPromotionType().getRank(), a2.getPromotionType().getRank()))
                        .get();
                ad.setPromotionType(best.getPromotionType());
                ad.setPromotionRank(best.getPromotionType().getRank());
                ad.setPromotionExpiresAt(best.getExpiresAt());
            }
            adRepository.save(ad);
        }
    }

    /**
     * Svako jutro u 03:00 deaktivira oglase kojima je isteklo 30-dnevno trajanje.
     * Korisnik može besplatno da obnovi.
     */
    @Override
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void expireAds() {
        LocalDateTime now = LocalDateTime.now();
        List<Ad> expiredAds = adRepository.findAll().stream()
                .filter(a -> a.getExpiresAt() != null
                        && a.getExpiresAt().isBefore(now)
                        && a.getAdStatus() == AdStatus.ACTIVE)
                .toList();

        for (Ad ad : expiredAds) {
            ad.setAdStatus(AdStatus.ARCHIVED);
            adRepository.save(ad);
        }
    }
}
