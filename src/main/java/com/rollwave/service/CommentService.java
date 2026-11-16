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
import jakarta.transaction.Transactional;

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

	@Transactional(rollbackOn = Exception.class)
	public List<CommentDto> getCommentsByUserIdAndBySkateparkId(Integer userId, Integer skateparkId) {

		if (!appUserDao.existsById(userId)) {
			throw new EntityNotFoundException("Utilisateur introuvable : " + userId);
		}
		if (!skateparkDao.existsById(skateparkId)) {
			throw new EntityNotFoundException("Skatepark introuvable : " + skateparkId);
		}

		return commentDao.findByUserIdandSkateparkId(userId, skateparkId).stream().map(this::convertToDto).toList();
	}

	@Transactional(rollbackOn = Exception.class)
	public CommentDto getCommentByIdAndUserId(Integer commentId, Integer userId) {
		if (!appUserDao.existsById(userId)) {
			throw new EntityNotFoundException("Utilisateur introuvable : " + userId);
		}

		Comment comment = commentDao.findByIdAndUserId(commentId, userId);
		if (!commentDao.existsById(userId)) {
			throw new EntityNotFoundException("Commentaire introuvable : " + commentId);
		}

		return convertToDto(comment);
	}

	@Transactional(rollbackOn = Exception.class)
	public List<CommentDto> getCommentsBySkateparkIdOrderByCreatedAtDesc(Integer skateparkId) {
		if (!skateparkDao.existsById(skateparkId)) {
			throw new EntityNotFoundException("Skatepark introuvable : " + skateparkId);
		}

		return commentDao.findBySkateparkIdOrderByCreatedAtDesc(skateparkId).stream().map(this::convertToDto).toList();
	}

	@Transactional(rollbackOn = Exception.class)
	public BigDecimal getCountBySkateparkId(Integer skateparkId) {

		if (!skateparkDao.existsById(skateparkId)) {
			throw new EntityNotFoundException("Skatepark introuvable : " + skateparkId);
		}

		return commentDao.countBySkateparkId(skateparkId);
	}

	// TODO Create
	@Transactional(rollbackOn = Exception.class)
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

	// TODO Delete
	@Transactional(rollbackOn = Exception.class)
	public void delete(Integer skateparkId, Integer userId, Integer commentId) {
		int deleted = commentDao.deletedByIdAndUserIdAndSkateparkId(commentId, userId, skateparkId);
		if (deleted == 0) {
			throw new EntityNotFoundException("Aucun commentaire n'a été trouvé pour cet id là:" + commentId + "lié à l'utilisateur:"
					+ userId + "et au skatepark:" + skateparkId);

		}
	}

	// TODO Update
	@Transactional(rollbackOn = Exception.class)
	public CommentDto udpate(Integer skateparkId, Integer userId, CommentDto commentDto) {
		
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
