package com.resumeparser.candidate_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.resumeparser.candidate_service.entity.Candidate;
import com.resumeparser.candidate_service.repository.CandidateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandidateService {
	private final CandidateRepository candidateRepository;
	
	public Candidate saveCandidate(Candidate candidate) {
		return candidateRepository.save(candidate);
	}
	
	public List<Candidate> getAllCandidates(){
		return candidateRepository.findAll();
	}
}
