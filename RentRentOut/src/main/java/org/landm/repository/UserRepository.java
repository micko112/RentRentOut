package org.landm.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

import org.landm.entity.User;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    boolean existsByEmail(String email);

    User findByEmail(String email);
    
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

