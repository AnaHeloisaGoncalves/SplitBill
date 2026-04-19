package br.com.splitbill.group.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.splitbill.group.model.GroupRole;

public record GroupMemberResponse(
    UUID userPublicId,
    String userName,
    GroupRole role,
    LocalDateTime joinedAt
) {}
