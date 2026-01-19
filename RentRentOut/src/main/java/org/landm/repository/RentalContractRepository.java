package org.landm.repository;

import java.util.List;

import org.landm.entity.RentalContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {
	
	@Query(" SELECT rc FROM RentalContract rc WHERE rc.lessee.id = :userId OR rc.ad.owner.id = :userId ")
	public List<RentalContract> findAllByUser(long userId);
	
	@Query("""
			SELECT rc 
			FROM RentalContract rc
			WHERE rc.id = :contractId
			""")
	public RentalContract findByIdForUpdate(long contractId);
	
}
