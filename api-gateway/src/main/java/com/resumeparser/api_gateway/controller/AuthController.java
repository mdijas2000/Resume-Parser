package com.resumeparser.api_gateway.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.resumeparser.api_gateway.entity.User;
import com.resumeparser.api_gateway.repository.UserRepository;
import com.resumeparser.api_gateway.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@PostMapping("/login")
	public ResponseEntity<?>  login(@RequestBody Map<String,String> credentials){
		String username=credentials.get("username");
		String password=credentials.get("password");
		
		Authentication authentication=authenticationManager.authenticate(new
				UsernamePasswordAuthenticationToken(username,password));
		if(authentication.isAuthenticated()) {
			String token=jwtUtil.generateToken(username);
			return ResponseEntity.ok(Map.of("token",token));
		}else {
			return ResponseEntity.status(401).body("Invalid access");
		}
	}
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody User user){
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole("RECRUITER");
		userRepository.save(user);
		return ResponseEntity.ok("User registered successfully");
	}
}
