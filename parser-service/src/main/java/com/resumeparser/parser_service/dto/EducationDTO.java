package com.resumeparser.parser_service.dto;

import lombok.Data;

@Data
public class EducationDTO {
	private String degree;
	private String institution;
	private String passingYear;
	private String percentage;
	private String branch;
}
