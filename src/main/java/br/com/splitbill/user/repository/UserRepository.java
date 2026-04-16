package br.com.splitbill.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import br.com.splitbill.user.model.User;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
	
	public User findByEmail(String email);
	public Optional<User> findByPublicId(UUID publicId);
	public boolean existsByEmail(String email);
	public boolean existsByPublicId(UUID publicId);

}