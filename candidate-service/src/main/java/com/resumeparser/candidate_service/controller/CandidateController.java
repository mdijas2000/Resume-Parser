package com.resumeparser.candidate_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resumeparser.candidate_service.entity.Candidate;
import com.resumeparser.candidate_service.service.CandidateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidateController {
	private final CandidateService candidateService;
	
	@PostMapping
	public ResponseEntity<Candidate> create(@RequestBody Candidate candidate){
		return ResponseEntity.status(HttpStatus.CREATED).body(candidateService.saveCandidate(candidate));
	}
	@GetMapping
	public List<Candidate> findAll(){
		return candidateService.getAllCandidates();
	}
}
