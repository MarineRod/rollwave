package com.rollwave.dto;

import com.rollwave.entity.Skatepark;

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
public class SkateparkDto {
	private Integer id;
	private String adress;
	private String description;
	private String latitude;
	private String longitude;

}
