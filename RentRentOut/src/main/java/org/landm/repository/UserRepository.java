package org.landm.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.landm.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String>{

    boolean existsByEmail(String email);
}

