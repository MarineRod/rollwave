package com.rollwave.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rollwave.entity.Video;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface VideoDao extends JpaRepository<Video, Integer> {

	List<Video> findByUserId(Integer userId);
	List<Video> findBySkateparkId(Integer skateparkId);
	List<Video> findByTrickId(Integer trickId);
}
