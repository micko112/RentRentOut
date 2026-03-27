package org.landm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.ad.AdSearchCriteriaDto;
import org.landm.entity.Ad;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.Currency;
import org.landm.entity.Enums.PriceInterval;
import org.landm.entity.Enums.PromotionType;
import org.landm.mapper.AdMapper;
import org.landm.repository.AdRepository;
import org.landm.repository.AdViewRepository;
import org.landm.repository.CategoryRepository;
import org.landm.repository.LocationRepository;
import org.landm.repository.RentalContractRepository;
import org.landm.repository.SavedAdRepository;
import org.landm.repository.UserRepository;
import org.landm.service.impl.AdServiceImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdServiceSearchTest {

    @Mock private AdRepository adRepository;
    @Mock private UserRepository userRepository;
    @Mock private AdMapper adMapper;
    @Mock private org.landm.mapper.LocationMapper locationMapper;
    @Mock private CategoryService categoryService;
    @Mock private CategoryRepository categoryRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private RentalContractRepository rentalContractRepository;
    @Mock private RentalContractService rentalContractService;
    @Mock private AdViewRepository adViewRepository;
    @Mock private SavedAdRepository savedAdRepository;
    @Mock private NotificationPersistenceService notificationPersistenceService;

    @InjectMocks
    private AdServiceImpl adService;

    private Ad testAd;
    private AdPreviewDto testDto;

    @BeforeEach
    void setUp() {
        testAd = new Ad();
        testAd.setId(1L);
        testAd.setTitle("Test oglas");
        testAd.setAdStatus(AdStatus.ACTIVE);
        testAd.setPromotionRank(0);
        testAd.setCreatedAt(LocalDateTime.now());

        testDto = new AdPreviewDto();
        testDto.setId(1L);
        testDto.setTitle("Test oglas");
    }

    // ─── promoSort = true ─────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void search_promoSortTrue_dodajePromotionRankDESCSortNaPocetku() {
        AdSearchCriteriaDto criteria = new AdSearchCriteriaDto();
        criteria.setPromoSort(true);

        Pageable inputPageable = PageRequest.of(0, 9, Sort.by(Sort.Direction.ASC, "price"));

        Page<Ad> adPage = new PageImpl<>(List.of(testAd));
        when(adRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(adPage);
        when(adMapper.toPreviewDto(any(Ad.class))).thenReturn(testDto);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        adService.search(criteria, inputPageable);

        verify(adRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        Sort capturedSort = capturedPageable.getSort();

        // promotionRank DESC mora biti na prvom mestu
        List<Sort.Order> orders = capturedSort.toList();
        assertThat(orders).isNotEmpty();
        assertThat(orders.get(0).getProperty()).isEqualTo("promotionRank");
        assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_promoSortTrue_originalniSortOstajePoslePromotionRank() {
        AdSearchCriteriaDto criteria = new AdSearchCriteriaDto();
        criteria.setPromoSort(true);

        // Korisnik traži sortiranje po ceni ASC
        Pageable inputPageable = PageRequest.of(0, 9, Sort.by(Sort.Direction.ASC, "price"));

        Page<Ad> adPage = new PageImpl<>(List.of(testAd));
        when(adRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(adPage);
        when(adMapper.toPreviewDto(any(Ad.class))).thenReturn(testDto);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        adService.search(criteria, inputPageable);
        verify(adRepository).findAll(any(Specification.class), pageableCaptor.capture());

        List<Sort.Order> orders = pageableCaptor.getValue().getSort().toList();

        // Mora biti: [promotionRank DESC, price ASC]
        assertThat(orders).hasSizeGreaterThanOrEqualTo(2);
        assertThat(orders.get(0).getProperty()).isEqualTo("promotionRank");
        assertThat(orders.get(1).getProperty()).isEqualTo("price");
        assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_promoSortTrueBezEksplicitnogSorta_dodajeCreatedAtDESCKaoFallback() {
        AdSearchCriteriaDto criteria = new AdSearchCriteriaDto();
        criteria.setPromoSort(true);

        // Pageable bez ikakvog sorta
        Pageable inputPageable = PageRequest.of(0, 9);

        Page<Ad> adPage = new PageImpl<>(List.of(testAd));
        when(adRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(adPage);
        when(adMapper.toPreviewDto(any(Ad.class))).thenReturn(testDto);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        adService.search(criteria, inputPageable);
        verify(adRepository).findAll(any(Specification.class), pageableCaptor.capture());

        List<Sort.Order> orders = pageableCaptor.getValue().getSort().toList();

        // Mora biti: [promotionRank DESC, createdAt DESC]
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getProperty()).isEqualTo("promotionRank");
        assertThat(orders.get(1).getProperty()).isEqualTo("createdAt");
        assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    // ─── promoSort = false ────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void search_promoSortFalse_korisiOriginalniPageableBezIzmene() {
        AdSearchCriteriaDto criteria = new AdSearchCriteriaDto();
        criteria.setPromoSort(false);

        Pageable inputPageable = PageRequest.of(0, 9, Sort.by(Sort.Direction.ASC, "price"));

        Page<Ad> adPage = new PageImpl<>(List.of(testAd));
        when(adRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(adPage);
        when(adMapper.toPreviewDto(any(Ad.class))).thenReturn(testDto);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        adService.search(criteria, inputPageable);
        verify(adRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        List<Sort.Order> orders = capturedPageable.getSort().toList();

        // Sa promoSort=false isti pageable se prosledi — nema promotionRank
        assertThat(orders.stream().map(Sort.Order::getProperty))
                .doesNotContain("promotionRank");
        assertThat(orders.get(0).getProperty()).isEqualTo("price");
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_promoSortFalse_nePozivaWithPromotionSortLogiku() {
        AdSearchCriteriaDto criteria = new AdSearchCriteriaDto();
        criteria.setPromoSort(false);

        Pageable inputPageable = PageRequest.of(1, 6);

        Page<Ad> adPage = new PageImpl<>(List.of());
        when(adRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(adPage);

        adService.search(criteria, inputPageable);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(adRepository).findAll(any(Specification.class), pageableCaptor.capture());

        // Pageble mora biti isti objekat (br. stranice, veličina)
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(6);
    }

    // ─── mapiranje u AdPreviewDto ──────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void search_rezultatiSeMapiraju_svakiAdProlaziFunkciom_toPreviewDto() {
        AdSearchCriteriaDto criteria = new AdSearchCriteriaDto();
        criteria.setPromoSort(false);

        Ad ad1 = new Ad(); ad1.setId(1L); ad1.setAdStatus(AdStatus.ACTIVE);
        Ad ad2 = new Ad(); ad2.setId(2L); ad2.setAdStatus(AdStatus.ACTIVE);

        AdPreviewDto dto1 = new AdPreviewDto(); dto1.setId(1L);
        AdPreviewDto dto2 = new AdPreviewDto(); dto2.setId(2L);

        Page<Ad> adPage = new PageImpl<>(List.of(ad1, ad2));
        when(adRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(adPage);
        when(adMapper.toPreviewDto(ad1)).thenReturn(dto1);
        when(adMapper.toPreviewDto(ad2)).thenReturn(dto2);

        Page<AdPreviewDto> result = adService.search(criteria, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(result.getContent().get(1).getId()).isEqualTo(2L);

        verify(adMapper).toPreviewDto(ad1);
        verify(adMapper).toPreviewDto(ad2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_praznaStranica_vrataPageBezElemenata() {
        AdSearchCriteriaDto criteria = new AdSearchCriteriaDto();
        criteria.setPromoSort(false);

        Page<Ad> emptyPage = new PageImpl<>(List.of());
        when(adRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        Page<AdPreviewDto> result = adService.search(criteria, PageRequest.of(0, 9));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(adMapper, never()).toPreviewDto(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_promoSortTrue_mapiraSvakiAdUDto() {
        AdSearchCriteriaDto criteria = new AdSearchCriteriaDto();
        criteria.setPromoSort(true);

        testAd.setPromotionRank(3);
        testAd.setPromotionType(PromotionType.FEATURED);

        AdPreviewDto expectedDto = new AdPreviewDto();
        expectedDto.setId(1L);
        expectedDto.setPromotionType(PromotionType.FEATURED);

        Page<Ad> adPage = new PageImpl<>(List.of(testAd));
        when(adRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(adPage);
        when(adMapper.toPreviewDto(testAd)).thenReturn(expectedDto);

        Page<AdPreviewDto> result = adService.search(criteria, PageRequest.of(0, 9));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPromotionType()).isEqualTo(PromotionType.FEATURED);
    }
}
