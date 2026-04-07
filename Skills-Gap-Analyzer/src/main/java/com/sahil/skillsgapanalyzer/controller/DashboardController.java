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

    @GetMapping("/student/{studentId}/export")
    public ResponseEntity<?> exportStudentDashboard(@PathVariable Long studentId) {
        try {
            // 1. Fetch the exact same data we use for the dashboard
            Object dashboardData = dashboardService.getStudentDashboard(studentId);

            // 2. Set headers to tell the browser this is a file download
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.add(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=student_" + studentId + "_analysis.json");
            headers.add(org.springframework.http.HttpHeaders.CONTENT_TYPE,
                    org.springframework.http.MediaType.APPLICATION_JSON_VALUE);

            // 3. Return the data with the attachment headers
            return org.springframework.http.ResponseEntity.ok()
                    .headers(headers)
                    .body(dashboardData);

        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("Error exporting data: " + e.getMessage());
        }
    }


    // Get the raw history to populate the frontend table
    @GetMapping("/student/{studentId}/history")
    public ResponseEntity<?> getStudentHistory(@PathVariable Long studentId) {
        try {
            return ResponseEntity.ok(dashboardService.getStudentRawHistory(studentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching history: " + e.getMessage());
        }
    }

    // Update a specific row in the history
    @PutMapping("/attempt/{attemptId}")
    public ResponseEntity<?> editAttempt(@PathVariable Long attemptId, @RequestBody com.sahil.skillsgapanalyzer.dto.StudentAttemptDto updatedData) {
        try {
            return ResponseEntity.ok(dashboardService.updateStudentAttempt(attemptId, updatedData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating attempt: " + e.getMessage());
        }
    }

    // Add a brand-new attempt for a student
    @PostMapping("/student/{studentId}/attempt")
    public ResponseEntity<?> addNewAttempt(
            @PathVariable Long studentId,
            @RequestBody com.sahil.skillsgapanalyzer.dto.StudentAttemptDto newData) {
        try {
            return ResponseEntity.ok(dashboardService.addStudentAttempt(studentId, newData));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding new attempt: " + e.getMessage());
        }
    }
}
