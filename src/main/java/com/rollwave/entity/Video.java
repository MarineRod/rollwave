package com.rollwave.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Video {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false)
	private String title;
	
	@Column(nullable = false)
	private String description;
	
	@Column(nullable = false)
	private LocalDateTime date;
	
	//many to one trick
	@ManyToOne
	@JoinColumn(name ="trick_id")
	private Trick trick;
	
	//many to one user
	@ManyToOne
	@JoinColumn(name="user_id")
	private AppUser user;
	
	//many to one skatepark
	@ManyToOne
	@JoinColumn(name="skatepark_id")
	private Skatepark skatepark;



}
