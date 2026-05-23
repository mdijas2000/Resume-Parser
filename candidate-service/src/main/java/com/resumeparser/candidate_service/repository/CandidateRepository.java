package com.resumeparser.candidate_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resumeparser.candidate_service.entity.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long>{

}
