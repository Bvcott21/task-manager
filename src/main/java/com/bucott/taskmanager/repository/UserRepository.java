package com.bucott.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bucott.taskmanager.model.User;

public interface UserRepository extends JpaRepository<User, Long>{

}
