package org.landm.repository;

import org.landm.entity.Ad;

import org.landm.entity.Enums.AdStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> , JpaSpecificationExecutor<Ad> {
    Page<Ad> findAllByAdStatus(AdStatus adStatus, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    		SELECT a
    		FROM Ad a
    		WHERE a.id = :adId
    		""")
    Optional<Ad> findByIdForUpdate(Long adId);

    Page<Ad> findAllByOwnerId(Long userId, Pageable pageable);

    Page<Ad> findAllByOwnerIdAndAdStatus(Long userId, AdStatus adStatus, Pageable pageable);

    @Query("""
            SELECT a FROM Ad a
            WHERE a.adStatus = org.landm.entity.Enums.AdStatus.ACTIVE
              AND a.expiresAt BETWEEN :from AND :to
            """)
    List<Ad> findAdsExpiringBetween(LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT a FROM Ad a
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL
                   OR a.adStatus = :status)
            ORDER BY a.id DESC
            """)
    Page<Ad> searchAds(@Param("search") String search,
                       @Param("status") AdStatus status,
                       Pageable pageable);

    @Query("SELECT a.id FROM Ad a WHERE a.adStatus = org.landm.entity.Enums.AdStatus.ACTIVE ORDER BY a.id DESC")
    List<Long> findAllActiveIds();

    @Query("SELECT a.id, a.createdAt FROM Ad a WHERE a.adStatus = org.landm.entity.Enums.AdStatus.ACTIVE ORDER BY a.id DESC")
    List<Object[]> findAllActiveIdsWithDate();
}
