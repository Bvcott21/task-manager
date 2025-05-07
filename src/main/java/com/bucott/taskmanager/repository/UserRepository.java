package com.bucott.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bucott.taskmanager.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
