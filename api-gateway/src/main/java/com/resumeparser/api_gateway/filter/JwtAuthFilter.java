package com.resumeparser.api_gateway.filter;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.resumeparser.api_gateway.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter{
	private final JwtUtil jwtUtil;
	
	protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException,IOException{
		System.out.println("--- Incoming Request: " + request.getMethod() + " " + request.getRequestURI());
		java.util.Enumeration<String> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			System.out.println(headerName + ": " + request.getHeader(headerName));
		}
		
		String authHeader=request.getHeader(HttpHeaders.AUTHORIZATION);
		String token=null;
		if(authHeader!=null && authHeader.startsWith("Bearer ")) {
			token=authHeader.substring(7);
			try {
				jwtUtil.validateToken(token);
				
				UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken("user", null,new ArrayList<>());
				
				SecurityContextHolder.getContext().setAuthentication(authentication);
				System.out.println("Token validated successfully, SecurityContext set.");
			}catch(Exception e) {
				System.out.println("JWT Filter Error: " + e.getMessage());
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Invalid or expired token");
				return;
			}
		} else {
			System.out.println("No valid Authorization header found. authHeader = " + authHeader);
		}
		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getRequestURI();
		return path.startsWith("/auth/");
	}
}
