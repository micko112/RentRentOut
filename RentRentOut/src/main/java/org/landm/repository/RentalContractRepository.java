package org.landm.repository;

import java.time.LocalDate;
import java.util.List;

import org.landm.entity.RentalContract;
import org.landm.entity.Enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface RentalContractRepository 
extends JpaRepository<RentalContract, Long>, JpaSpecificationExecutor<RentalContract> {
	
	@Query(" SELECT rc FROM RentalContract rc WHERE rc.lessee.id = :userId OR rc.ad.owner.id = :userId ")
	public List<RentalContract> findAllByUser(long userId);
	
	@Query("""
			SELECT rc 
			FROM RentalContract rc
			WHERE rc.id = :contractId
			""")
	public RentalContract findByIdForUpdate(long contractId);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT rc 
			FROM RentalContract rc
			WHERE rc.id = :contractId
			""")
	public RentalContract findByIdPessWriteLock(long contractId);
	
	public List<RentalContract> findByAdIdAndContractStatusIn(long adId, List<ContractStatus> statusList);
	
    @Query("""
    		SELECT count(rc) > 0
    		FROM RentalContract rc
    		WHERE rc.ad.id = :adId 
    			AND rc.contractStatus IN (ACCEPTED, ACTIVE)
    		""")
    public boolean hasActiveOrFutureContracts(long adId);
    
    @Modifying
    @Query("UPDATE RentalContract rc " +
    		"SET rc.contractStatus = 'AD_DELETED' " +
    		"WHERE rc.ad.id = :adId")
    public void markToAdDeleted(long adId);


	@Query("SELECT rc FROM RentalContract rc WHERE rc.ad.id = :adId AND rc.contractStatus IN :statuses ")
	List<RentalContract> findActiveContractForAd(
			@Param("adId") long adId,
			@Param("statuses") List<ContractStatus> statuses);
	
	@Query("""
			SELECT rc 
			FROM RentalContract rc 
			WHERE rc.ad.id = :adId AND rc.startDate <= :endDate 
			AND rc.endDate >= :startDate
			""")
	List<RentalContract> findContractsInDateInterval(@Param("adId") long adId, 
			@Param("startDate")LocalDate startDate, 
			@Param("endDate") LocalDate endDate);
}
