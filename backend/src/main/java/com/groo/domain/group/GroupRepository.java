package com.groo.domain.group;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    boolean existsByInvitationCode(String invitationCode);

    Optional<Group> findByInvitationCode(String invitationCode);
}
