package com.aspatal.hospitalmanagementbackend.controller;

import com.aspatal.hospitalmanagementbackend.entity.Appointment;
import com.aspatal.hospitalmanagementbackend.entity.BookingRequest;
import com.aspatal.hospitalmanagementbackend.entity.Prescription;
import com.aspatal.hospitalmanagementbackend.service.PatientService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@PreAuthorize("hasAuthority('PATIENT')")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/booking-requests")
    public ResponseEntity<Map<String, String>> requestAppointment(
            @RequestBody Map<String, Object> request
    ) {
        Long doctorId = Long.valueOf(request.get("doctorId").toString());
        LocalDate date = LocalDate.parse(request.get("date").toString());
        LocalTime time = LocalTime.parse(request.get("time").toString());
        return patientService.requestAppointment(doctorId, date, time);
    }

    @GetMapping("/booking-requests")
    public ResponseEntity<List<BookingRequest>> getBookingRequests() {
        return patientService.getBookingRequests();
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<List<Prescription>> getPrescriptions() {
        return patientService.getPrescriptions();
    }

    @GetMapping("/medical-history")
    public ResponseEntity<List<Appointment>> getMedicalHistory() {
        return patientService.getMedicalHistory();
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Map<String, String>> deletePatientRecord() {
        return patientService.deletePatientRecord();
    }

