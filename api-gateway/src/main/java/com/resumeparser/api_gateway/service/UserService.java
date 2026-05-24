package com.resumeparser.api_gateway.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.resumeparser.api_gateway.entity.User;
import com.resumeparser.api_gateway.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService{
	private final UserRepository userRepository;
	
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		User user=userRepository.findByUsername(username).orElseThrow(()-> new UsernameNotFoundException("User not found: "+username));
		return new org.springframework.security.core.userdetails.User(
											user.getUsername(),
											user.getPassword(),new ArrayList<>());
	}
}
