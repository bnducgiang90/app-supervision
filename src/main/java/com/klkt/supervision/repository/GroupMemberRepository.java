package com.klkt.supervision.repository;

import com.klkt.supervision.entity.GroupMember;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GroupMemberRepository extends R2dbcRepository<GroupMember, Long> {
    
    Flux<GroupMember> findByGroupId(Long groupId);
    
    Mono<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    
    Mono<Boolean> existsByGroupIdAndUserId(Long groupId, Long userId);
    
    Mono<Void> deleteByGroupIdAndUserId(Long groupId, Long userId);
}
