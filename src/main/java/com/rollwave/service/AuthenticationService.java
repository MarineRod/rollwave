package com.rollwave.service;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.rollwave.dao.AppUserDao;
import com.rollwave.dto.AppUserDto;
import com.rollwave.dto.LoginResponse;
import com.rollwave.dto.LoginUserDto;
import com.rollwave.dto.RegisterUserDto;
import com.rollwave.entity.AppUser;
import com.rollwave.security.JwtService;

@Service
public class AuthenticationService {

	private final AppUserDao appUserDao;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final ModelMapper modelMapper;

	public AuthenticationService(AppUserDao appUserDao, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JwtService jwtService, ModelMapper modelMapper) {
		this.appUserDao = appUserDao;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.modelMapper = modelMapper;
	}

	public AppUserDto signup(RegisterUserDto input) {
		String email = normalize(input.email());

		if (appUserDao.findByEmail(email).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
		}

		AppUser user = new AppUser();
		user.setFirstName(input.firstName());
		user.setLastName(input.lastName());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(input.password()));

		AppUser saved = appUserDao.save(user);
		return modelMapper.map(saved, AppUserDto.class);
	}

	public LoginResponse authenticate(LoginUserDto input) {
		// AuthenticationManager -> DaoAuthenticationProvider -> UserDetailsService +
		// PasswordEncoder
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(normalize(input.email()), input.password()));

		AppUser user = (AppUser) authentication.getPrincipal();
		String token = jwtService.generateToken(user);

		return new LoginResponse(token, jwtService.getExpirationTime());
	}

	private String normalize(String email) {
		return email.trim().toLowerCase();
	}
}
