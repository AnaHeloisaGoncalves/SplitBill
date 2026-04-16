package br.com.splitbill.user.mapper;

import br.com.splitbill.user.dto.UserResponse;
import br.com.splitbill.user.model.User;

public final class UserMapper {
    
    private UserMapper() {
        // Prevent instantiation
    }

    public static UserResponse toResponse(User entity) {
        if (entity == null) {
            return null;
        }

        return new UserResponse(
                entity.getPublicId() != null ? entity.getPublicId().toString() : null,
                entity.getName(),
                entity.getEmail(),
                entity.getCreatedAt()
        );
    }
}
