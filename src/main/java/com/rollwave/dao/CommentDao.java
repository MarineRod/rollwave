package com.rollwave.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rollwave.entity.Comment;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface CommentDao extends JpaRepository<Comment, Integer> {

	
	List<Comment> findByUserIdandSkateparkId(Integer userId, Integer skateparkId);

	
	List<Comment> findBySkateparkIdOrderByCreatedAtDesc(Integer skateparkId);

	//TODO
	List<Comment> findByUserIdOrderByCreatedAtDesc(Integer userId);
	
	Comment findByIdAndUserIdAndSkateparkId(Integer id, Integer userId, Integer skateparkId);

	Comment findByIdAndUserId(Integer id, Integer userId);

	
	BigDecimal countBySkateparkId(Integer skateparkId);
	
	int deletedByIdAndUserIdAndSkateparkId(Integer commentId, Integer userId, Integer skateparkId);
}
