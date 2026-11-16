package com.rollwave.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rollwave.entity.AppUser;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface AppUserDao extends JpaRepository<AppUser, Integer> {
	/**
	 * Recherche un User via son email
	 * 
	 * @param email email
	 * @return {@link Optional}
	 */
	Optional<AppUser> findByEmail(String email);

}