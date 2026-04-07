package com.sahil.aianalyzer.controller;


import com.sahil.aianalyzer.dto.AiPredictionResponse;
import com.sahil.aianalyzer.service.AiPredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
@CrossOrigin(origins = "*")
public class PredictionController {

    @Autowired
    private AiPredictionService aiPredictionService;

    @PostMapping("/student/{studentId}")
    public ResponseEntity<?> getPredictionsForStudent(
            @PathVariable Long studentId,
            @RequestBody Map<String, List<Integer>> payload) {

        try {
            // 1. Extract the data sent by the frontend
            List<Integer> skills = payload.get("skills");
            List<Integer> corrects = payload.get("corrects");

            // Optional: In a real app, you might save these answers to a Database here first!

            // 2. Call our AI Service to get the predictions
            AiPredictionResponse response = aiPredictionService.getStudentPredictions(skills, corrects);

            if (response == null || "error".equals(response.getApiStatus())) {
                return ResponseEntity.internalServerError().body("Failed to get predictions from AI");
            }

            // 3. Send the clean data back to the frontend
            return ResponseEntity.ok(response.getSkillsData());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request: " + e.getMessage());
        }
    }
}
