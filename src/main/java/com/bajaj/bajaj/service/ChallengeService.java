package com.bajaj.bajaj.service;

import com.bajaj.bajaj.config.ChallengeProperties;
import com.bajaj.bajaj.dto.GenerateWebhookRequest;
import com.bajaj.bajaj.dto.GenerateWebhookResponse;
import com.bajaj.bajaj.dto.SubmitSolutionRequest;
import com.bajaj.bajaj.entity.Solution;
import com.bajaj.bajaj.repository.SolutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChallengeService {

    private static final Logger log = LoggerFactory.getLogger(ChallengeService.class);

    private static final String GENERATE_WEBHOOK_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    private final RestTemplate restTemplate;
    private final ChallengeProperties properties;
    private final SolutionRepository solutionRepository;

    public ChallengeService(RestTemplate restTemplate,
                            ChallengeProperties properties,
                            SolutionRepository solutionRepository) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.solutionRepository = solutionRepository;
    }

    /**
     * Main flow:
     *  1. Call generateWebhook API.
     *  2. Decide question based on regNo.
     *  3. Pick SQL query from properties and store it in DB.
     *  4. POST finalQuery to returned webhook with JWT in Authorization header.
     */
    public void runChallenge() {
        log.info("Starting challenge flow...");

        // 1. Generate webhook
        GenerateWebhookResponse webhookResponse = callGenerateWebhook();
        if (webhookResponse == null ||
            webhookResponse.getWebhook() == null ||
            webhookResponse.getAccessToken() == null) {
            log.error("Failed to obtain webhook or access token, aborting.");
            return;
        }

        String webhookUrl = webhookResponse.getWebhook();
        String accessToken = webhookResponse.getAccessToken();

        log.info("Received webhook URL: {}", webhookUrl);

        // 2. Decide question based on regNo last two digits
        int questionNumber = decideQuestionNumber(properties.getRegNo());
        log.info("Question selected for regNo {}: Question {}", properties.getRegNo(), questionNumber);

        // 3. Get final SQL query for that question
        String finalQuery = getFinalQueryForQuestion(questionNumber);

        if (finalQuery == null || finalQuery.isBlank()) {
            log.error("No SQL query configured for Question {}. Please set it in application.properties", questionNumber);
            return;
        }

        // Store in DB
        Solution solution = new Solution(properties.getRegNo(), questionNumber, finalQuery);
        solutionRepository.save(solution);
        log.info("Stored solution with id {}", solution.getId());

        // 4. Submit final query to webhook using JWT token
        sendFinalQueryToWebhook(webhookUrl, accessToken, finalQuery);
    }

    private GenerateWebhookResponse callGenerateWebhook() {
        try {
            log.info("Calling generateWebhook with payload: name={}, regNo={}, email={}",
                    properties.getName(), properties.getRegNo(), properties.getEmail());

            GenerateWebhookRequest requestBody = new GenerateWebhookRequest(
                    properties.getName(),
                    properties.getRegNo(),
                    properties.getEmail()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GenerateWebhookRequest> requestEntity =
                    new HttpEntity<>(requestBody, headers);

            ResponseEntity<GenerateWebhookResponse> response =
                    restTemplate.postForEntity(
                            GENERATE_WEBHOOK_URL,
                            requestEntity,
                            GenerateWebhookResponse.class
                    );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("generateWebhook API call successful: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("generateWebhook API call failed with status: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception ex) {
            log.error("Error while calling generateWebhook API", ex);
            return null;
        }
    }

    /**
     * According to the assignment:
     *   last two digits odd  -> Question 1
     *   last two digits even -> Question 2
     */
    private int decideQuestionNumber(String regNo) {
        if (regNo == null) {
            return 1; // default fallback
        }

        String digitsOnly = regNo.replaceAll("\\D+", "");
        if (digitsOnly.length() < 2 && !digitsOnly.isEmpty()) {
            int value = Integer.parseInt(digitsOnly);
            return (value % 2 == 0) ? 2 : 1;
        } else if (digitsOnly.isEmpty()) {
            return 1;
        }

        String lastTwoStr = digitsOnly.substring(digitsOnly.length() - 2);
        int lastTwo = Integer.parseInt(lastTwoStr);
        return (lastTwo % 2 == 0) ? 2 : 1;
    }

    private String getFinalQueryForQuestion(int questionNumber) {
        if (questionNumber == 1) {
            return properties.getQuestion1Sql();
        } else {
            return properties.getQuestion2Sql();
        }
    }

    private void sendFinalQueryToWebhook(String webhookUrl, String accessToken, String finalQuery) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // JWT in Authorization header.
            headers.setBearerAuth(accessToken);

            SubmitSolutionRequest body = new SubmitSolutionRequest(finalQuery);
            HttpEntity<SubmitSolutionRequest> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully submitted final query to webhook. Response: {}", response.getBody());
            } else {
                log.error("Failed to submit final query. Status: {}, Response: {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (Exception ex) {
            log.error("Error while submitting final query to webhook", ex);
        }
    }
}

