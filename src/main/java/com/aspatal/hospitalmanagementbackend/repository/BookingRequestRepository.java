package com.aspatal.hospitalmanagementbackend.repository;

import com.aspatal.hospitalmanagementbackend.entity.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    List<BookingRequest> findByPatientUserId(Long patientId);
}