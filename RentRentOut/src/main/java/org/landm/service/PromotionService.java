package org.landm.service;

import org.landm.dto.promotion.*;
import org.landm.entity.Enums.PromotionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PromotionService {

    /** Vraća sve dostupne pakete promocija */
    List<PromotionPackageDto> getPackages();

    /** Aktivira promociju na oglasu (skida kredit od korisnika) */
    ActivePromotionDto activate(Long adId, PromotionType type, Long userId);

    /** Vraća aktivne promocije za oglas */
    List<ActivePromotionDto> getActivePromotions(Long adId);

    /** Obnavlja oglas (besplatno — produžava expiresAt za 30 dana i reaktivira ako je bio EXPIRED) */
    void renewAd(Long adId, Long userId);

    /** Dodaje kredit korisniku (poziva admin) */
    void addCredit(Long userId, BigDecimal amount, String description);

    /** Vraća stanje kredita korisnika */
    BigDecimal getCreditBalance(Long userId);

    /** Istorija transakcija kredita */
    Page<CreditTransactionDto> getCreditHistory(Long userId, Pageable pageable);

    /** Sve transakcije — za admin panel */
    Page<AdminCreditTransactionDto> getAllTransactions(Pageable pageable);

    /** Scheduled: gasi istekle promocije (poziva se automatski svakih sat) */
    void expirePromotions();

    /** Scheduled: deaktivira oglase kojima je istekao rok (poziva se svako jutro) */
    void expireAds();

    /** Scheduled: šalje email podsetnik vlasnicima čiji oglasi ističu za 3 dana */
    void sendExpiryReminders();
}
