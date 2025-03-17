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

    @PostMapping("/appointments")
    public ResponseEntity<Map<String, String>> scheduleAppointment(
            @RequestBody Map<String, String> request) {
        Long patientId = Long.valueOf(request.get("patientId"));
        Long doctorId = Long.valueOf(request.get("doctorId"));
        LocalDate date = LocalDate.parse(request.get("date"));
        LocalTime time = LocalTime.parse(request.get("time"));
        return receptionistService.assignPatientToDoctor(patientId, doctorId, date, time);
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAppointmentsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return receptionistService.getAppointmentsByDate(date);
    }
    @GetMapping("/allAppointments")
    public ResponseEntity<List<Appointment>> getAppointments() {
        return receptionistService.getAppointments();
    }
