package com.rollwave.dto;

import com.rollwave.enums.SkateparkType;

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
	private String name;
	private SkateparkType type;
	private String adress;
	private String latitude;
	private String longitude;

}
