package com.resumeparser.candidate_service.entity;


import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="candidates")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Candidate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Column(unique = true)
	private String email;
	private String phnNo;
	@ElementCollection
	@CollectionTable(name="candidate_skills",joinColumns=@JoinColumn(name="candidate_id"))
	@Column(name="skill")
	private List<String> skills;

	private double totalExperience;

	@ElementCollection
	@CollectionTable(name="candidate_education", joinColumns=@JoinColumn(name="candidate_id"))
	private List<Education> education;

	@ElementCollection
	@CollectionTable(name="candidate_experience", joinColumns=@JoinColumn(name="candidate_id"))
	private List<Experience> experience;
	@ElementCollection
	@CollectionTable(name="candidate_project", joinColumns=@JoinColumn(name="candidate_id"))
	private List<Project> projects;
}
