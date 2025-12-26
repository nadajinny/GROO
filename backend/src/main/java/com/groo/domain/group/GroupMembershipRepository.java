package com.groo.domain.group;

import com.groo.domain.group.GroupStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    @EntityGraph(attributePaths = {"group"})
    List<GroupMembership> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<GroupMembership> findAllByGroupId(Long groupId);

    Optional<GroupMembership> findByGroupIdAndUserId(Long groupId, Long userId);

    long countByGroupId(Long groupId);

    @EntityGraph(attributePaths = {"group"})
    @Query("""
            SELECT m FROM GroupMembership m
            WHERE m.user.id = :userId
            AND (:status IS NULL OR m.group.status = :status)
            AND (:keyword IS NULL OR LOWER(m.group.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY m.group.createdAt DESC
            """)
    Page<GroupMembership> searchMemberships(
            @Param("userId") Long userId,
            @Param("status") GroupStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);
}
