package com.aspatal.hospitalmanagementbackend.controller;

import com.aspatal.hospitalmanagementbackend.dto.EmployeeDto;
import com.aspatal.hospitalmanagementbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')") // ✅ Only Admin can access
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // ✅ Add Employee Endpoint
    @PostMapping("/employees")
    public ResponseEntity<Map<String, String>> addEmployee(@RequestBody EmployeeDto employeeDto) {

        return userService.addEmployee(employeeDto);
    }
    // NEW: Get All Employees Endpoint
    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
        return userService.getAllEmployees();
    }


    // ✅ 2. Get employee by ID
    @GetMapping("/employees/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        return userService.getEmployeeById(id);
    }

    // ✅ 3. Get employees by role
    @GetMapping("/employees/role/{role}")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByRole(@PathVariable String role) {
        return userService.getEmployeesByRole(role);
    }

    // ✅ 4. Update employee details
    @PutMapping("/employees/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDto employeeDto) {
        return userService.updateEmployee(id, employeeDto);
    }

    