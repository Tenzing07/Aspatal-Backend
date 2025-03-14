package com.aspatal.hospitalmanagementbackend.service;

import com.aspatal.hospitalmanagementbackend.entity.*;
import com.aspatal.hospitalmanagementbackend.repository.AppointmentRepository;
import com.aspatal.hospitalmanagementbackend.repository.DoctorRepository;
import com.aspatal.hospitalmanagementbackend.repository.PrescriptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PrescriptionRepository prescriptionRepository;

    public DoctorService(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository, PrescriptionRepository prescriptionRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.prescriptionRepository= prescriptionRepository;
    }

    private Long getCurrentDoctorId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return doctorRepository.findByUserEmail(email)
                .map(Doctor::getUserId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    public ResponseEntity<List<Appointment>> getDoctorAppointments(LocalDate date) {
        Long doctorId = getCurrentDoctorId();
        List<Appointment> appointments = date == null
                ? appointmentRepository.findByDoctorUserId(doctorId)
                : appointmentRepository.findByDoctorUserIdAndAppointmentDate(doctorId, date);
        return ResponseEntity.ok(appointments);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> updateAppointmentStatus(Long id, String status) {
        Long doctorId = getCurrentDoctorId();
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);

        if (appointmentOpt.isEmpty() || !appointmentOpt.get().getDoctor().getUserId().equals(doctorId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Appointment not found or not assigned to you"));
        }

        Appointment appointment = appointmentOpt.get();
        try {
            appointment.setStatus(AppointmentStatus.valueOf(status.toUpperCase()));
            appointmentRepository.save(appointment);
            return ResponseEntity.ok(Map.of("message", "Appointment status updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid status value"));
        }
    }

    public ResponseEntity<List<Map<String, Object>>> getAssignedPatients() {
        Long doctorId = getCurrentDoctorId();
        List<Appointment> appointments = appointmentRepository.findByDoctorUserId(doctorId);
        List<Map<String, Object>> patients = appointments.stream()
                .map(appt -> appt.getPatient())
                .distinct()
                .map(patient -> {
                    Map<String, Object> patientMap = new HashMap<>();
                    patientMap.put("id", patient.getUserId());
                    patientMap.put("name", patient.getName());
                    patientMap.put("email", patient.getUser().getEmail());
                    patientMap.put("phoneNumber", patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "N/A");
                    return patientMap;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(patients);
    }

    public ResponseEntity<List<Appointment>> getPatientVisitHistory(Long patientId) {
        Long doctorId = getCurrentDoctorId();
        List<Appointment> visits = appointmentRepository.findByPatientUserId(patientId);
        return ResponseEntity.ok(visits);
    }

    public ResponseEntity<List<Prescription>> getPatientPrescriptionHistory(Long patientId) {
        Long doctorId = getCurrentDoctorId();
        List<Prescription> prescriptions = prescriptionRepository.findByPatientUserId(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> createPrescription(Long patientId, String content) {
        Long doctorId = getCurrentDoctorId();
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        Optional<Patient> patientOpt = Optional.ofNullable(appointmentRepository.findByDoctorUserId(doctorId)
                .stream()
                .map(Appointment::getPatient)
                .filter(p -> p.getUserId().equals(patientId))
                .findFirst()
                .orElse(null));

        if (doctorOpt.isEmpty() || patientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Doctor or patient not found"));
        }

        Prescription prescription = new Prescription();
        prescription.setDoctor(doctorOpt.get());
        prescription.setPatient(patientOpt.get());
        prescription.setContent(content);
        prescriptionRepository.save(prescription);

        // TODO: Notify receptionist (e.g., via email or internal messaging system)
        return ResponseEntity.ok(Map.of("message", "Prescription created successfully"));
    }

    public ResponseEntity<Map<String, Object>> getDoctorProfile() {
        Long doctorId = getCurrentDoctorId();
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Doctor not found"));
        }
        Doctor doctor = doctorOpt.get();
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", doctor.getUserId());
        profile.put("name", doctor.getName());
        profile.put("email", doctor.getUser().getEmail());
        profile.put("speciality", doctor.getSpeciality());
        profile.put("opdFee", doctor.getOpdFee()); // Assuming opdFee is a field in Doctor entity
        return ResponseEntity.ok(profile);
    }
}