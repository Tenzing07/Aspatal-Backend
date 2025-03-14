package com.aspatal.hospitalmanagementbackend.repository;

import com.aspatal.hospitalmanagementbackend.entity.Doctor;
import com.aspatal.hospitalmanagementbackend.entity.Receptionist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReceptionistRepository extends JpaRepository<Receptionist, Long> {
    Optional<Receptionist> findByUserEmail(String email);

}