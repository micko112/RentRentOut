package org.landm.repository;

import java.time.LocalDate;
import java.util.List;

import org.landm.entity.RentalContract;
import org.landm.entity.Enums.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	public List<RentalContract> findAllByUser(Long userId);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT rc 
			FROM RentalContract rc
			WHERE rc.id = :contractId
			""")
	public RentalContract findByIdPessWriteLock(Long contractId);
	
	public List<RentalContract> findByAdIdAndContractStatusIn(Long adId, List<ContractStatus> statusList);
	
    @Query("""
    		SELECT count(rc) > 0
    		FROM RentalContract rc
    		WHERE rc.ad.id = :adId 
    			AND rc.contractStatus IN ('ACCEPTED', 'ACTIVE')
    		""")
    public boolean hasActiveOrFutureContracts(Long adId);
    
    @Modifying
    @Query("UPDATE RentalContract rc " +
    		"SET rc.contractStatus = 'AD_DELETED' " +
    		"WHERE rc.ad.id = :adId")
    public void markToAdDeleted(Long adId);


	@Query("SELECT rc FROM RentalContract rc WHERE rc.ad.id = :adId AND rc.contractStatus IN :statuses ")
	List<RentalContract> findActiveContractForAd(
			@Param("adId") Long adId,
			@Param("statuses") List<ContractStatus> statuses);

	@Query("""
        SELECT rc
        FROM RentalContract rc
        WHERE rc.ad.id = :adId
          AND rc.contractStatus IN ('ACCEPTED', 'ACTIVE')
          AND rc.startDate <= :endDate
          AND rc.endDate >= :startDate
        """)
	List<RentalContract> findContractsInDateInterval(@Param("adId") Long adId,
			@Param("startDate")LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	@Query("""
        SELECT rc
        FROM RentalContract rc
        WHERE rc.ad.id = :adId
          AND rc.contractStatus IN ('ACCEPTED', 'ACTIVE', 'BLOCKED_BY_OWNER')
          AND rc.startDate <= :endDate
          AND rc.endDate >= :startDate
        """)
	List<RentalContract> findContractsInDateIntervalIncludingBlocked(@Param("adId") Long adId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate);

	long countByContractStatusIn(List<ContractStatus> statuses);

	@Query("SELECT rc FROM RentalContract rc WHERE rc.contractStatus IN :statuses AND rc.startDate < :today")
	List<RentalContract> findByStatusInAndStartDateBefore(
			@Param("statuses") List<ContractStatus> statuses,
			@Param("today") LocalDate today);
}
