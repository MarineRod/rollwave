package com.rollwave.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
public class Skatepark {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(unique = true)
    private String name;
	
	@Column(nullable = false)
	private String adress;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private double latitude;

	@Column(nullable = false)
	private double longitude;
	
	@Column(unique = true)
    private String externalId;

	//one to many comment
	@OneToMany(mappedBy ="skatepark")
	private List<Comment> comments;
	//many to one user
	@ManyToOne
	@JoinColumn(name ="user_id")
	private AppUser user;
	//one to many video
	@OneToMany(mappedBy ="skatepark")
	private List<Video> videos;
}
