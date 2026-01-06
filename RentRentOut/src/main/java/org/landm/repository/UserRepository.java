package org.landm.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.landm.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    boolean existsByEmail(String email);

    User findByEmail(String email);
}

