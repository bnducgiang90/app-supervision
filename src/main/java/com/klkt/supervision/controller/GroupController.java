package com.klkt.supervision.controller;

import com.klkt.supervision.dto.AddMemberRequest;
import com.klkt.supervision.dto.CreateGroupRequest;
import com.klkt.supervision.dto.GroupResponse;
import com.klkt.supervision.dto.UserResponse;
import com.klkt.supervision.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    
    private final GroupService groupService;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        return groupService.createGroup(request);
    }
    
    @GetMapping("/{id}")
    public Mono<GroupResponse> getGroupById(@PathVariable Long id) {
        return groupService.getGroupById(id);
    }
    
    @GetMapping("/user/{userId}")
    public Flux<GroupResponse> getUserGroups(@PathVariable Long userId) {
        return groupService.getUserGroups(userId);
    }
    
    @PostMapping("/{groupId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> addMember(
            @PathVariable Long groupId,
            @Valid @RequestBody AddMemberRequest request) {
        return groupService.addMember(groupId, request);
    }
    
    @DeleteMapping("/{groupId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        return groupService.removeMember(groupId, userId);
    }
    
    @GetMapping("/{groupId}/members")
    public Flux<UserResponse> getGroupMembers(@PathVariable Long groupId) {
        return groupService.getGroupMembers(groupId);
    }
}
