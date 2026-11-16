package com.rollwave.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rollwave.dto.CommentDto;
import com.rollwave.service.CommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CommentController {

	private final CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	// Ajouter un commentaire sur un skatepark
	@PostMapping("/skateparks/{skateparkId}/comments")
	public ResponseEntity<CommentDto> create(@PathVariable Integer skateparkId, @RequestParam Integer userId,
			@Valid @RequestBody CommentDto commentDto) {

		CommentDto created = commentService.create(skateparkId, userId, commentDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}
	
	//TODO getCommentByIdAndUserId
	//TODO getCommentsByIdAndUserIdAndBySkateparkId
	//TODO getCommentsBySkateparkIdOrderByCreatedAtDesc
	//TODO getCommentsByUserIdOrderByCreatedAtDesc
	//TODO getCommentsByUserIdAndSkateparkId
	//TODO getCountBySkateparkId
	//TODO Update
	//TODO Delete
}
