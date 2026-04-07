package com.sahil.aianalyzer.service;

import com.sahil.aianalyzer.dto.AiPredictionRequest;
import com.sahil.aianalyzer.dto.AiPredictionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Service
public class AiPredictionService {

    // This is the URL of your Flask app
    private final String FLASK_API_URL = "http://127.0.0.1:5000/predict";
    private final RestTemplate restTemplate;

    public AiPredictionService() {
        this.restTemplate = new RestTemplate();
    }

    public AiPredictionResponse getStudentPredictions(List<Integer> skillsHistory, List<Integer> correctsHistory) {
        // 1. Prepare the request headers (tell Flask we are sending JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. Package the data into our Request DTO
        AiPredictionRequest requestPayload = new AiPredictionRequest(skillsHistory, correctsHistory);
        HttpEntity<AiPredictionRequest> request = new HttpEntity<>(requestPayload, headers);

        try {
            // 3. Send the POST request to Flask and wait for the response
            ResponseEntity<AiPredictionResponse> response = restTemplate.postForEntity(
                    FLASK_API_URL,
                    request,
                    AiPredictionResponse.class
            );

            // 4. Return the beautifully formatted Java object!
            return response.getBody();

        } catch (Exception e) {
            System.err.println("Error connecting to Flask AI: " + e.getMessage());
            return null;
        }
    }
}