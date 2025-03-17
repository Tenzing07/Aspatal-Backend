package com.aspatal.hospitalmanagementbackend.controller;

import com.aspatal.hospitalmanagementbackend.dto.EmployeeDto;
import com.aspatal.hospitalmanagementbackend.entity.Appointment;
import com.aspatal.hospitalmanagementbackend.entity.BookingRequest;
import com.aspatal.hospitalmanagementbackend.entity.Prescription;
import com.aspatal.hospitalmanagementbackend.service.ReceptionistService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receptionist")
@PreAuthorize("hasAuthority('RECEPTIONIST')")
public class  ReceptionistController {
    private final ReceptionistService receptionistService;

    public ReceptionistController(ReceptionistService receptionistService) {
        this.receptionistService = receptionistService;
    }

    @GetMapping("/patients")
    public ResponseEntity<List<EmployeeDto>> getAllPatients() {
        return receptionistService.getAllPatients();
    }

    @PostMapping("/patients")
    public ResponseEntity<Map<String, String>> registerPatient(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String phoneNumber = request.getOrDefault("phoneNumber", null);
        return receptionistService.registerPatient(name, email, phoneNumber);
    }

