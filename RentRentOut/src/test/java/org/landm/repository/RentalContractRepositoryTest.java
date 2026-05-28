package org.landm.repository;

import org.junit.jupiter.api.Test;
import org.landm.entity.Ad;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.ContractStatus;
import org.landm.entity.Enums.Currency;
import org.landm.entity.Enums.PriceInterval;
import org.landm.entity.RentalContract;
import org.landm.entity.Role;
import org.landm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("test")
public class RentalContractRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private RentalContractRepository rentalContractRepository;

    @Test
    void hasActiveOrFutureContracts_returnsTrue() {
        User owner = createUser();
        User lessee = createUser();
        Ad ad = createAd(owner);

        em.persist(createContract(ad, lessee,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10),
                ContractStatus.ACTIVE));
        em.flush();
        em.clear();

        boolean result = rentalContractRepository.hasActiveOrFutureContracts(ad.getId());

        assertThat(result).isTrue();
    }

    @Test
    void hasActiveOrFutureContracts_onlyFinished_returnsFalse() {
        User owner = createUser();
        User lessee = createUser();
        Ad ad = createAd(owner);

        em.persist(createContract(ad, lessee,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10),
                ContractStatus.FINISHED));
        em.flush();
        em.clear();

        boolean result = rentalContractRepository.hasActiveOrFutureContracts(ad.getId());

        assertThat(result).isFalse();
    }

    private User createUser() {
        Role role = new Role();
        role.setName("USER_" + UUID.randomUUID());
        em.persist(role);

        User u = new User();
        u.setEmail("u" + System.nanoTime() + "@test.com");
        u.setPassword("x");
        u.setFirstname("Pera");
        u.setLastname("Perić");
        u.setCurrency(Currency.RSD);
        u.setRole(role);
        em.persist(u);
        return u;
    }

    private Ad createAd(User owner) {
        Ad ad = new Ad();
        ad.setTitle("Test");
        ad.setDescription("Opis");
        ad.setPrice(BigDecimal.valueOf(100));
        ad.setCurrency(Currency.RSD);
        ad.setPriceInterval(PriceInterval.PER_DAY);
        ad.setTotalQuantity(1);
        ad.setOwner(owner);
        ad.setAdStatus(AdStatus.ACTIVE);
        em.persist(ad);
        return ad;
    }

    private RentalContract createContract(Ad ad, User lessee, LocalDate start, LocalDate end, ContractStatus status) {
        RentalContract rc = new RentalContract();
        rc.setAd(ad);
        rc.setLessee(lessee);
        rc.setStartDate(start);
        rc.setEndDate(end);
        rc.setAgreedPrice(BigDecimal.valueOf(500));
        rc.setCurrency(Currency.RSD);
        rc.setAmount(1L);
        rc.setContractStatus(status);
        return rc;
    }
}
