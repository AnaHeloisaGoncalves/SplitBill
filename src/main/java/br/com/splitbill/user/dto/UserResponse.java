package br.com.splitbill.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
		String publicId,
		String name,
		String email,
		LocalDateTime createdAt
		) {

}
