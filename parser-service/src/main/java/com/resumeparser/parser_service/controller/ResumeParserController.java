package com.resumeparser.parser_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.resumeparser.parser_service.dto.ParsedResumeDTO;
import com.resumeparser.parser_service.service.ResumeParserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
public class ResumeParserController {
	private final ResumeParserService resumeParserService;

	@PostMapping("/upload")
	public ResponseEntity<ParsedResumeDTO> uploadResume(@RequestParam("file") MultipartFile file) {
		ParsedResumeDTO result = resumeParserService.parseResume(file);
		return ResponseEntity.ok(result);
	}
}
