package com.aspatal.hospitalmanagementbackend.repository;

import com.aspatal.hospitalmanagementbackend.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByAppointmentDate(LocalDate date);

//    List<Appointment> findAppointments();
    List<Appointment> findByDoctorUserId(Long doctorId);
    List<Appointment> findByPatientUserId(Long patientId);

    List<Appointment> findByDoctorUserIdAndAppointmentDate(Long doctorId, LocalDate date);
}