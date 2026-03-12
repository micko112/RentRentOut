package org.landm.specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.landm.dto.rentalContract.RentalContractSearchDto;
import org.landm.entity.Ad;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.landm.entity.Enums.ContractStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

public class RentalContractSpecification {

	public static Specification<RentalContract> search(Long userId, boolean isAdmin, RentalContractSearchDto searchDto){
		return(root, query, cb) -> {
			
			String term = searchDto.getTerm();
			
			//query.distinct(true);
			
			List<Predicate> predicates = new ArrayList<>();

			Join<RentalContract, Ad> adJoin = root.join("ad", JoinType.LEFT);
			Join<Ad, User> ownerJoin = adJoin.join("owner", JoinType.LEFT);
			Join<RentalContract, User> lesseeJoin = root.join("lessee", JoinType.LEFT);
			
			Predicate isLessee = 
					cb.equal(lesseeJoin.get("id"), userId);
			
			Predicate isOwner = 
					cb.equal(ownerJoin.get("id"), userId);
			
			Predicate canAccess = isAdmin ? cb.conjunction() : cb.or(isLessee, isOwner);
			
			Predicate isDeleted = isAdmin ? cb.conjunction() : cb.notEqual(root.get("contractStatus"), ContractStatus.DELETED);
			
			if(searchDto != null) {
				if(searchDto.getStatus() != null) {
					predicates.add(
							cb.equal(root.get("contractStatus"), searchDto.getStatus())
							);
				}
				if(searchDto.getPriceFrom() != null) {
					predicates.add(
							cb.greaterThanOrEqualTo(root.get("agreedPrice"), searchDto.getPriceFrom())
							);
				}
				if(searchDto.getPriceTo() != null) {
					predicates.add(
							cb.lessThanOrEqualTo(root.get("agreedPrice"), searchDto.getPriceTo())
							);
				}
				if(searchDto.getStartDateFrom() != null) {
					predicates.add(
							cb.greaterThanOrEqualTo(root.get("startDate"), searchDto.getStartDateFrom())
							);
				}
				if(searchDto.getStartDateTo() != null) {
					predicates.add(
							cb.lessThanOrEqualTo(root.get("startDate"), searchDto.getStartDateTo())
							);
				}
				if(searchDto.getEndDateFrom() != null) {
					predicates.add(
							cb.greaterThanOrEqualTo(root.get("endDate"), searchDto.getEndDateFrom())
							);
				}
				if(searchDto.getEndDateTo() != null) {
					predicates.add(
							cb.lessThanOrEqualTo(root.get("endDate"), searchDto.getEndDateTo())
							);
				}
			}
			
			Predicate filterPredicate = cb.and(predicates.toArray(new Predicate[0]));
			
			if(term == null || term.isBlank()) {
				return cb.and(canAccess, isDeleted, filterPredicate);
			}
			
			String likeTerm = "%" + term.toLowerCase() + "%";
			
			predicates.add(
					cb.like(cb.lower(ownerJoin.get("firstname")), likeTerm)
					);
			
			predicates.add(
					cb.like(cb.lower(ownerJoin.get("lastname")), likeTerm)
					);
			
			predicates.add(
					cb.like(cb.lower(ownerJoin.get("email")), likeTerm)
					);
			
			predicates.add(
					cb.like(cb.lower(adJoin.get("title")), likeTerm)
					);
			
			try {
				BigDecimal price = new BigDecimal(term);
				predicates.add(
						cb.equal(root.get("agreedPrice"), price)
						);
			} catch (NumberFormatException ignore) {}
			
			
			try {
				LocalDate date = LocalDate.parse(term);
				predicates.add(
						cb.equal(root.get("startDate"), date)
						);
				predicates.add(
						cb.equal(root.get("endDate"), date)
						);
			} catch (DateTimeParseException ignore) {}
			
			Predicate searchPredicate = cb.or(predicates.toArray(new Predicate[0]));
			
			return cb.and(searchPredicate, filterPredicate, canAccess, isDeleted);
			
		};
	}
	
}
