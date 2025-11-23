package com.klkt.supervision.repository;

import com.klkt.supervision.entity.Group;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface GroupRepository extends R2dbcRepository<Group, Long> {
    
    @Query("SELECT g.* FROM groups g " +
           "INNER JOIN group_members gm ON g.id = gm.group_id " +
           "WHERE gm.user_id = :userId " +
           "ORDER BY g.updated_at DESC")
    Flux<Group> findGroupsByUserId(Long userId);
    
    @Query("SELECT * FROM groups WHERE group_code = :groupCode LIMIT 1")
    Mono<Group> findByGroupCode(String groupCode);
}
