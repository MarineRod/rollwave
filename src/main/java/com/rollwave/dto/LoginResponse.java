package com.rollwave.dto;

public record LoginResponse(String token, long expiresIn) {
}
