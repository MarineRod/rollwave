package com.rollwave.dto;

import java.time.LocalDateTime;

import com.rollwave.entity.Comment;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

	private Integer id;
	private String content;
	private LocalDateTime date;
}
