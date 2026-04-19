package org.landm.repository;

import org.landm.entity.IdentityVerification;
import org.landm.entity.Enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdentityVerificationRepository extends JpaRepository<IdentityVerification, Long> {
    Optional<IdentityVerification> findByUserId(Long userId);
    Page<IdentityVerification> findAllByStatusOrderBySubmittedAtAsc(VerificationStatus status, Pageable pageable);
    Page<IdentityVerification> findAllByOrderBySubmittedAtDesc(Pageable pageable);
    long countByStatus(VerificationStatus status);
}
