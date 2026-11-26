package com.rollwave.service;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
	private final AppUserService appUserService;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final ModelMapper modelMapper;

	public AuthenticationService(AppUserDao appUserDao, AppUserService appUserService,
			AuthenticationManager authenticationManager, JwtService jwtService, ModelMapper modelMapper) {
		super();
		this.appUserDao = appUserDao;
		this.appUserService = appUserService;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.modelMapper = modelMapper;
	}

	public AppUserDto signup(RegisterUserDto input) {
		if (appUserDao.findByEmail(normalize(input.email())).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet email est déjà utilisé");
		}
		AppUser saved = appUserService.createUser(input.firstName(), input.lastName(), input.email(), input.password());
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
