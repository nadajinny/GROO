package com.groo.domain.group;

import com.groo.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_memberships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "user_id"})
})
public class GroupMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupRole role = GroupRole.MEMBER;

    @Column(nullable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    protected GroupMembership() {
    }

    public GroupMembership(Group group, User user, GroupRole role) {
        this.group = group;
        this.user = user;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public Group getGroup() {
        return group;
    }

    public User getUser() {
        return user;
    }

    public GroupRole getRole() {
        return role;
    }

    public void setRole(GroupRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
