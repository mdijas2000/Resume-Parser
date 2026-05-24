package com.resumeparser.api_gateway.util;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	private final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
	
	private SecretKey getSignKey() {
		return Keys.hmacShaKeyFor(SECRET.getBytes());
	}
	public String generateToken(String username) {
		return Jwts.builder()
					.subject(username)
					.issuedAt(new Date(System.currentTimeMillis()))
					.expiration(new Date(System.currentTimeMillis()+1000*60*60))
					.signWith(getSignKey())
							.compact();
	}
	public void validateToken(final String token) {
		Jwts.parser()
			.verifyWith(getSignKey())
			.build()
			.parseSignedClaims(token);
	}
}
