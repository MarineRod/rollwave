package com.rollwave.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rollwave.entity.Comment;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface CommentDao extends JpaRepository<Comment, Integer> {

	
	List<Comment> findAllByUserIdAndSkateparkId(Integer userId, Integer skateparkId);

	
	List<Comment> findAllBySkateparkIdOrderByDateDesc(Integer skateparkId);

	//TODO
	List<Comment> findByUserIdOrderByDateDesc(Integer userId);
	
	Optional<Comment> findByIdAndUserIdAndSkateparkId(Integer id, Integer userId, Integer skateparkId);
	
	BigDecimal countBySkateparkId(Integer skateparkId);
	
	int deleteByIdAndUserIdAndSkateparkId(Integer commentId, Integer userId, Integer skateparkId);
}
