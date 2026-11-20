package com.rollwave.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.rollwave.dao.AppUserDao;
import com.rollwave.entity.AppUser;

@Service
public class AppUserService {

	 private final AppUserDao appUserDao;

	    public AppUserService(AppUserDao appUserDao) {
	        this.appUserDao = appUserDao;
	    }

	    public AppUser getConnectedUser() {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	        if (authentication == null
	                || !authentication.isAuthenticated()
	                || !(authentication.getPrincipal() instanceof AppUser principal)) {
	            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Aucun utilisateur connecté");
	        }

	        String email = principal.getEmail();
	        return appUserDao.findByEmail(email)
	                .orElseThrow(() -> new ResponseStatusException(
	                        HttpStatus.UNAUTHORIZED, "Utilisateur non trouvé : " + email));
	    }
	}
