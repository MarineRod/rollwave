package com.rollwave.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rollwave.dto.AppUserDto;
import com.rollwave.dto.LoginResponse;
import com.rollwave.dto.LoginUserDto;
import com.rollwave.dto.RegisterUserDto;
import com.rollwave.service.AuthenticationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationService authenticationService;

	public AuthController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/signup")
	public ResponseEntity<AppUserDto> signup(@Valid @RequestBody RegisterUserDto input) {
		AppUserDto user = authenticationService.signup(input);
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginUserDto input) {
		return ResponseEntity.ok(authenticationService.authenticate(input));
	}
}