package com.aspatal.hospitalmanagementbackend.service;

import com.aspatal.hospitalmanagementbackend.dto.EmployeeDto;
import com.aspatal.hospitalmanagementbackend.entity.*;
import com.aspatal.hospitalmanagementbackend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReceptionistService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final PrescriptionRepository prescriptionRepository;
    private  final ReceptionistRepository receptionistRepository;
    private final BookingRequestRepository bookingRequestRepository;

    public ReceptionistService(UserRepository userRepository, PatientRepository patientRepository,
                               DoctorRepository doctorRepository, AppointmentRepository appointmentRepository,
                               PasswordEncoder passwordEncoder, PrescriptionRepository prescriptionRepository, ReceptionistRepository receptionistRepository, BookingRequestRepository bookingRequestRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.prescriptionRepository=prescriptionRepository;
        this.receptionistRepository=receptionistRepository;
        this.bookingRequestRepository=bookingRequestRepository;
    }
    private Long getCurrentReceptionistId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return receptionistRepository.findByUserEmail(email)
                .map(Receptionist::getUserId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    @Transactional
    public ResponseEntity<Map<String, String>> registerPatient(String name, String email, String phoneNumber) {
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Email already in use!"));
        }

        User user = new User();
        user.setEmail(email);
        String generatedPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setRole(Role.PATIENT);
        user.setEnabled(true); // No verification needed for receptionist-added patients

        Patient patient = new Patient();
        patient.setName(name);
        patient.setPhoneNumber(phoneNumber);
        patient.setUser(user);

        patientRepository.save(patient); // Cascades to User

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Patient registered successfully!", "password", generatedPassword));
    }

  