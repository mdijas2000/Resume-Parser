package com.resumeparser.candidate_service.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Education {
    private String degree;
    private String institution;
    private String passingYear;
    private String branch;
    private String percentage;
}
