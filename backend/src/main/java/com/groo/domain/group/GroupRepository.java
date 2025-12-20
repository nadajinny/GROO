package com.groo.domain.group;

import com.groo.domain.group.GroupStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    boolean existsByInvitationCode(String invitationCode);

    Optional<Group> findByInvitationCode(String invitationCode);

    long countByStatus(GroupStatus status);
}
