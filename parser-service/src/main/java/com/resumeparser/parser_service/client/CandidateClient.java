package com.resumeparser.parser_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.resumeparser.parser_service.dto.ParsedResumeDTO;

@FeignClient(name = "CANDIDATE-SERVICE")
public interface CandidateClient {

    @PostMapping("/api/candidates")
    ParsedResumeDTO saveCandidate(@RequestBody ParsedResumeDTO candidateData);

}
