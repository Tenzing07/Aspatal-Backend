package com.aspatal.hospitalmanagementbackend.controller;

import com.aspatal.hospitalmanagementbackend.entity.Appointment;
import com.aspatal.hospitalmanagementbackend.entity.Prescription;
import com.aspatal.hospitalmanagementbackend.service.DoctorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasAuthority('DOCTOR')")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return doctorService.getDoctorAppointments(date);
    }

    @PutMapping("/appointments/{id}")
    public ResponseEntity<Map<String, String>> updateAppointmentStatus(
            @PathVariable Long id, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        return doctorService.updateAppointmentStatus(id, status);
    }

    @GetMapping("/patients")
    public ResponseEntity<List<Map<String, Object>>> getAssignedPatients() {
        return doctorService.getAssignedPatients();
    }
    @GetMapping("/patients/{patientId}/visits")
    public ResponseEntity<List<Appointment>> getPatientVisitHistory(@PathVariable Long patientId) {
        return doctorService.getPatientVisitHistory(patientId);
    }
    @GetMapping("/patients/{patientId}/prescriptions")
    public ResponseEntity<List<Prescription>> getPatientPrescriptionHistory(@PathVariable Long patientId) {
        return doctorService.getPatientPrescriptionHistory(patientId);
    }

    @PostMapping("/prescriptions")
    public ResponseEntity<Map<String, String>> createPrescription(
            @RequestBody Map<String, Object> request) {
        Long patientId = Long.valueOf(request.get("patientId").toString());
        String content = request.get("content").toString();
        return doctorService.createPrescription(patientId, content);
    }
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getDoctorProfile() {
        return doctorService.getDoctorProfile();
    }
}