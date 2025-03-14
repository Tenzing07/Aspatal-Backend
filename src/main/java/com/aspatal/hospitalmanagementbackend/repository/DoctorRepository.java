package com.aspatal.hospitalmanagementbackend.repository;

import com.aspatal.hospitalmanagementbackend.entity.Doctor;
import com.aspatal.hospitalmanagementbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserEmail(String email);

}