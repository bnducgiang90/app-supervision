package com.klkt.supervision.repository;

import com.klkt.supervision.entity.MessageAttachment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageAttachmentRepository extends R2dbcRepository<MessageAttachment, Long> {
    
    Flux<MessageAttachment> findByMessageId(Long messageId);
}
