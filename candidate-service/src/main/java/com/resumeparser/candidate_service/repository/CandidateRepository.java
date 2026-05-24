package com.resumeparser.candidate_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.resumeparser.candidate_service.entity.Candidate;
import java.util.List;
import java.util.Optional;


public interface CandidateRepository extends JpaRepository<Candidate, Long>{
	Optional<Candidate> findFirstByEmail(String email);
}
