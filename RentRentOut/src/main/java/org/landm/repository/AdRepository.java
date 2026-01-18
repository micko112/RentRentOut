package org.landm.repository;

import org.landm.entity.Ad;

import org.landm.entity.Enums.AdStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {
    Page<Ad> findAllByAdStatus(AdStatus adStatus, Pageable pageable);
}
