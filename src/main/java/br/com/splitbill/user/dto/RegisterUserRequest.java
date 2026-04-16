package br.com.splitbill.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
		
		@NotBlank(message = "Nome é obrigatório")
	    @Size(min = 3, max = 100)
		String name,
		
		@NotBlank(message = "Email é obrigatório")
	    @Email(message = "Email inválido")
	    @Size(max = 150)
		String email,
		
		@NotBlank(message = "Senha é obrigatória")
	    @Size(min = 6, max = 100, message = "A senha deve ter no mínimo 6 caracteres")
		String password
		) {

}
