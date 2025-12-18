package com.groo.domain.group;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    @EntityGraph(attributePaths = {"group"})
    List<GroupMembership> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<GroupMembership> findAllByGroupId(Long groupId);

    Optional<GroupMembership> findByGroupIdAndUserId(Long groupId, Long userId);

    long countByGroupId(Long groupId);
}
