package org.landm.repository;

import org.landm.entity.AdTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdTemplateRepository extends JpaRepository<AdTemplate, Long> {
    List<AdTemplate> findByUserIdOrderByUpdatedAtDesc(Long userId);
    long countByUserId(Long userId);
}
