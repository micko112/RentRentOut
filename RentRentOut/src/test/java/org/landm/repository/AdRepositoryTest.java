package org.landm.repository;


import org.junit.jupiter.api.Test;
import org.landm.entity.Ad;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.Currency;
import org.landm.entity.Enums.PriceInterval;
import org.landm.entity.Role;
import org.landm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;



@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE) // koristi H2 URL iz application-test.properties
@ActiveProfiles("test")
class AdRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private AdRepository adRepository;

    @Test
    void findAllActiveIds() {
        User owner = createOwner();
        em.persist(owner);

        Ad ad = createAd(owner, AdStatus.ACTIVE);

        em.persist(ad);
        em.flush();
        em.clear();
        List<Long> ids = adRepository.findAllActiveIds();
        assertThat(ids).contains(ad.getId());
    }
    @Test
    void findAllActiveIds_returnsAllActiveDesc() {
        User owner = createOwner();
        em.persist(owner);

        Ad ad1 = createAd(owner, AdStatus.ACTIVE);
        Ad ad2 = createAd(owner, AdStatus.ACTIVE);
        Ad ad3 = createAd(owner, AdStatus.ARCHIVED);

        em.persist(ad1);
        em.persist(ad2);
        em.persist(ad3);
        em.flush();
        em.clear();

        List<Long> ids = adRepository.findAllActiveIds();
        assertThat(ids).containsExactly( ad2.getId(), ad1.getId());
        assertThat(ids).doesNotContain(ad3.getId());


    }
    @Test
    void findAllByAdStatus_returnsOnlyActiveAds() {
        User owner = createOwner();
        em.persist(owner);

        Ad active = createAd(owner, AdStatus.ACTIVE);
        Ad archived = createAd(owner, AdStatus.ARCHIVED);
        em.persist(active);
        em.persist(archived);

        em.flush();
        em.clear();

        Page<Ad> results = adRepository.findAllByAdStatus(AdStatus.ACTIVE, PageRequest.of(0, 10));

        assertThat(results.getContent())
                .hasSize(1)
                .extracting(Ad::getId)
                .containsExactly(active.getId());

    }
    private User createOwner() {
        Role role = new Role();
        role.setName("USER");
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

    private Ad createAd(User owner, AdStatus status) {
        Ad ad = new Ad();
        ad.setTitle("Test");
        ad.setDescription("Opis");
        ad.setPrice(BigDecimal.valueOf(100));
        ad.setCurrency(Currency.RSD);
        ad.setPriceInterval(PriceInterval.PER_DAY);
        ad.setTotalQuantity(1);
        ad.setOwner(owner);
        ad.setAdStatus(status);
        return ad;
    }

}
