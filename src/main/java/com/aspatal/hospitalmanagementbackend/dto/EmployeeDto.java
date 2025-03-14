package com.aspatal.hospitalmanagementbackend.dto;

import com.aspatal.hospitalmanagementbackend.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeDto {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String phoneNumber;

    @Email
    @NotBlank
    private String email;

    // Doctor-specific fields
    private String nmcNumber;    // Added NMC number for doctors
    private String speciality;

//    public EmployeeDto(String name, String email,  Role role) {
//        this.name = name;
//        this.email = email;
//        this.role = role;
//    }
    private double opdFee;

    public double getOpdFee() {
        return opdFee;
    }

    public void setOpdFee(double opdFee) {
        this.opdFee = opdFee;
    }

    public EmployeeDto(Long id, String name, String email, String phoneNumber, Role role,
                       String nmcNumber, String speciality , double opdFee) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.nmcNumber = nmcNumber;
        this.speciality = speciality;
        this.opdFee=opdFee;
    }
    public EmployeeDto() {
    }

    @NotBlank
    private String password;

    private Role role; // Admin sets the role while adding employees

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNmcNumber() {
        return nmcNumber;
    }

    public void setNmcNumber(String nmcNumber) {
        this.nmcNumber = nmcNumber;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
