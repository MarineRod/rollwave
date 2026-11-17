package com.rollwave.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rollwave.entity.Trick;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface TrickDao extends JpaRepository<Trick, Integer> {

	List<Trick> findByUserId(Integer userId);
	List<Trick> findByLevel(String difficulty);
	List<Trick> findByNameContainingIgnoreCase(String name);
}
