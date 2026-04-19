package br.com.splitbill.group.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.splitbill.group.dto.CreateGroupRequest;
import br.com.splitbill.group.dto.GroupMemberResponse;
import br.com.splitbill.group.dto.GroupResponse;
import br.com.splitbill.group.exception.GroupNotFoundException;
import br.com.splitbill.group.exception.NotGroupMemberException;
import br.com.splitbill.group.mapper.GroupMapper;
import br.com.splitbill.group.model.Group;
import br.com.splitbill.group.model.GroupMember;
import br.com.splitbill.group.model.GroupRole;
import br.com.splitbill.group.repository.GroupMemberRepository;
import br.com.splitbill.group.repository.GroupRepository;
import br.com.splitbill.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest req, User currentUser) {
        Group group = Group.builder()
                .name(req.name())
                .description(req.description())
                .createdBy(currentUser)
                .build();

        groupRepository.save(group);

        GroupMember owner = GroupMember.builder()
                .user(currentUser)
                .group(group)
                .role(GroupRole.OWNER)
                .build();

        groupMemberRepository.save(owner);

        log.info("Group '{}' created by user {}", group.getName(), currentUser.getId());
        return GroupMapper.toResponse(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getMyGroups(User currentUser) {
        return groupMemberRepository.findByUser(currentUser)
                .stream()
                .map(GroupMember::getGroup)
                .map(GroupMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroupByPublicId(UUID publicId, User currentUser) {
        Group group = groupRepository.findByPublicId(publicId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + publicId));

        if (!groupMemberRepository.existsByUserAndGroup(currentUser, group)) {
            throw new NotGroupMemberException("You are not a member of this group");
        }

        return GroupMapper.toResponse(group);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(UUID publicId, User currentUser) {
        Group group = groupRepository.findByPublicId(publicId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found: " + publicId));

        if (!groupMemberRepository.existsByUserAndGroup(currentUser, group)) {
            throw new NotGroupMemberException("You are not a member of this group");
        }

        return groupMemberRepository.findByGroup(group)
                .stream()
                .map(GroupMapper::toMemberResponse)
                .toList();
    }
}
