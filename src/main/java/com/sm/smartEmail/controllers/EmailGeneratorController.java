package com.sm.smartEmail.controllers;

import com.sm.smartEmail.Entities.EmailRequest;
import com.sm.smartEmail.services.EmailGeneratorService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@AllArgsConstructor
public class EmailGeneratorController {
    private final EmailGeneratorService emailGeneratorService;
    @PostMapping("/generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest){
        String emailResponse = emailGeneratorService.generateEmailReply(emailRequest);
        return ResponseEntity.ok(emailResponse);
    }
}
