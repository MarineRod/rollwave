package com.rollwave.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.rollwave.dao.AppUserDao;
import com.rollwave.dao.CommentDao;
import com.rollwave.dao.SkateparkDao;

@Service
public class SkateparkService {
	private final ModelMapper modelMapper;
	private final CommentDao commentDao;
	private final AppUserDao appUserDao;
	private final SkateparkDao skateparkDao;

	public SkateparkService(ModelMapper modelMapper, CommentDao commentDao, AppUserDao appUserDao,
			SkateparkDao skateparkDao) {
		super();
		this.modelMapper = modelMapper;
		this.commentDao = commentDao;
		this.appUserDao = appUserDao;
		this.skateparkDao = skateparkDao;
	}

	
}
