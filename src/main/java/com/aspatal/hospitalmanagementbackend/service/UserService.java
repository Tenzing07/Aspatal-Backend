package com.aspatal.hospitalmanagementbackend.service;

import com.aspatal.hospitalmanagementbackend.dto.LoginDto;
import com.aspatal.hospitalmanagementbackend.dto.EmployeeDto;
import com.aspatal.hospitalmanagementbackend.dto.RegisterDto;
import com.aspatal.hospitalmanagementbackend.entity.Role;
import com.aspatal.hospitalmanagementbackend.entity.User;
import com.aspatal.hospitalmanagementbackend.entity.Doctor;
import com.aspatal.hospitalmanagementbackend.entity.Patient;
import com.aspatal.hospitalmanagementbackend.entity.Receptionist;
import com.aspatal.hospitalmanagementbackend.repository.UserRepository;
import com.aspatal.hospitalmanagementbackend.repository.DoctorRepository;
import com.aspatal.hospitalmanagementbackend.repository.PatientRepository;
import com.aspatal.hospitalmanagementbackend.repository.ReceptionistRepository;
import com.aspatal.hospitalmanagementbackend.util.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ReceptionistRepository receptionistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 8;

    public UserService(UserRepository userRepository, DoctorRepository doctorRepository, PatientRepository patientRepository,
                       ReceptionistRepository receptionistRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.receptionistRepository = receptionistRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public ResponseEntity<String> registerUser(RegisterDto request) throws MessagingException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already in use!");
        }

        // Create User entity
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() == Role.ADMIN || request.getRole() == Role.DOCTOR || request.getRole() == Role.RECEPTIONIST
                ? request.getRole() : Role.PATIENT);
        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);
        user.setEnabled(false);

        // Handle based on role
        if (user.getRole() == Role.PATIENT) {
            Patient patient = new Patient();
            patient.setName(request.getName());
            patient.setUser(user);
            patientRepository.save(patient); // Cascades to User
        } else {
            userRepository.save(user); // For non-patients, save User directly
        }

        sendVerificationEmail(request.getEmail(), verificationToken);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully! Please verify your email.");
    }

    private void sendVerificationEmail(String email, String token) throws MessagingException {
        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + token;
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("Verify Your Email - Aspatal");
        helper.setText("Click the link to verify your account: " + verificationLink, true);
        mailSender.send(message);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> addEmployee(EmployeeDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return buildErrorResponse("Email already in use!", HttpStatus.BAD_REQUEST);
        }

        String generatedPassword = generateRandomPassword();
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setRole(request.getRole());
        user.setEnabled(true);

        if (request.getRole() == Role.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setName(request.getName());
            doctor.setPhoneNumber(request.getPhoneNumber());
            doctor.setNmcNumber(request.getNmcNumber());
            doctor.setSpeciality(request.getSpeciality());
            doctor.setOpdFee(request.getOpdFee());
            doctor.setUser(user);
            doctorRepository.save(doctor); // Cascades to User
        } else if (request.getRole() == Role.RECEPTIONIST) {
            Receptionist receptionist = new Receptionist();
            receptionist.setName(request.getName());
            receptionist.setPhoneNumber(request.getPhoneNumber());
            receptionist.setUser(user);
            receptionistRepository.save(receptionist); // Cascades to User
        } else { // ADMIN
            Receptionist admin = new Receptionist();
            admin.setName(request.getName());
            admin.setPhoneNumber(request.getPhoneNumber());
            admin.setUser(user);
            receptionistRepository.save(admin); // Cascades to User
        }

        try {
            sendEmployeeWelcomeEmail(request.getEmail(), generatedPassword);
        } catch (MessagingException e) {
            return buildErrorResponse("Employee added but email failed!", HttpStatus.BAD_REQUEST);
        }
        return buildSuccessResponse("Employee added successfully!");
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }

    private void sendEmployeeWelcomeEmail(String email, String rawPassword) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("Welcome to Aspatal");
        helper.setText("Hello,\n\nYour account has been created.\n\nEmail: " + email + "\nPassword: " + rawPassword + "\n\nPlease log in and change your password.\n\nAspatal Team");
        mailSender.send(message);
    }

    public String verifyUser(String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);
        if (userOptional.isEmpty()) return "Invalid verification token!";
        User user = userOptional.get();
        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        return "Account verified successfully!";
    }

    public ResponseEntity<Map<String, String>> login(LoginDto request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid email or password!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        User user = userOptional.get();
        if (!user.isEnabled()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Please verify your email before logging in!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().toString());
        Map<String, String> successResponse = new HashMap<>();
        successResponse.put("token", token);
        successResponse.put("role", user.getRole().toString());
        return ResponseEntity.ok(successResponse);
    }

    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        List<EmployeeDto> doctors = doctorRepository.findAll().stream()
                .map(this::convertDoctorToDto)
                .collect(Collectors.toList());
        List<EmployeeDto> receptionists = receptionistRepository.findAll().stream()
                .map(this::convertReceptionistToDto)
                .collect(Collectors.toList());
        doctors.addAll(receptionists);
        return ResponseEntity.ok(doctors);
    }

    public ResponseEntity<?> getEmployeeById(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isPresent()) {
            return ResponseEntity.ok(convertDoctorToDto(doctor.get()));
        }
        Optional<Receptionist> receptionist = receptionistRepository.findById(id);
        if (receptionist.isPresent()) {
            return ResponseEntity.ok(convertReceptionistToDto(receptionist.get()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Employee not found"));
    }

    public ResponseEntity<List<EmployeeDto>> getEmployeesByRole(String role) {
        try {
            Role employeeRole = Role.valueOf(role.toUpperCase());
            if (employeeRole == Role.PATIENT) {
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }
            if (employeeRole == Role.DOCTOR) {
                return ResponseEntity.ok(doctorRepository.findAll().stream()
                        .map(this::convertDoctorToDto)
                        .collect(Collectors.toList()));
            } else if (employeeRole == Role.RECEPTIONIST || employeeRole == Role.ADMIN) {
                return ResponseEntity.ok(receptionistRepository.findAll().stream()
                        .filter(r -> r.getUser().getRole() == employeeRole)
                        .map(this::convertReceptionistToDto)
                        .collect(Collectors.toList()));
            }
            return ResponseEntity.ok(Collections.emptyList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @Transactional
    public ResponseEntity<?> updateEmployee(Long id, EmployeeDto request) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(id);
        if (doctorOpt.isPresent()) {
            Doctor doctor = doctorOpt.get();
            if (!doctor.getUser().getEmail().equals(request.getEmail()) &&
                    userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email already in use"));
            }
            doctor.getUser().setEmail(request.getEmail());
            doctor.setName(request.getName());
            doctor.setPhoneNumber(request.getPhoneNumber());
            doctor.getUser().setRole(request.getRole());
            doctor.setNmcNumber(request.getRole() == Role.DOCTOR ? request.getNmcNumber() : null);
            doctor.setSpeciality(request.getRole() == Role.DOCTOR ? request.getSpeciality() : null);
            doctor.setOpdFee(request.getRole() == Role.DOCTOR ? request.getOpdFee() : 0);

            doctorRepository.save(doctor); // Cascades to User
            return ResponseEntity.ok(Map.of("message", "Employee updated successfully!"));
        }

        Optional<Receptionist> receptionistOpt = receptionistRepository.findById(id);
        if (receptionistOpt.isPresent()) {
            Receptionist receptionist = receptionistOpt.get();
            if (!receptionist.getUser().getEmail().equals(request.getEmail()) &&
                    userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Email already in use"));
            }
            receptionist.getUser().setEmail(request.getEmail());
            receptionist.setName(request.getName());
            receptionist.setPhoneNumber(request.getPhoneNumber());
            receptionist.getUser().setRole(request.getRole());
            receptionistRepository.save(receptionist); // Cascades to User
            return ResponseEntity.ok(Map.of("message", "Employee updated successfully!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Employee not found"));
    }

    @Transactional
    public ResponseEntity<?> deleteEmployee(Long id) {
        Optional<Doctor> doctor = doctorRepository.findById(id);
        if (doctor.isPresent()) {
            if (doctor.get().getUser().getRole() == Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Cannot delete admin user"));
            }
            doctorRepository.delete(doctor.get()); // Cascades to User
            return ResponseEntity.ok(Map.of("message", "Employee deleted successfully!"));
        }
        Optional<Receptionist> receptionist = receptionistRepository.findById(id);
        if (receptionist.isPresent()) {
            if (receptionist.get().getUser().getRole() == Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Cannot delete admin user"));
            }
            receptionistRepository.delete(receptionist.get()); // Cascades to User
            return ResponseEntity.ok(Map.of("message", "Employee deleted successfully!"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Employee not found"));
    }

    public ResponseEntity<List<EmployeeDto>> getAllPatients() {
        List<EmployeeDto> patients = patientRepository.findAll().stream()
                .map(this::convertPatientToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(patients);
    }

    @Transactional
    public ResponseEntity<?> deletePatient(Long id) {
        Optional<Patient> patient = patientRepository.findById(id);
        if (patient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Patient not found"));
        }
        patientRepository.delete(patient.get()); // Cascades to User
        return ResponseEntity.ok(Map.of("message", "Patient deleted successfully!"));
    }

    private EmployeeDto convertDoctorToDto(Doctor doctor) {
        return new EmployeeDto(
                doctor.getUserId(),
                doctor.getName(),
                doctor.getUser().getEmail(),
                doctor.getPhoneNumber(),
                doctor.getUser().getRole(),
                doctor.getNmcNumber(),
                doctor.getSpeciality(),
                doctor.getOpdFee()
        );
    }

    private EmployeeDto convertReceptionistToDto(Receptionist receptionist) {
        return new EmployeeDto(
                receptionist.getUserId(),
                receptionist.getName(),
                receptionist.getUser().getEmail(),
                receptionist.getPhoneNumber(),
                receptionist.getUser().getRole(),
                null,
                null,
                0
        );
    }

    private EmployeeDto convertPatientToDto(Patient patient) {
        return new EmployeeDto(
                patient.getUserId(),
                patient.getName(),
                patient.getUser().getEmail(),
                patient.getPhoneNumber(),
                patient.getUser().getRole(),
                null,
                null,
                0
        );
    }

    private ResponseEntity<Map<String, String>> buildSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("status", "success");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        response.put("status", "error");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.status(status).body(response);
    }
}