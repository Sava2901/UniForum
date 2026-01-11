package com.forum.repository;

import com.forum.model.User;
import com.forum.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByVerifiedFalse(); // For admin queue
    List<User> findByRole(Role role);
}
