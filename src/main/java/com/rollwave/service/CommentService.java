package com.rollwave.service;

import java.math.BigDecimal;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.rollwave.dao.AppUserDao;
import com.rollwave.dao.CommentDao;
import com.rollwave.dao.SkateparkDao;
import com.rollwave.dto.CommentDto;
import com.rollwave.entity.AppUser;
import com.rollwave.entity.Comment;
import com.rollwave.entity.Skatepark;
import com.rollwave.exception.InvalidContentException;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
	private final ModelMapper modelMapper;
	private final CommentDao commentDao;
	private final AppUserDao appUserDao;
	private final SkateparkDao skateparkDao;

	public CommentService(ModelMapper modelMapper, CommentDao commentDao, AppUserDao appUser,
			SkateparkDao skateparkDao) {
		super();
		this.modelMapper = modelMapper;
		this.commentDao = commentDao;
		this.appUserDao = appUser;
		this.skateparkDao = skateparkDao;
	}

	public CommentDto convertToDto(Comment comment) {
		return modelMapper.map(comment, CommentDto.class);
	}

	public Comment convertToEntity(CommentDto commentDto) {
		return modelMapper.map(commentDto, Comment.class);
	}

	@Transactional
	public List<CommentDto> getAllCommentsByUserIdAndBySkateparkId(Integer userId, Integer skateparkId) {

		if (!appUserDao.existsById(userId)) {
			throw new EntityNotFoundException("Utilisateur introuvable : " + userId);
		}
		if (!skateparkDao.existsById(skateparkId)) {
			throw new EntityNotFoundException("Skatepark introuvable : " + skateparkId);
		}

		return commentDao.findAllByUserIdAndSkateparkId(userId, skateparkId).stream().map(this::convertToDto).toList();
	}

	@Transactional(readOnly = true)
	public CommentDto getCommentByIdAndUserIdAndSkateparkId(Integer commentId, Integer userId, Integer skateparkId) {

		Comment comment = commentDao.findByIdAndUserIdAndSkateparkId(commentId, userId, skateparkId)
				.orElseThrow(() -> new EntityNotFoundException("Commentaire introuvable : " + commentId));

		return convertToDto(comment);
	}

	@Transactional
	public List<CommentDto> getAllCommentsBySkateparkIdOrderByDateCreationDesc(Integer skateparkId) {
		if (!skateparkDao.existsById(skateparkId)) {
			throw new EntityNotFoundException("Skatepark introuvable : " + skateparkId);
		}

		return commentDao.findAllBySkateparkIdOrderByDateDesc(skateparkId).stream().map(this::convertToDto)
				.toList();
	}

	@Transactional
	public BigDecimal getCountBySkateparkId(Integer skateparkId) {

		if (!skateparkDao.existsById(skateparkId)) {
			throw new EntityNotFoundException("Skatepark introuvable : " + skateparkId);
		}

		return commentDao.countBySkateparkId(skateparkId);
	}


	@Transactional
	public CommentDto create(Integer skateparkId, Integer userId, CommentDto commentDto)
			throws InvalidContentException {
		Comment comment = convertToEntity(commentDto);

		AppUser user = appUserDao.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("user introuvable:" + userId));
		Skatepark skatepark = skateparkDao.findById(skateparkId)
				.orElseThrow(() -> new EntityNotFoundException("skatepark introuvable:" + skateparkId));
		validationComment(comment);
		comment.setId(null);
		comment.setSkatepark(skatepark);
		comment.setUser(user);

		Comment saved = commentDao.save(comment);

		return convertToDto(saved);

	}

	
	@Transactional
	public void delete(Integer skateparkId, Integer userId, Integer commentId) {
		int deleted = commentDao.deleteByIdAndUserIdAndSkateparkId(commentId, userId, skateparkId);
		if (deleted == 0) {
			throw new EntityNotFoundException("Aucun commentaire n'a été trouvé pour cet id là:" + commentId
					+ "lié à l'utilisateur:" + userId + "et au skatepark:" + skateparkId);

		}
	}

	
	@Transactional
	public CommentDto update(Integer commentId, Integer skateparkId, Integer userId, CommentDto commentDto) {

		Comment comment = commentDao.findByIdAndUserIdAndSkateparkId(commentId, userId, skateparkId)
				.orElseThrow(() -> new EntityNotFoundException("commentaire introuvable:" + commentId));

		comment.setContent(commentDto.getContent());
		validationComment(comment);
		return convertToDto(commentDao.save(comment));
	}

	// =============================
	// MÉTHODES PRIVÉES
	// =============================

	private void validationComment(Comment comment) throws InvalidContentException {

		if (comment.getContent() == null) {
			throw new InvalidContentException("le commentaire ne peut pas être vide");
		}

	}
}
