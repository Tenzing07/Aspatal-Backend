package com.aspatal.hospitalmanagementbackend.service;

import com.aspatal.hospitalmanagementbackend.entity.*;
import com.aspatal.hospitalmanagementbackend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService {
    private final PatientRepository patientRepository;
    private final BookingRequestRepository bookingRequestsRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final DoctorRepository doctorRepository;

    public PatientService(
            PatientRepository patientRepository,
            BookingRequestRepository bookingRequestsRepository,
            AppointmentRepository appointmentRepository,
            PrescriptionRepository prescriptionRepository,
            DoctorRepository doctorRepository
    ) {
        this.patientRepository = patientRepository;
        this.bookingRequestsRepository = bookingRequestsRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.doctorRepository=doctorRepository;
    }

    private Long getCurrentPatientId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return patientRepository.findByUserEmail(email)
                .map(Patient::getUserId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    public ResponseEntity<Map<String, String>> requestAppointment(Long doctorId, LocalDate date, LocalTime time) {
        Long patientId = getCurrentPatientId();
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);

        if (patientOpt.isEmpty() || doctorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Patient or Doctor not found"));
        }

        BookingRequest request = new BookingRequest();
        request.setPatient(patientOpt.get());
        request.setDoctor(doctorOpt.get());
        request.setRequestDate(date);
        request.setRequestTime(time);
        bookingRequestsRepository.save(request);

        return ResponseEntity.ok(Map.of("message", "Appointment request submitted successfully"));
    }

    public ResponseEntity<List<BookingRequest>> getBookingRequests() {
        Long patientId = getCurrentPatientId();
        List<BookingRequest> requests = bookingRequestsRepository.findByPatientUserId(patientId);
        return ResponseEntity.ok(requests);
    }

    public ResponseEntity<List<Prescription>> getPrescriptions() {
        Long patientId = getCurrentPatientId();
        List<Prescription> prescriptions = prescriptionRepository.findByPatientUserId(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    public ResponseEntity<List<Appointment>> getMedicalHistory() {
        Long patientId = getCurrentPatientId();
        List<Appointment> visits = appointmentRepository.findByPatientUserId(patientId);
        return ResponseEntity.ok(visits);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> deletePatientRecord() {
        Long patientId = getCurrentPatientId();
        patientRepository.deleteById(patientId);
        return ResponseEntity.ok(Map.of("message", "Patient record deleted successfully"));
    }
    public ResponseEntity<List<Map<String, Object>>> getAvailableDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Map<String, Object>> doctorList = doctors.stream().map(doctor -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doctor.getUserId());
            map.put("name", doctor.getName());
            map.put("speciality", doctor.getSpeciality());
            map.put("opdFee", doctor.getOpdFee());

            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(doctorList);
    }
    public ResponseEntity<Map<String, Object>> getPatientProfile() {
        Long doctorId = getCurrentPatientId();
        Optional<Patient> patientOpt = patientRepository.findById(doctorId);
        if (patientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Doctor not found"));
        }
        Patient patient = patientOpt.get();
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", patient.getUserId());
        profile.put("name", patient.getName());
        profile.put("email", patient.getUser().getEmail());
        return ResponseEntity.ok(profile);
    }
}