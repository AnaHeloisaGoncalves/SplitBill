package br.com.splitbill.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
		
		@Size(min = 3, max = 100)
	    String name,

	    @Email(message = "Email inválido")
	    String email,

	    @Size(min = 6, max = 100)
	    String password
		
		) {

}
