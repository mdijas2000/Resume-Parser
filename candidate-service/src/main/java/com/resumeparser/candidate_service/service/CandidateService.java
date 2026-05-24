package com.resumeparser.candidate_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.resumeparser.candidate_service.entity.Candidate;
import com.resumeparser.candidate_service.repository.CandidateRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CandidateService {
	private final CandidateRepository candidateRepository;
	
	@Transactional
	public Candidate saveCandidate(Candidate candidate) {
		
		try {
			Optional<Candidate> existingCandidate=candidateRepository.findFirstByEmail(candidate.getEmail());
			
			if(existingCandidate.isPresent()) {
				Candidate existing = existingCandidate.get();
				// Force initialize lazy collections
				if (existing.getSkills() != null) existing.getSkills().size();
				if (existing.getEducation() != null) existing.getEducation().size();
				if (existing.getExperience() != null) existing.getExperience().size();
				if (existing.getProjects() != null) existing.getProjects().size();
				return existing;
			}
		} catch (Exception e) {
			System.err.println("CRASH IN CANDIDATE SERVICE WHEN FETCHING DUPLICATE:");
			e.printStackTrace();
			throw e;
		}
		
		return candidateRepository.save(candidate);
	}
	
	public List<Candidate> getAllCandidates(){
		return candidateRepository.findAll();
	}
}
