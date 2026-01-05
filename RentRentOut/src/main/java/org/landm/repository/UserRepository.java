package org.landm.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.landm.entity.User;

public interface UserRepository extends JpaRepository<User, String>{
}

