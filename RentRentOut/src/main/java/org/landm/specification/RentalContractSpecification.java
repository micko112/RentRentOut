package org.landm.specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.landm.entity.Ad;
import org.landm.entity.RentalContract;
import org.landm.entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class RentalContractSpecification {

	public static Specification<RentalContract> search(String term, long userId, boolean isAdmin){
		return(root, query, cb) -> {
			
			query.distinct(true);
			
			List<Predicate> predicates = new ArrayList<>();

			Join<RentalContract, Ad> adJoin = root.join("ad", JoinType.LEFT);
			Join<Ad, User> ownerJoin = adJoin.join("owner", JoinType.LEFT);
			Join<RentalContract, User> lesseeJoin = root.join("lessee", JoinType.LEFT);
			
			Predicate isLessee = 
					cb.equal(lesseeJoin.get("id"), userId);
			
			Predicate isOwner = 
					cb.equal(ownerJoin.get("id"), userId);
			
			Predicate canAccess = cb.or(isLessee, isOwner);
			
			if(term == null || term.isBlank()) {
				return canAccess;
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
			
			if(isAdmin == true) return searchPredicate;
			
			return cb.and(searchPredicate, canAccess);
			
		};
	}
	
}
