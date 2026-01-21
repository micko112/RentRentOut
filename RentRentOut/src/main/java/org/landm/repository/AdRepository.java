package org.landm.repository;

import org.landm.entity.Ad;

import org.landm.entity.Enums.AdStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> , JpaSpecificationExecutor<Ad> {
    Page<Ad> findAllByAdStatus(AdStatus adStatus, Pageable pageable);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    		SELECT a 
    		FROM Ad a
    		WHERE a.id = :adId
    		""")
    public Ad findByIdForUpdate(long adId);
    
}
