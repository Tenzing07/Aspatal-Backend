package com.aspatal.hospitalmanagementbackend.repository;

import com.aspatal.hospitalmanagementbackend.entity.Role;
import com.aspatal.hospitalmanagementbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String token);
    List<User> findByRole(Role role);
}
