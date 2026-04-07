package com.sahil.skillsgapanalyzer.integration;

import com.sahil.skillsgapanalyzer.dto.MLPredictionResponse;
import com.sahil.skillsgapanalyzer.dto.MlPredictionRequest;
import com.sahil.skillsgapanalyzer.dto.SkillPredictionDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class MlPredictionClient {

    private final RestClient restClient;

    public MlPredictionClient(RestClient restClient){
        this.restClient = restClient;
    }

    public MLPredictionResponse getPrediction(MlPredictionRequest requestPayload){
        try{
            return restClient.post()
                    .uri("http://localhost:5000/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(MLPredictionResponse.class);
        }
        catch (Exception e){
            System.err.println("Error calling Flask API: " + e.getMessage());

            MLPredictionResponse fallback = new MLPredictionResponse();
            fallback.api_status = "error";
            fallback.skills_data = new HashMap<>();   // ✅ FIX

            return fallback;
        }
    }
}
