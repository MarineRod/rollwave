package com.rollwave.enums;

import java.util.Arrays;
import java.util.Optional;

public enum SkateparkType {

	SKATEPARK("Skatepark"), PUMPTRACK("Pumptrack"), ANNEAU_ROLLER("Anneau de rollers"),
	ANNEAU_VITESSE("Anneau de vitesse");

	private final String label;

	SkateparkType(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * Retrouve le type à partir du libellé du fichier Data ES (sans tenir compte de
	 * la casse).
	 */
	public static Optional<SkateparkType> fromLabel(String label) {
		if (label == null) {
			return Optional.empty();
		}
		return Arrays.stream(values()).filter(type -> type.label.equalsIgnoreCase(label.trim())).findFirst();
	}
}