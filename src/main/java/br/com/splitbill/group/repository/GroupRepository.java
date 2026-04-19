package br.com.splitbill.group.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.splitbill.group.model.Group;
import br.com.splitbill.user.model.User;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByPublicId(UUID publicId);

    List<Group> findByCreatedBy(User createdBy);
}
