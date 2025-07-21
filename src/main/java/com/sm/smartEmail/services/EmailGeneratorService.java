package com.sm.smartEmail.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sm.smartEmail.Entities.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EmailGeneratorService {
    private final WebClient webClient;
    private final String apiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder,
                                 @Value("${gemini.api.url}") String baseUrl,
                                 @Value("${gemini.api.key}") String geminiApiKey) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = geminiApiKey;
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        // Step 1 : Build Prompt
        String prompt = buildPrompt(emailRequest);

        // Step 2 : Prepare raw JSOnn Body
        String requestBody = String.format("""
                  {
                  "contents": [
                    {
                      "parts": [
                        {
                          "text": "%s"
                        }
                      ]
                    }
                  ]
                }
                """,prompt);

        // Step 3 : Send Request
        String response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/gemini-2.0-flash:generateContent")
                        .build())
                .header("X-goog-api-key",apiKey)
                .header("Content-Type","application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Step 4 : Extract Response
        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Private method to create prompt from EmailRequest
    private String buildPrompt(EmailRequest emailRequest) {
        // Normal String => Can't Modify
        StringBuilder prompt = new StringBuilder(); // Create a new StringBuilder

        // Add the main instruction to prompt
        prompt.append("Generate a professional email reply for the following email content. ");
        prompt.append("Please do not generate a subject line. ");

        // Check if the tone is given and add it
        if (emailRequest.getEmailTone() != null && !emailRequest.getEmailTone().isEmpty()) {
            prompt.append("Use a ").append(emailRequest.getEmailTone()).append(" tone. ");
        }

        // Add original email content to the prompt
        prompt.append("\nOriginal Email:\n").append(emailRequest.getEmailContent());

        // Return complete prompt
        return prompt.toString();
    }
}
