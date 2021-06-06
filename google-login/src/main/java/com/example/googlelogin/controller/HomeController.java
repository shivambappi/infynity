package com.example.googlelogin.controller;



import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

	@GetMapping("/home")
	public ResponseEntity<?> home()
	{
		return ResponseEntity.ok("Welcome");
	}
	
	
	
	@GetMapping("/login")
	public ResponseEntity<Principal> prevent(Principal principal)
	{
		return ResponseEntity.ok(principal);
	}
	
	
	@GetMapping("/")
	public ResponseEntity<Principal> first(Principal principal)
	{
		
		return ResponseEntity.ok(principal);
	}
	
	
}
