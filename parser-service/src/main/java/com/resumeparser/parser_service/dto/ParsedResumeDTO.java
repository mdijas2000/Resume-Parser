package com.resumeparser.parser_service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedResumeDTO {
	private String email;
	private String phnNo;
	private String name;
	private List<String> skills;
	private List<EducationDTO> education;
	private List<ExperienceDTO> experience;
	private List<String> certifications;
	private List<ProjectDTO> projects;
	private double totalExperience;
}
