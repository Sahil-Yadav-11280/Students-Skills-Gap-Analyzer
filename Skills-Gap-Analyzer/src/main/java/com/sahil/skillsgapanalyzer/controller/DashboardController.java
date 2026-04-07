package com.sahil.skillsgapanalyzer.controller;


import com.sahil.skillsgapanalyzer.dto.AddStudentRequest;
import com.sahil.skillsgapanalyzer.dto.DashboardDataResponse;
import com.sahil.skillsgapanalyzer.dto.StudentSummaryDto;
import com.sahil.skillsgapanalyzer.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*") // Allows this api to be called with CORS errors
public class DashboardController {

    private final DashboardService dashboardService;
    public DashboardController(DashboardService dashboardService){
        this.dashboardService = dashboardService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<DashboardDataResponse> getDashboardForStudent(@PathVariable Long studentId){
        try{
            DashboardDataResponse response = dashboardService.getStudentDashboard(studentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e){
            return ResponseEntity.notFound().build();
        }

    }

//    Fetch the list of all students
    @GetMapping("/students")
    public ResponseEntity<List<StudentSummaryDto>> getAllStudents(){
        return ResponseEntity.ok(dashboardService.getAllStudentsSummary());
    }

//    add a new student
    @PostMapping("/students")
    public ResponseEntity<?> addNewStudent(@RequestBody java.util.Map<String, String> payload) {
        try {
            String name = payload.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Student name is required.");
            }
            // Calls the existing method in your DashboardService
            return ResponseEntity.ok(dashboardService.addStudent(name));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding student: " + e.getMessage());
        }
    }
}
