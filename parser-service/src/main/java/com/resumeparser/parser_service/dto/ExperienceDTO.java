package com.resumeparser.parser_service.dto;

import lombok.Data;

@Data
public class ExperienceDTO {
	private String company;
	private String role;
	private String duration;
	private boolean isCurrentJob;
}
