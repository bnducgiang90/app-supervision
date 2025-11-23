package com.klkt.supervision.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("group_members")
public class GroupMember {
    
    @Id
    private Long id;
    
    private Long groupId;
    private Long userId;
    private MemberRole role;
    private LocalDateTime joinedAt;
    
    public enum MemberRole {
        ADMIN, MODERATOR, MEMBER
    }
}
