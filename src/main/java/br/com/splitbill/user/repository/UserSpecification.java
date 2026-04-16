package br.com.splitbill.user.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import br.com.splitbill.user.model.User;

public class UserSpecification {

    public static Specification<User> searchUsers(String query) {
        return (root, cq, cb) -> {
            if (!StringUtils.hasText(query)) {
                return cb.conjunction();
            }
            String likePattern = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("email")), likePattern)
            );
        };
    }
}
