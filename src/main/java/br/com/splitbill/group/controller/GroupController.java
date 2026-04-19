package br.com.splitbill.group.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.splitbill.group.dto.CreateGroupRequest;
import br.com.splitbill.group.dto.GroupMemberResponse;
import br.com.splitbill.group.dto.GroupResponse;
import br.com.splitbill.group.service.GroupService;
import br.com.splitbill.user.model.User;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Validated @RequestBody CreateGroupRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.createGroup(req, currentUser));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(groupService.getMyGroups(currentUser));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(groupService.getGroupByPublicId(publicId, currentUser));
    }

    @GetMapping("/{publicId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getMembers(
            @PathVariable UUID publicId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(groupService.getGroupMembers(publicId, currentUser));
    }
}
