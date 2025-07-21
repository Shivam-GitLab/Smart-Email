package com.sm.smartEmail.Entities;

import lombok.Data;

@Data
public class EmailRequest {
    private String emailContent;
    private String emailTone;
}
