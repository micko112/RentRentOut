package org.landm.service.impl;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.landm.dto.ad.*;
import org.landm.entity.*;
import org.landm.entity.Enums.AdStatus;
import org.landm.entity.Enums.ContractStatus;
import org.landm.helper.DateInterval;
import org.landm.mapper.AdMapper;
import org.landm.mapper.LocationMapper;
import org.landm.repository.*;
import org.landm.security.JwtUtil;
import org.landm.service.AdService;
import org.landm.service.CategoryService;
import org.landm.service.RentalContractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
public class AdServiceImpl implements AdService {

    private final UserRepository userRepository;
    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final LocationMapper locationMapper;
    private final CategoryService categoryService;
    private final JwtUtil jwtUtil;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RentalContractRepository rentalContractRepository;
    private final RentalContractService rentalContractService;

    public AdServiceImpl(AdRepository adRepository, UserRepository userRepository,
                         AdMapper adMapper, LocationMapper locationMapper, 
                         CategoryRepository categoryRepository, LocationRepository locationRepository, 
                         JwtUtil jwtUtil, RentalContractRepository rentalContractRepository, 
                         RentalContractService rentalContractService, CategoryService categoryService) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.adMapper = adMapper;
        this.categoryRepository = categoryRepository;
        this.locationMapper = locationMapper;
        this.locationRepository = locationRepository;
        this.jwtUtil = jwtUtil;
        this.rentalContractRepository = rentalContractRepository;
        this.rentalContractService = rentalContractService ;
        this.categoryService = categoryService;
    }

//    @Override
//    public ItemDto create(CreateItemRequestDto req, Long userId) {

//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String email = auth.getName();
//
//    }

    @Override
    public AdDto create(CreateAdRequestDto req, Long userId) {
        User owner = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + req.getCategoryId()));
        Location location = locationRepository.findById(req.getLocationId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + req.getCategoryId()));
        Ad adToCreate = new Ad(
                req.getTitle(),
                req.getDescription(),
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
        return adMapper.toDto(adRepository.save(adToCreate));
    }

    @Override
    public AdDto getAdById(Long id) {
        Ad ad = adRepository.findById(id).orElseThrow(() -> new RuntimeException("Ad not found with id: " + id));
        
        List<RentalContract> contracts = rentalContractService
        		.findByAdIdAndContractStatusIn(ad.getId(), List.of(ContractStatus.ACCEPTED, ContractStatus.ACTIVE, ContractStatus.BLOCKED_BY_OWNER));
        //TREBA TRANSACTIONAL
        System.out.println("DEBUG: Broj ugovora za oglas " + id + " je: " + contracts.size());
        List<DateInterval> blockedIntervals = getBlockedIntervals(contracts, ad.getTotalQuantity());
        
        AdDto adDto = adMapper.toDto(ad);
        adDto.setBlockedIntervals(blockedIntervals);
        
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
    			blockedIntervals.add(new DateInterval(blockedStart, event.date.minusDays(0)));
    			blockedStart = null;
    		}
    	}
    	
    	return blockedIntervals;
    }
    
    public int getAvailableAmountForInterval(List<RentalContract> contracts, LocalDate startDate, 
    		LocalDate endDate, int totalAmount) {
    	List<Event> events = new ArrayList<>();
    	
    	int avaliableAmountForDates = 0;
    	
    	for(RentalContract rc : contracts) {
    		events.add(new Event(rc.getStartDate(), rc.getAmount()));
    		
    		events.add(new Event(rc.getEndDate().plusDays(1), -rc.getAmount()));
    	}
    	
    	events.sort(Comparator.comparing(e -> e.date));
    	
    	int currUsed = 0;
    	int availableItems = totalAmount;
    	
    	for (Event e : events) {
    		if(e.date.isAfter(endDate)) break;
    		
    		if(e.date.isBefore(startDate)) {
    			currUsed += e.itemCount;
    			continue;
    		}
    		
    		currUsed += e.itemCount;
    		int availableNow = availableItems - currUsed;
    		avaliableAmountForDates = Math.min(availableNow, avaliableAmountForDates);
    		
    	}
    	
    	return avaliableAmountForDates;
    	
    } 

    @Override
    public Page<AdPreviewDto> getAllActiveAds(Pageable pageable) {

        Page<Ad> adPage = adRepository.findAllByAdStatus(AdStatus.ACTIVE, pageable);
        return adPage.map(adMapper::toPreviewDto);
    }

    @Override
    public AdDto updateAd(UpdateAdRequestDto req, Long id, Long userId) {

        Ad adToUpdate = adRepository.findById(id).orElseThrow(() -> new RuntimeException("There is no ad"));
        if (adToUpdate.getOwner().getId() != (userId)) {
            throw new RuntimeException("You are not the owner of this ad");
        }

        if (adToUpdate.getAdStatus() == AdStatus.DELETED) {
            throw new IllegalStateException("Cannot update ad with status: " + adToUpdate.getAdStatus());
        }
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + req.getCategoryId()));

        Location location = null;
        if(req.getLocationId() != null){
            location = locationRepository.findById(req.getLocationId()).orElseThrow(() ->
                    new RuntimeException("Location not found with id: " + req.getLocationId()));
        }
        adToUpdate.setTitle(req.getTitle());
        adToUpdate.setDescription(req.getDescription());
        adToUpdate.setPrice(req.getPrice());
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
				.orElseThrow(() -> new RuntimeException("Error deleting ad - ad not found"));
		
		if(currAd.getOwner().getId() != userId) {
			throw new RuntimeException("Deleting someone's ad - not allowed!");
		}
		
		if(rentalContractRepository.hasActiveOrFutureContracts(adId)) {
			throw new RuntimeException("Trying to delete Ad contained in ongoing contract - not allowed!");
		}
		
		if(currAd.getAdStatus() != AdStatus.DELETED) {
			currAd.setAdStatus(AdStatus.DELETED);
			rentalContractService.markToAdDeleted(currAd.getId());
		}else {
			throw new RuntimeException("Ad already deleted!");
		}
		
		return "Successfully deleted your Ad!";
	}

    @Transactional
    @Override
    public AdDto updateAdStatus(Long adId, AdStatus newStatus, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));
        if (!ad.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to modify this ad");
        }
        if (newStatus == AdStatus.DELETED || newStatus == AdStatus.SUSPENDED_BY_ADMIN) {
            throw new RuntimeException("Invalid status transition — use the delete endpoint instead.");
        }
        ad.setAdStatus(newStatus);
        return adMapper.toDto(adRepository.save(ad));
    }

    @Override
    public Page<AdPreviewDto> search(AdSearchCriteriaDto criteria, Pageable pageable) {
        Specification<Ad> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if(criteria.getKeyword() != null && !criteria.getKeyword().isBlank()){
                String keywordPattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keywordPattern);
                Predicate descriptionLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), keywordPattern);
                predicates.add(criteriaBuilder.or(titleLike, descriptionLike));
            }
            if(criteria.getCategoryId() != null){
                List<Long> categoryIds = categoryService.findAllSubCategoryId(criteria.getCategoryId());
                if (!categoryIds.isEmpty()){
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
            predicates.add(criteriaBuilder.equal(root.get("adStatus"), AdStatus.ACTIVE));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Ad> adPage = adRepository.findAll(specification, pageable);
        return adPage.map(adMapper::toPreviewDto);
    }

    @Override
    public Page<AdPreviewDto> findAll(Pageable pageable) {
        Page<Ad> adPage = adRepository.findAll(pageable);
        return adPage.map(adMapper::toPreviewDto);
    }
    @Override
    public Page<AdPreviewDto> findAllByUser(Pageable pageable, Long userId){
        Page<Ad> adPage = adRepository.findAllByOwnerId(userId, pageable);
        return adPage.map(adMapper::toPreviewDto);
    }



}
