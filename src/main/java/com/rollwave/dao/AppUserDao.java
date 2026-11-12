package com.rollwave.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rollwave.entity.AppUser;

public interface AppUserDao extends JpaRepository<AppUser, Integer> {
	/**
	 * Recherche un User via son email
	 * 
	 * @param email email
	 * @return {@link Optional}
	 */
	Optional<AppUser> findByEmail(String email);

}