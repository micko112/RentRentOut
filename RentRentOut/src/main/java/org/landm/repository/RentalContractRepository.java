package org.landm.repository;

import org.landm.entity.RentalContract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {
}
