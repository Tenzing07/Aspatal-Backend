package com.aspatal.hospitalmanagementbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "doctors")
public class Doctor {
    @Id
    private Long userId;
    private String name;
    private String phoneNumber;
    private String nmcNumber;
    private String speciality;

    @OneToOne(cascade = CascadeType.ALL)    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    private double opdFee;

    public double getOpdFee() {
        return this.opdFee;
    }

    public void setOpdFee(double opdFee) {
        this.opdFee = opdFee;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}