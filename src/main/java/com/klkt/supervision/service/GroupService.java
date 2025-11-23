package com.klkt.supervision.service;

import com.klkt.supervision.dto.AddMemberRequest;
import com.klkt.supervision.dto.CreateGroupRequest;
import com.klkt.supervision.dto.GroupResponse;
import com.klkt.supervision.dto.UserResponse;
import com.klkt.supervision.entity.Group;
import com.klkt.supervision.entity.GroupMember;
import com.klkt.supervision.repository.GroupMemberRepository;
import com.klkt.supervision.repository.GroupRepository;
import com.klkt.supervision.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SSEService sseService;
    private final GroupCodeService groupCodeService;
    
    public Mono<GroupResponse> createGroup(CreateGroupRequest request) {
        return userRepository.existsById(request.getCreatedBy())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RuntimeException("User not found"));
                    }
                    
                    // Generate unique group code
                    return groupCodeService.generateGroupCode()
                            .flatMap(groupCode -> {
                                Group group = Group.builder()
                                        .name(request.getName())
                                        .description(request.getDescription())
                                        .avatarUrl(request.getAvatarUrl())
                                        .groupCode(groupCode)
                                        .createdBy(request.getCreatedBy())
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();
                                
                                return groupRepository.save(group);
                            });
                })
                .flatMap(savedGroup -> {
                    // Add creator as admin
                    GroupMember member = GroupMember.builder()
                            .groupId(savedGroup.getId())
                            .userId(request.getCreatedBy())
                            .role(GroupMember.MemberRole.ADMIN)
                            .joinedAt(LocalDateTime.now())
                            .build();
                    
                    return groupMemberRepository.save(member)
                            .then(getGroupMemberCount(savedGroup.getId())
                                    .map(count -> toGroupResponse(savedGroup, count)))
                            .doOnSuccess(g -> {
                                log.info("Created group: {} with code: {}", g.getName(), savedGroup.getGroupCode());
                                sseService.sendToUser(request.getCreatedBy(), 
                                        "group_created", g);
                            });
                });
    }
    
    public Mono<Void> addMember(Long groupId, AddMemberRequest request) {
        return groupRepository.existsById(groupId)
                .flatMap(groupExists -> {
                    if (!groupExists) {
                        return Mono.error(new RuntimeException("Group not found"));
                    }
                    
                    return userRepository.existsById(request.getUserId())
                            .flatMap(userExists -> {
                                if (!userExists) {
                                    return Mono.error(new RuntimeException("User not found"));
                                }
                                
                                return groupMemberRepository.existsByGroupIdAndUserId(
                                        groupId, request.getUserId())
                                        .flatMap(isMember -> {
                                            if (isMember) {
                                                return Mono.error(new RuntimeException(
                                                        "User is already a member"));
                                            }
                                            
                                            GroupMember member = GroupMember.builder()
                                                    .groupId(groupId)
                                                    .userId(request.getUserId())
                                                    .role(GroupMember.MemberRole.valueOf(
                                                            request.getRole().toUpperCase()))
                                                    .joinedAt(LocalDateTime.now())
                                                    .build();
                                            
                                            return groupMemberRepository.save(member)
                                                    .then(getGroupById(groupId))
                                                    .doOnSuccess(group -> {
                                                        log.info("Added user {} to group {}", 
                                                                request.getUserId(), groupId);
                                                        sseService.sendToGroup(groupId, 
                                                                "member_added", group);
                                                    })
                                                    .then();
                                        });
                            });
                });
    }
    
    public Mono<Void> removeMember(Long groupId, Long userId) {
        return groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId)
                .doOnSuccess(v -> {
                    log.info("Removed user {} from group {}", userId, groupId);
                    
                    // Create a simple Map to avoid self-reference in anonymous class
                    java.util.Map<String, Long> data = new java.util.HashMap<>();
                    data.put("groupId", groupId);
                    data.put("userId", userId);
                    
                    sseService.sendToGroup(groupId, "member_removed", data);
                });
    }
    
    public Mono<GroupResponse> getGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .flatMap(group -> getGroupMemberCount(groupId)
                        .map(count -> toGroupResponse(group, count)))
                .switchIfEmpty(Mono.error(new RuntimeException("Group not found")));
    }
    
    public Flux<GroupResponse> getUserGroups(Long userId) {
        // First get user to check role
        return userRepository.findById(userId)
                .flatMapMany(user -> {
                    // If user is ADMIN, return all groups
                    if (user.getRole() != null && user.getRole() == com.klkt.supervision.entity.User.UserRole.ADMIN) {
                        return groupRepository.findAll()
                                .flatMap(group -> getGroupMemberCount(group.getId())
                                        .map(count -> toGroupResponse(group, count)));
                    } else {
                        // If user is MEMBER, return only groups they are member of
                        return groupRepository.findGroupsByUserId(userId)
                                .flatMap(group -> getGroupMemberCount(group.getId())
                                        .map(count -> toGroupResponse(group, count)));
                    }
                })
                .switchIfEmpty(Flux.empty());
    }
    
    public Flux<UserResponse> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId)
                .flatMap(member -> userRepository.findById(member.getUserId()))
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .displayName(user.getDisplayName())
                        .avatarUrl(user.getAvatarUrl())
                        .role(user.getRole() != null ? user.getRole().name() : "MEMBER")
                        .status(user.getStatus().name())
                        .createdAt(user.getCreatedAt())
                        .build());
    }
    
    private Mono<Integer> getGroupMemberCount(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId)
                .count()
                .map(Long::intValue);
    }
    
    private GroupResponse toGroupResponse(Group group, Integer memberCount) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .avatarUrl(group.getAvatarUrl())
                .groupCode(group.getGroupCode())
                .createdBy(group.getCreatedBy())
                .memberCount(memberCount)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}
