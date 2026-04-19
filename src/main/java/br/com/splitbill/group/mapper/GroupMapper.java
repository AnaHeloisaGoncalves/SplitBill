package br.com.splitbill.group.mapper;

import br.com.splitbill.group.dto.GroupMemberResponse;
import br.com.splitbill.group.dto.GroupResponse;
import br.com.splitbill.group.model.Group;
import br.com.splitbill.group.model.GroupMember;

public class GroupMapper {

    public static GroupResponse toResponse(Group group) {
        return new GroupResponse(
            group.getPublicId(),
            group.getName(),
            group.getDescription(),
            group.getCreatedBy().getName(),
            group.getCreatedAt()
        );
    }

    public static GroupMemberResponse toMemberResponse(GroupMember member) {
        return new GroupMemberResponse(
            member.getUser().getPublicId(),
            member.getUser().getName(),
            member.getRole(),
            member.getJoinedAt()
        );
    }
}
