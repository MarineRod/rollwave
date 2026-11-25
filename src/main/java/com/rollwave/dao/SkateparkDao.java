package com.rollwave.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rollwave.entity.Skatepark;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface SkateparkDao extends JpaRepository<Skatepark, Integer> {

	//Optional<Skatepark> findByGooglePlaceId(String googlePlaceId);
	//boolean existsByGooglePlaceId(String googlePlaceId); // éviter les doublons à l'import Google
	//List<Skatepark> findByAdressContainingIgnoreCase(String address);
	//List<Skatepark> findByUserIsNull(); 
	
	Optional<Skatepark> findByExternalId(String externalId);
}
