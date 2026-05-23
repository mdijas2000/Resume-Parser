package com.resumeparser.parser_service.dto;

import java.util.List;

import lombok.Data;

@Data
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
