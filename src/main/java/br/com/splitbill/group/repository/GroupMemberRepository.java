package br.com.splitbill.group.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.splitbill.group.model.Group;
import br.com.splitbill.group.model.GroupMember;
import br.com.splitbill.group.model.GroupRole;
import br.com.splitbill.user.model.User;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByUser(User user);

    List<GroupMember> findByGroup(Group group);

    Optional<GroupMember> findByUserAndGroup(User user, Group group);

    boolean existsByUserAndGroup(User user, Group group);

    boolean existsByUserAndGroupAndRole(User user, Group group, GroupRole role);
}
