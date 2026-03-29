package com.sahil.skillsgapanalyzer.integration;

import com.sahil.skillsgapanalyzer.dto.MLPredictionResponse;
import com.sahil.skillsgapanalyzer.dto.MlPredictionRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MlPredictionClient {

    private final RestClient restClient;

    public MlPredictionClient(RestClient restClient){
        this.restClient = restClient;
    }

    public MLPredictionResponse getPrediction(MlPredictionRequest requestPayload){
        try{
            return restClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(MLPredictionResponse.class);
        } catch (Exception e){
            System.err.println("Error calling Flask API: "+e.getMessage());

            MLPredictionResponse fallback = new MLPredictionResponse();
            fallback.status = "error";
            fallback.predicted_probability = 0.0;
            fallback.mastery_score_percentage = 0.0;
            fallback.model_confidence = 0.0;

            return fallback;
        }
    }
}
