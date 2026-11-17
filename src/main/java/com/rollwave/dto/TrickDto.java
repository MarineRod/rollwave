package com.rollwave.dto;

import com.rollwave.entity.Trick;

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
public class TrickDto {
	private Integer id;
	private String name;
	private String description;
	private String level;

}
