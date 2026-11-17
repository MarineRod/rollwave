package com.rollwave.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class AppUser implements UserDetails {

	/**
	 * AppUser.java
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	@Column(unique = true, nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// Pas de rôles pour le moment
		return List.of();
	}

	@Override
	public String getUsername() {
		return email;
	}

	// Un user crée 0..n tricks
	@OneToMany(mappedBy = "user")
	private List<Trick> tricks;

	// Un user écrit 0..n commentaires
	@OneToMany(mappedBy = "user")
	private List<Comment> comments;

	// Un user publie 0..n vidéos
	@OneToMany(mappedBy = "user")
	private List<Video> videos;

	// Un user ajoute 0..n skateparks
	@OneToMany(mappedBy = "user")
	private List<Skatepark> skateparks;

}
