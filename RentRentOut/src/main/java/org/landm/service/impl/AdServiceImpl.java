package org.landm.service.impl;

import jakarta.persistence.criteria.Predicate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.landm.dto.ad.*;
import org.landm.entity.*;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.ContractStatus;
import org.landm.helper.DateInterval;
import org.landm.mapper.AdMapper;
import org.landm.mapper.LocationMapper;
import org.landm.repository.*;
import org.landm.entity.Enums.NotificationType;
import org.landm.util.HtmlSanitizer;
import org.landm.exception.UserNotFoundException;
import org.landm.service.AdService;
import org.landm.service.CategoryService;
import org.landm.service.NotificationPersistenceService;
import org.landm.service.RentalContractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


@Service
public class AdServiceImpl implements AdService {

    private static final String CLOUDINARY_PREFIX = "https://res.cloudinary.com/drwxucq4m/";

    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final LocationMapper locationMapper;
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RentalContractRepository rentalContractRepository;
    private final RentalContractService rentalContractService;
    private final AdViewRepository adViewRepository;
    private final SavedAdRepository savedAdRepository;
    private final NotificationPersistenceService notificationPersistenceService;

    public AdServiceImpl(AdRepository adRepository, UserRepository userRepository,
                         AdMapper adMapper, LocationMapper locationMapper,
                         CategoryRepository categoryRepository, LocationRepository locationRepository,
                         RentalContractRepository rentalContractRepository,
                         RentalContractService rentalContractService, CategoryService categoryService,
                         AdViewRepository adViewRepository, SavedAdRepository savedAdRepository,
                         NotificationPersistenceService notificationPersistenceService) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.adMapper = adMapper;
        this.categoryRepository = categoryRepository;
        this.locationMapper = locationMapper;
        this.locationRepository = locationRepository;
        this.rentalContractRepository = rentalContractRepository;
        this.rentalContractService = rentalContractService;
        this.categoryService = categoryService;
        this.adViewRepository = adViewRepository;
        this.savedAdRepository = savedAdRepository;
        this.notificationPersistenceService = notificationPersistenceService;
    }

    private void validateImageUrls(List<String> images) {
        if (images == null) return;
        for (String url : images) {
            if (url == null || !url.startsWith(CLOUDINARY_PREFIX)) {
                throw new IllegalArgumentException("Nevažeći URL slike.");
            }
        }
    }

    @Override
    @Transactional
    public AdDto create(CreateAdRequestDto req, Long userId) {
        validateImageUrls(req.getImages());
        User owner = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Location location = locationRepository.findById(req.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
        Ad adToCreate = new Ad(
                HtmlSanitizer.sanitize(req.getTitle()),
                HtmlSanitizer.sanitize(req.getDescription()),
                req.getPrice(),
                req.getCurrency(),
                req.getPriceInterval(),
                owner,
                category,
                location,
                AdStatus.ACTIVE,
                req.getTotalQuantity(),
                req.getImages()
                );
        adToCreate.setPricePerWeek(req.getPricePerWeek());
        adToCreate.setPricePerMonth(req.getPricePerMonth());
        return adMapper.toDto(adRepository.save(adToCreate));
    }

    @Override
    public AdDto getAdById(Long id) {
        Ad ad = adRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        List<RentalContract> contracts = rentalContractService
                .findByAdIdAndContractStatusIn(ad.getId(), List.of(ContractStatus.ACCEPTED, ContractStatus.ACTIVE, ContractStatus.BLOCKED_BY_OWNER));
        List<DateInterval> blockedIntervals = getBlockedIntervals(contracts, ad.getTotalQuantity());

        AdDto adDto = adMapper.toDto(ad);
        adDto.setBlockedIntervals(blockedIntervals);

        return adDto;
    }

    @Override
    public AdDto getAdById(Long id, Long userId) {
        AdDto adDto = getAdById(id);
        if (userId != null) {
            adDto.setSaved(savedAdRepository.existsByUserIdAndAdId(userId, id));
        }
        return adDto;
    }
    
    private static class Event{
    	LocalDate date;
    	Long itemCount;
    	
    	Event(LocalDate date, Long amount){
    		this.date = date;
    		this.itemCount = amount;
    	}
    }
    
    public List<DateInterval> getBlockedIntervals(List<RentalContract> contracts, int quantity){
    	
    	List<Event> events = new ArrayList<>(); 
    	
    	List<DateInterval> blockedIntervals = new ArrayList<>();
    	
    	for (RentalContract rc : contracts) {
    		events.add(new Event(rc.getStartDate(), rc.getAmount()));
    		
    		events.add(new Event(rc.getEndDate().plusDays(1), -rc.getAmount()));
    	}
    	
    	events.sort(Comparator.comparing(e -> e.date));
    	
    	int active = 0;
    	LocalDate blockedStart = null;
    	
    	for(Event event : events) {

    		int prevActive = active;
    		active += event.itemCount;

    		if(prevActive < quantity && active >= quantity) {
    			blockedStart = event.date;
    		}

    		if(prevActive >= quantity && active < quantity) {
    			blockedIntervals.add(new DateInterval(blockedStart, event.date.minusDays(1)));
    			blockedStart = null;
    		}
    	}

    	// Zatvori poslednji interval ako ostaje otvoren (sve je zauzeto do kraja podataka)
    	if (blockedStart != null) {
    		LocalDate farFuture = LocalDate.now().plusYears(5);
    		blockedIntervals.add(new DateInterval(blockedStart, farFuture));
    	}

    	return blockedIntervals;
    }
    
    public int getAvailableAmountForInterval(List<RentalContract> contracts, LocalDate startDate,
    		LocalDate endDate, int totalAmount) {
    	List<Event> events = new ArrayList<>();

    	int avaliableAmountForDates = totalAmount;

    	for(RentalContract rc : contracts) {
    		events.add(new Event(rc.getStartDate(), rc.getAmount()));
    		
    		events.add(new Event(rc.getEndDate().plusDays(1), -rc.getAmount()));
    	}
    	
    	events.sort(Comparator.comparing(e -> e.date));
    	
    	int currUsed = 0;
    	int availableItems = totalAmount;
    	boolean firstInRange = true;

    	for (Event e : events) {
    		if(e.date.isAfter(endDate)) break;

    		if(e.date.isBefore(startDate)) {
    			currUsed += e.itemCount;
    			continue;
    		}

    		// Pre prvog in-range eventa: proveri stanje na osnovu pre-range rezervacija
    		if (firstInRange) {
    			avaliableAmountForDates = Math.min(availableItems - currUsed, avaliableAmountForDates);
    			firstInRange = false;
    		}

    		currUsed += e.itemCount;
    		int availableNow = availableItems - currUsed;
    		avaliableAmountForDates = Math.min(availableNow, avaliableAmountForDates);

    	}

    	// Ako nema in-range eventa, provjeri stanje na osnovu pre-range rezervacija
    	if (firstInRange) {
    		avaliableAmountForDates = Math.min(availableItems - currUsed, avaliableAmountForDates);
    	}

    	return avaliableAmountForDates;

    }

    @Override
    public Page<AdPreviewDto> getAllActiveAds(Pageable pageable) {
        Page<Ad> adPage = adRepository.findAllByAdStatus(AdStatus.ACTIVE, pageable);
        return adPage.map(adMapper::toPreviewDto);
    }

    @Override
    public Page<AdPreviewDto> getAllActiveAds(Pageable pageable, Long userId) {
        Page<Ad> adPage = adRepository.findAllByAdStatus(AdStatus.ACTIVE, pageable);
        Set<Long> savedAdIds = getSavedAdIds(userId, adPage);
        return adPage.map(ad -> {
            AdPreviewDto dto = adMapper.toPreviewDto(ad);
            dto.setSaved(savedAdIds.contains(ad.getId()));
            return dto;
        });
    }

    @Override
    @Transactional
    public AdDto updateAd(UpdateAdRequestDto req, Long id, Long userId) {
        validateImageUrls(req.getImages());
        Ad adToUpdate = adRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Ad not found"));
        if (!adToUpdate.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Nemate dozvolu za izmenu ovog oglasa.");
        }

        if (adToUpdate.getAdStatus() == AdStatus.DELETED) {
            throw new IllegalStateException("Ne možete izmeniti obrisan oglas.");
        }
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Location location = locationRepository.findById(req.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
        adToUpdate.setTitle(HtmlSanitizer.sanitize(req.getTitle()));
        adToUpdate.setDescription(HtmlSanitizer.sanitize(req.getDescription()));
        adToUpdate.setPrice(req.getPrice());
        adToUpdate.setPricePerWeek(req.getPricePerWeek());
        adToUpdate.setPricePerMonth(req.getPricePerMonth());
        adToUpdate.setPriceInterval(req.getPriceInterval());
        adToUpdate.setCurrency(req.getCurrency());
        adToUpdate.setImages(req.getImages());
        adToUpdate.setCategory(category);
        adToUpdate.setLocation(location);

        adToUpdate.setTotalQuantity(req.getTotalQuantity());

        Ad savedAd = adRepository.save(adToUpdate);
        return adMapper.toDto(savedAd);
    }

    @Transactional
	@Override
	public String deleteAd(Long adId, Long userId) {
		
		Ad currAd = adRepository.findById(adId)
				.orElseThrow(() -> new IllegalArgumentException("Ad not found"));
		
		if(!currAd.getOwner().getId().equals(userId)) {
			throw new AccessDeniedException("Nemate dozvolu za brisanje ovog oglasa.");
		}

		if(rentalContractRepository.hasActiveOrFutureContracts(adId)) {
			throw new IllegalStateException("Ne možete obrisati oglas koji ima aktivne ugovore.");
		}

		if(currAd.getAdStatus() != AdStatus.DELETED) {
			currAd.setAdStatus(AdStatus.DELETED);
			rentalContractService.markToAdDeleted(currAd.getId());
		}else {
			throw new IllegalStateException("Oglas je već obrisan.");
		}
		
		return "Successfully deleted your Ad!";
	}

    private Specification<Ad> buildSearchSpec(AdSearchCriteriaDto criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
                String escaped = criteria.getKeyword().toLowerCase()
                        .replace("!", "!!")
                        .replace("%", "!%")
                        .replace("_", "!_");
                String keywordPattern = "%" + escaped + "%";
                Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keywordPattern, '!');
                Predicate descriptionLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), keywordPattern, '!');
                predicates.add(criteriaBuilder.or(titleLike, descriptionLike));
            }
            if (criteria.getCategoryId() != null) {
                List<Long> categoryIds = categoryService.findAllSubCategoryId(criteria.getCategoryId());
                if (!categoryIds.isEmpty()) {
                    predicates.add(root.get("category").get("id").in(categoryIds));
                }
            }
            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }
            if (criteria.getLocationId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("location").get("id"), criteria.getLocationId()));
            }
            if (criteria.getCity() != null && !criteria.getCity().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("location").get("city"), criteria.getCity()));
            }
            if (criteria.getPriceInterval() != null) {
                predicates.add(criteriaBuilder.equal(root.get("priceInterval"), criteria.getPriceInterval()));
            }
            predicates.add(criteriaBuilder.equal(root.get("adStatus"), AdStatus.ACTIVE));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public Page<AdPreviewDto> search(AdSearchCriteriaDto criteria, Pageable pageable) {
        Page<Ad> adPage = adRepository.findAll(buildSearchSpec(criteria), pageable);
        return adPage.map(adMapper::toPreviewDto);
    }

    @Override
    public Page<AdPreviewDto> search(AdSearchCriteriaDto criteria, Pageable pageable, Long userId) {
        Page<Ad> adPage = adRepository.findAll(buildSearchSpec(criteria), pageable);
        Set<Long> savedAdIds = getSavedAdIds(userId, adPage);
        return adPage.map(ad -> {
            AdPreviewDto dto = adMapper.toPreviewDto(ad);
            dto.setSaved(savedAdIds.contains(ad.getId()));
            return dto;
        });
    }

    private Set<Long> getSavedAdIds(Long userId, Page<Ad> adPage) {
        if (userId == null) {
            return Set.of();
        }
        List<Long> adIds = adPage.getContent().stream().map(Ad::getId).toList();
        if (adIds.isEmpty()) return Set.of();
        return savedAdRepository.findSavedAdIdsByUserIdAndAdIdIn(userId, adIds);
    }

    @Override
    public Page<AdPreviewDto> findAll(Pageable pageable) {
        Page<Ad> adPage = adRepository.findAll(pageable);
        return adPage.map(adMapper::toPreviewDto);
    }

    @Override
    public Page<AdPreviewDto> findAllByUser(Pageable pageable, Long userId) {
        Page<Ad> adPage = adRepository.findAllByOwnerId(userId, pageable);
        return adPage.map(adMapper::toPreviewDto);
    }

    @Override
    public Page<AdPreviewDto> findAllActiveByUser(Pageable pageable, Long userId) {
        Page<Ad> adPage = adRepository.findAllByOwnerIdAndAdStatus(userId, AdStatus.ACTIVE, pageable);
        return adPage.map(adMapper::toPreviewDto);
    }

    @Override
    @Transactional
    public void recordView(Long adId, Long userId) {
        if (adViewRepository.existsByUserIdAndAdId(userId, adId)) return;
        try {
            Ad ad = adRepository.findById(adId)
                    .orElseThrow(() -> new IllegalArgumentException("Ad not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            AdView adView = new AdView(user, ad);
            adViewRepository.save(adView);
            ad.setViewCount(ad.getViewCount() + 1);
            adRepository.save(ad);
        } catch (DataIntegrityViolationException e) {
            // Prava race condition — dva paralelna zahteva, pre-check je prošao za oba
        }
    }

    @Override
    @Transactional
    public void saveAd(Long adId, Long userId) {
        if (savedAdRepository.existsByUserIdAndAdId(userId, adId)) return;
        try {
            Ad ad = adRepository.findById(adId)
                    .orElseThrow(() -> new IllegalArgumentException("Ad not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            SavedAd savedAd = new SavedAd(user, ad);
            savedAdRepository.save(savedAd);
            ad.setSaveCount(ad.getSaveCount() + 1);
            adRepository.save(ad);
            if (!ad.getOwner().getId().equals(userId)) {
                notificationPersistenceService.create(
                    ad.getOwner().getId(),
                    NotificationType.AD_SAVED,
                    "Neko je sačuvao vaš oglas",
                    user.getFirstname() + " " + user.getLastname() + " je sačuvao/la vaš oglas \"" + ad.getTitle() + "\"",
                    ad.getId(),
                    "AD",
                    user.getFirstname() + " " + user.getLastname()
                );
            }
        } catch (DataIntegrityViolationException e) {
            // Prava race condition — dva paralelna zahteva, pre-check je prošao za oba
        }
    }

    @Override
    @Transactional
    public void unsaveAd(Long adId, Long userId) {
        int deleted = savedAdRepository.deleteByUserIdAndAdId(userId, adId);
        if (deleted > 0) {
            adRepository.findById(adId).ifPresent(ad -> {
                ad.setSaveCount(Math.max(0, ad.getSaveCount() - 1));
                adRepository.save(ad);
            });
        }
    }

    @Override
    public boolean isSaved(Long adId, Long userId) {
        return savedAdRepository.existsByUserIdAndAdId(userId, adId);
    }

    @Override
    public Page<AdPreviewDto> getSavedAds(Long userId, Pageable pageable) {
        Page<SavedAd> savedAds = savedAdRepository.findAllByUserId(userId, pageable);
        return savedAds.map(savedAd -> {
            AdPreviewDto dto = adMapper.toPreviewDto(savedAd.getAd());
            dto.setSaved(true);
            return dto;
        });
    }

}
