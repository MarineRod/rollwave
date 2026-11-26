package com.rollwave.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

		return new ResponseEntity<>(created, HttpStatus.OK);
	}

	// Modifier son commentaire sur un skatepark
	@PutMapping("/skateparks/{skateparkId}/comments/{commentId}")
	public ResponseEntity<CommentDto> update(@PathVariable Integer skateparkId, @PathVariable Integer commentId,
			@RequestParam Integer userId, @Valid @RequestBody CommentDto commentDto) {

		CommentDto updated = commentService.update(commentId, skateparkId, userId, commentDto);

		return new ResponseEntity<>(updated, HttpStatus.OK);
	}

	// Tous les commentaires d'un skatepark
	@GetMapping("/skateparks/{skateparkId}/comments")
	public ResponseEntity<List<CommentDto>> getAllCommentsBySkatepark(@PathVariable Integer skateparkId) {

		List<CommentDto> comments = commentService.getAllCommentsBySkateparkIdOrderByDateCreationDesc(skateparkId);
		return new ResponseEntity<>(comments, HttpStatus.OK);
	}

	// Nombre de commentaires d'un skatepark
	@GetMapping("/skateparks/{skateparkId}/comments/count")
	public ResponseEntity<BigDecimal> countBySkatepark(@PathVariable Integer skateparkId) {
		BigDecimal nbComments = commentService.getCountBySkateparkId(skateparkId);
		return new ResponseEntity<>(nbComments, HttpStatus.OK);
	}

	// Commentaires d'un utilisateur sur un skatepark
	@GetMapping("/skateparks/{skateparkId}/users/{userId}/comments")
	public ResponseEntity<List<CommentDto>> getAllByUserAndSkatepark(@PathVariable Integer skateparkId,
			@RequestParam Integer userId) {
		List<CommentDto> comments = commentService.getAllCommentsByUserIdAndBySkateparkId(userId, skateparkId);
		return new ResponseEntity<>(comments, HttpStatus.OK);
	}

	// Un commentaire précis, s'il appartient à l'utilisateur
	@GetMapping("/skateparks/{skateparkId}/comments/{commentId}")
	public ResponseEntity<CommentDto> getComment(@PathVariable Integer commentId, @RequestParam Integer userId,
			@PathVariable Integer skateparkId) {
		CommentDto comment = commentService.getCommentByIdAndUserIdAndSkateparkId(commentId, userId, skateparkId);
		return new ResponseEntity<>(comment, HttpStatus.OK);
	}

	// TODO Supprimer son commentaire
	@DeleteMapping("/skateparks/{skateparkId}/comments/{commentId}")
	public ResponseEntity<Void> delete(@PathVariable Integer commentId, @PathVariable Integer skateparkId,
			@RequestParam Integer userId) {
		commentService.delete(userId, commentId, skateparkId);
		return ResponseEntity.noContent().build();
	}
}
