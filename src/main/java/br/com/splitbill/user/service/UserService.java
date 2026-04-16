package br.com.splitbill.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.splitbill.user.dto.UpdateUserRequest;
import br.com.splitbill.user.dto.UserResponse;
import br.com.splitbill.user.exception.DuplicateEmailException;
import br.com.splitbill.user.exception.UserNotFoundException;
import br.com.splitbill.user.mapper.UserMapper;
import br.com.splitbill.user.model.User;
import br.com.splitbill.user.repository.UserRepository;
import br.com.splitbill.user.repository.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getProfile(User currentUser) {
        return UserMapper.toResponse(currentUser);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        return userRepository.findAll(UserSpecification.searchUsers(query), pageable)
                .map(UserMapper::toResponse);
    }

    @Transactional
    public UserResponse updateProfile(User currentUser, UpdateUserRequest req) {
        if (StringUtils.hasText(req.email()) && !currentUser.getEmail().equals(req.email())) {
            if (userRepository.existsByEmail(req.email())) {
                throw new DuplicateEmailException("Email já cadastrado: " + req.email());
            }
            currentUser.setEmail(req.email());
            log.info("Usuário {} atualizou o email para {}", currentUser.getId(), req.email());
        }

        if (StringUtils.hasText(req.name())) {
            currentUser.setName(req.name());
        }

        if (StringUtils.hasText(req.password())) {
            currentUser.setPassword(passwordEncoder.encode(req.password()));
            log.info("Usuário {} atualizou sua senha", currentUser.getId());
        }

        User updated = userRepository.save(currentUser);
        return UserMapper.toResponse(updated);
    }
}
