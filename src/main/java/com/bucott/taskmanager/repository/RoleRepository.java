package com.bucott.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bucott.taskmanager.model.Authority;
import com.bucott.taskmanager.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByAuthority(Authority authority);
}
