package org.landm.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import org.landm.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    @Query("""
            SELECT u FROM User u
            WHERE :search IS NULL OR :search = ''
               OR LOWER(CONCAT(u.firstname, ' ', u.lastname)) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY u.id DESC
            """)
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    boolean existsByEmail(String email);

    User findByEmail(String email);

    User findByFacebookId(String facebookId);

    User findByAppleId(String appleId);
    
    @Lock(LockModeType.OPTIMISTIC)
    @Query("""
    		SELECT u
    		FROM User u
    		WHERE u.id = :userId
    		""")
    public Optional<User> findByIdForCheck(Long userId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    		SELECT u
    		FROM User u
    		WHERE u.id = :userId
    		""")
    public Optional<User> findByIdForUpdate(Long userId);
    
}

