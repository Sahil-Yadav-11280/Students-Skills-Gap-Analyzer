package com.sahil.skillsgapanalyzer.controller;


import com.sahil.skillsgapanalyzer.dto.DashboardDataResponse;
import com.sahil.skillsgapanalyzer.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.util.Elements;

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
}
