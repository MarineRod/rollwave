package com.rollwave.dto;

import java.time.LocalDateTime;

import com.rollwave.entity.Video;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
	private Integer id;
	private String title;
	private String description;
	private LocalDateTime date;

}
