package com.klkt.supervision.repository;

import com.klkt.supervision.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageRepository extends R2dbcRepository<Message, Long> {
    
    Flux<Message> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);
    
    Flux<Message> findByGroupIdOrderByCreatedAtDesc(Long groupId);
}
