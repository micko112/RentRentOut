package org.landm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.landm.dto.promotion.ActivePromotionDto;
import org.landm.entity.Ad;
import org.landm.entity.AdPromotion;
import org.landm.entity.CreditTransaction;
import org.landm.entity.Role;
import org.landm.entity.User;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.PromotionType;
import org.landm.entity.Enums.TransactionType;
import org.landm.repository.AdPromotionRepository;
import org.landm.repository.AdRepository;
import org.landm.repository.CreditTransactionRepository;
import org.landm.repository.UserRepository;
import org.landm.service.impl.PromotionServiceImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.landm.service.HtmlEmailService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {

    @Mock
    private AdRepository adRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdPromotionRepository adPromotionRepository;

    @Mock
    private CreditTransactionRepository creditTransactionRepository;

    @Mock
    private HtmlEmailService htmlEmailService;

    @InjectMocks
    private PromotionServiceImpl promotionService;

    private User owner;
    private Ad ad;

    @BeforeEach
    void setUp() {
        Role role = new Role("ROLE_USER");

        owner = new User();
        owner.setId(1L);
        owner.setEmail("korisnik@test.com");
        owner.setFirstname("Marko");
        owner.setLastname("Markovic");
        owner.setCredit(new BigDecimal("1000.00"));

        ad = new Ad();
        ad.setId(10L);
        ad.setTitle("Test oglas");
        ad.setOwner(owner);
        ad.setAdStatus(AdStatus.ACTIVE);
        ad.setPromotionRank(0);
    }

    // ─── activate() ────────────────────────────────────────────────────────────

    @Test
    void activate_happyPath_skidaKreditIKreiraPromociju() {
        // FEATURED kosta 500 RSD; korisnik ima 1000 RSD
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(adPromotionRepository.save(any(AdPromotion.class))).thenAnswer(inv -> {
            AdPromotion p = inv.getArgument(0);
            // Simuliraj ID koji baza dodela
            return p;
        });
        when(adRepository.save(any(Ad.class))).thenReturn(ad);
        when(userRepository.save(any(User.class))).thenReturn(owner);
        when(creditTransactionRepository.save(any(CreditTransaction.class))).thenAnswer(inv -> inv.getArgument(0));

        ActivePromotionDto result = promotionService.activate(10L, PromotionType.FEATURED, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getPromotionType()).isEqualTo(PromotionType.FEATURED);
        assertThat(result.getPricePaid()).isEqualByComparingTo(new BigDecimal("500"));

        // Kredit mora biti skinut
        assertThat(owner.getCredit()).isEqualByComparingTo(new BigDecimal("500.00"));

        // Oglas mora imati novi rank
        assertThat(ad.getPromotionRank()).isEqualTo(PromotionType.FEATURED.getRank());
        assertThat(ad.getPromotionType()).isEqualTo(PromotionType.FEATURED);

        // Moraju biti pozvani save-ovi
        verify(userRepository).save(owner);
        verify(creditTransactionRepository).save(any(CreditTransaction.class));
        verify(adPromotionRepository).save(any(AdPromotion.class));
        verify(adRepository).save(ad);
    }

    @Test
    void activate_nedovoljnoKredita_bacaIllegalStateException() {
        // Korisnik ima 200 RSD, FEATURED kosta 500
        owner.setCredit(new BigDecimal("200.00"));
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> promotionService.activate(10L, PromotionType.FEATURED, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nedovoljno kredita");

        verify(userRepository, never()).save(any());
        verify(creditTransactionRepository, never()).save(any());
        verify(adPromotionRepository, never()).save(any());
    }

    @Test
    void activate_oglasSNedovoljnimKreditomZaPRIORITY_bacaIllegalStateException() {
        // Korisnik ima 100 RSD, PRIORITY kosta 250
        owner.setCredit(new BigDecimal("100.00"));
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> promotionService.activate(10L, PromotionType.PRIORITY, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nedovoljno kredita");
    }

    @Test
    void activate_oglasTacanKreditZaHIGHLIGHTED_uspesnoAktivira() {
        // Korisnik ima tačno 100 RSD = cena HIGHLIGHTED
        owner.setCredit(new BigDecimal("100.00"));
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(adPromotionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(adRepository.save(any())).thenReturn(ad);
        when(userRepository.save(any())).thenReturn(owner);
        when(creditTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ActivePromotionDto result = promotionService.activate(10L, PromotionType.HIGHLIGHTED, 1L);

        assertThat(result.getPromotionType()).isEqualTo(PromotionType.HIGHLIGHTED);
        assertThat(owner.getCredit()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void activate_oglasNePripadaKorisniku_bacaSecurityException() {
        User drugCorisnik = new User();
        drugCorisnik.setId(99L);
        ad.setOwner(drugCorisnik);

        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> promotionService.activate(10L, PromotionType.FEATURED, 1L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("sopstvene oglase");

        verify(creditTransactionRepository, never()).save(any());
    }

    @Test
    void activate_oglasJeObrisan_bacaIllegalStateException() {
        ad.setAdStatus(AdStatus.DELETED);
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> promotionService.activate(10L, PromotionType.FEATURED, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nije dostupan");
    }

    @Test
    void activate_oglasJeSuspendovan_bacaIllegalStateException() {
        ad.setAdStatus(AdStatus.SUSPENDED_BY_ADMIN);
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> promotionService.activate(10L, PromotionType.FEATURED, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nije dostupan");
    }

    @Test
    void activate_oglasJeARCHIVED_aktiviraGaIPostavljaNoviExpiresAt() {
        ad.setAdStatus(AdStatus.ARCHIVED);
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(adPromotionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(adRepository.save(any())).thenReturn(ad);
        when(userRepository.save(any())).thenReturn(owner);
        when(creditTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        promotionService.activate(10L, PromotionType.FEATURED, 1L);

        assertThat(ad.getAdStatus()).isEqualTo(AdStatus.ACTIVE);
        assertThat(ad.getExpiresAt()).isNotNull();
    }

    @Test
    void activate_oglasNijePronadjen_bacaIllegalArgumentException() {
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.activate(999L, PromotionType.FEATURED, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Oglas nije pronađen");
    }

    @Test
    void activate_kreiraTransakcijuNegativnogIznosa() {
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(adPromotionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(adRepository.save(any())).thenReturn(ad);
        when(userRepository.save(any())).thenReturn(owner);

        ArgumentCaptor<CreditTransaction> txCaptor = ArgumentCaptor.forClass(CreditTransaction.class);
        when(creditTransactionRepository.save(txCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        promotionService.activate(10L, PromotionType.PRIORITY, 1L);

        CreditTransaction tx = txCaptor.getValue();
        assertThat(tx.getAmount()).isEqualByComparingTo(new BigDecimal("-250"));
        assertThat(tx.getTransactionType()).isEqualTo(TransactionType.PROMOTION_PURCHASE);
        assertThat(tx.getReferenceId()).isEqualTo(10L);
    }

    // ─── renewAd() ─────────────────────────────────────────────────────────────

    @Test
    void renewAd_happyPath_produzavaExpiresAtI30Dana() {
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(adRepository.save(any(Ad.class))).thenReturn(ad);

        promotionService.renewAd(10L, 1L);

        assertThat(ad.getExpiresAt()).isNotNull();
        // ExpiresAt mora biti negde u budućnosti (između 29 i 31 dan od sada)
        assertThat(ad.getExpiresAt()).isAfter(java.time.LocalDateTime.now().plusDays(29));
        verify(adRepository).save(ad);
    }

    @Test
    void renewAd_arhiviraniOglas_postavljaStatusNaACTIVE() {
        ad.setAdStatus(AdStatus.ARCHIVED);
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(adRepository.save(any(Ad.class))).thenReturn(ad);

        promotionService.renewAd(10L, 1L);

        assertThat(ad.getAdStatus()).isEqualTo(AdStatus.ACTIVE);
    }

    @Test
    void renewAd_aktivniOglas_neMenujaStatus() {
        ad.setAdStatus(AdStatus.ACTIVE);
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));
        when(adRepository.save(any(Ad.class))).thenReturn(ad);

        promotionService.renewAd(10L, 1L);

        assertThat(ad.getAdStatus()).isEqualTo(AdStatus.ACTIVE);
    }

    @Test
    void renewAd_oglasNePripadaKorisniku_bacaSecurityException() {
        User drugCorisnik = new User();
        drugCorisnik.setId(99L);
        ad.setOwner(drugCorisnik);

        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThatThrownBy(() -> promotionService.renewAd(10L, 1L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("sopstvene oglase");

        verify(adRepository, never()).save(any());
    }

    @Test
    void renewAd_obrisaniOglas_bacaIllegalStateException() {
        ad.setAdStatus(AdStatus.DELETED);
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThatThrownBy(() -> promotionService.renewAd(10L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ne može biti obnovljen");
    }

    @Test
    void renewAd_suspendovaniOglas_bacaIllegalStateException() {
        ad.setAdStatus(AdStatus.SUSPENDED_BY_ADMIN);
        when(adRepository.findById(10L)).thenReturn(Optional.of(ad));

        assertThatThrownBy(() -> promotionService.renewAd(10L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ne može biti obnovljen");
    }

    @Test
    void renewAd_oglasNijePronadjen_bacaIllegalArgumentException() {
        when(adRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.renewAd(999L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Oglas nije pronađen");
    }

    // ─── getCreditBalance() ────────────────────────────────────────────────────

    @Test
    void getCreditBalance_korisnikPostoji_vrataEgzaktnuVrednost() {
        owner.setCredit(new BigDecimal("750.50"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        BigDecimal result = promotionService.getCreditBalance(1L);

        assertThat(result).isEqualByComparingTo(new BigDecimal("750.50"));
    }

    @Test
    void getCreditBalance_korisnikSaNulaKredita_vrataZero() {
        owner.setCredit(BigDecimal.ZERO);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        BigDecimal result = promotionService.getCreditBalance(1L);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getCreditBalance_korisnikNijePronadjen_bacaIllegalArgumentException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.getCreditBalance(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Korisnik nije pronađen");
    }

    // ─── addCredit() ───────────────────────────────────────────────────────────

    @Test
    void addCredit_happyPath_dodajeKreditIKreiraTransakciju() {
        owner.setCredit(new BigDecimal("200.00"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.save(any(User.class))).thenReturn(owner);

        ArgumentCaptor<CreditTransaction> txCaptor = ArgumentCaptor.forClass(CreditTransaction.class);
        when(creditTransactionRepository.save(txCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // Email može da se pošalje ili ne — ne bacamo grešku
        doNothing().when(htmlEmailService).sendCreditAddedEmail(any(), any(), any(), any(), any(), any());

        promotionService.addCredit(1L, new BigDecimal("300.00"), "Test dopuna");

        // Kredit mora biti uvećan
        assertThat(owner.getCredit()).isEqualByComparingTo(new BigDecimal("500.00"));

        // Transakcija mora biti TOPUP_ADMIN sa pozitivnim iznosom
        CreditTransaction tx = txCaptor.getValue();
        assertThat(tx.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(tx.getTransactionType()).isEqualTo(TransactionType.TOPUP_ADMIN);
        assertThat(tx.getDescription()).isEqualTo("Test dopuna");
        assertThat(tx.getReferenceId()).isNull();

        verify(userRepository).save(owner);
        verify(creditTransactionRepository).save(any(CreditTransaction.class));
    }

    @Test
    void addCredit_negativanIznos_bacaIllegalArgumentException() {
        assertThatThrownBy(() -> promotionService.addCredit(1L, new BigDecimal("-50"), "negativno"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pozitivan");

        verify(userRepository, never()).findById(any());
    }

    @Test
    void addCredit_nultaVrednost_bacaIllegalArgumentException() {
        assertThatThrownBy(() -> promotionService.addCredit(1L, BigDecimal.ZERO, "nula"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pozitivan");
    }

    @Test
    void addCredit_korisnikNijePronadjen_bacaIllegalArgumentException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.addCredit(99L, new BigDecimal("100"), "opis"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Korisnik nije pronađen");

        verify(creditTransactionRepository, never()).save(any());
    }

    @Test
    void addCredit_emailGreska_nePrekinjeTransakciju() {
        owner.setCredit(BigDecimal.ZERO);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.save(any(User.class))).thenReturn(owner);
        when(creditTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Email baci RuntimeException
        doThrow(new RuntimeException("SMTP greška")).when(htmlEmailService).sendCreditAddedEmail(any(), any(), any(), any(), any(), any());

        // Ne sme da propagira grešku
        promotionService.addCredit(1L, new BigDecimal("100"), "dopuna");

        // Kredit mora biti sačuvan uprkos email grešci
        assertThat(owner.getCredit()).isEqualByComparingTo(new BigDecimal("100"));
        verify(userRepository).save(owner);
        verify(creditTransactionRepository).save(any());
    }

    // ─── getPackages() ─────────────────────────────────────────────────────────

    @Test
    void getPackages_vrataTriPaketa() {
        var packages = promotionService.getPackages();

        assertThat(packages).hasSize(3);
        assertThat(packages.stream().map(p -> p.getType()).toList())
                .containsExactly(PromotionType.FEATURED, PromotionType.PRIORITY, PromotionType.HIGHLIGHTED);
    }

    @Test
    void getPackages_ceneOdgovarajuEnumu() {
        var packages = promotionService.getPackages();

        assertThat(packages.get(0).getPriceRsd()).isEqualTo(500);   // FEATURED
        assertThat(packages.get(1).getPriceRsd()).isEqualTo(250);   // PRIORITY
        assertThat(packages.get(2).getPriceRsd()).isEqualTo(100);   // HIGHLIGHTED
    }
}
