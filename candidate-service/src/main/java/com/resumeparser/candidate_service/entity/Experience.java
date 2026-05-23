package com.resumeparser.candidate_service.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Experience {
    private String company;
    private String role;
    private String duration;
    private boolean currentJob;
}
