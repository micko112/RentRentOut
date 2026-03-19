package org.landm.repository;

import org.landm.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    @Query("SELECT ps FROM PushSubscription ps WHERE ps.user.id = :userId")
    List<PushSubscription> findAllByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PushSubscription ps WHERE ps.endpoint = :endpoint AND ps.user.id = :userId")
    void deleteByEndpointAndUserId(@Param("endpoint") String endpoint, @Param("userId") Long userId);
}
