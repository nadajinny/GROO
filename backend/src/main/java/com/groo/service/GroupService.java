package com.groo.service;

import com.groo.common.exception.BusinessException;
import com.groo.common.exception.ErrorCode;
import com.groo.domain.group.Group;
import com.groo.domain.group.GroupMembership;
import com.groo.domain.group.GroupMembershipRepository;
import com.groo.domain.group.GroupRepository;
import com.groo.domain.group.GroupRole;
import com.groo.domain.group.GroupStatus;
import com.groo.domain.user.User;
import com.groo.domain.user.UserRepository;
import com.groo.dto.AddGroupMemberRequest;
import com.groo.dto.CreateGroupRequest;
import com.groo.dto.GroupDetailDto;
import com.groo.dto.GroupMemberDto;
import com.groo.dto.GroupSummaryDto;
import com.groo.dto.JoinGroupRequest;
import com.groo.dto.PageResponse;
import com.groo.dto.UpdateGroupRequest;
import com.groo.security.UserPrincipal;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class GroupService {

    private static final Comparator<GroupMembership> MEMBER_COMPARATOR =
            Comparator.comparing(GroupMembership::getRole)
                    .thenComparing(GroupMembership::getJoinedAt);

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public GroupService(
            GroupRepository groupRepository,
            GroupMembershipRepository membershipRepository,
            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    public List<GroupSummaryDto> myGroups(UserPrincipal principal) {
        Long userId = requirePrincipal(principal);
        return membershipRepository.findAllByUserId(userId).stream()
                .sorted(Comparator.comparing((GroupMembership m) -> m.getGroup().getCreatedAt()).reversed())
                .map(membership -> {
                    long memberCount = membershipRepository.countByGroupId(membership.getGroup().getId());
                    return GroupSummaryDto.of(membership.getGroup(), membership.getRole(), memberCount);
                })
                .toList();
    }

    public GroupDetailDto createGroup(CreateGroupRequest request, UserPrincipal principal) {
        User owner = fetchCurrentUser(principal);
        Group group = new Group(request.name(), request.description(), owner);
        assignUniqueInvitationCode(group);
        Group saved = groupRepository.save(group);
        membershipRepository.save(new GroupMembership(saved, owner, GroupRole.OWNER));
        return toDetailDto(saved, GroupRole.OWNER);
    }

    public GroupDetailDto getGroup(Long groupId, UserPrincipal principal) {
        GroupMembership membership = requireMembership(groupId, requirePrincipal(principal));
        return toDetailDto(membership.getGroup(), membership.getRole());
    }

    public GroupDetailDto updateGroup(Long groupId, UpdateGroupRequest request, UserPrincipal principal) {
        GroupMembership membership = requireMembership(groupId, requirePrincipal(principal));
        ensureManagerPrivileges(membership);
        Group group = membership.getGroup();
        group.setName(request.name());
        group.setDescription(request.description());
        if (request.archived()) {
            group.setStatus(GroupStatus.ARCHIVED);
        } else {
            group.setStatus(GroupStatus.ACTIVE);
        }
        return toDetailDto(group, membership.getRole());
    }

    public List<GroupMemberDto> listMembers(Long groupId, UserPrincipal principal) {
        requireMembership(groupId, requirePrincipal(principal));
        return membershipRepository.findAllByGroupId(groupId).stream()
                .sorted(MEMBER_COMPARATOR)
                .map(GroupMemberDto::from)
                .toList();
    }

    public GroupMemberDto addMember(Long groupId, AddGroupMemberRequest request, UserPrincipal principal) {
        GroupMembership myMembership = requireMembership(groupId, requirePrincipal(principal));
        ensureManagerPrivileges(myMembership);
        User target = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        membershipRepository.findByGroupIdAndUserId(groupId, target.getId())
                .ifPresent(m -> {
                    throw new BusinessException(ErrorCode.GROUP_MEMBER_ALREADY_EXISTS);
                });
        GroupRole role = request.role() == null ? GroupRole.MEMBER : request.role();
        if (role != GroupRole.MEMBER && myMembership.getRole() != GroupRole.OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN_OPERATION);
        }
        GroupMembership created = new GroupMembership(myMembership.getGroup(), target, role);
        membershipRepository.save(created);
        return GroupMemberDto.from(created);
    }

    public void removeMember(Long groupId, Long membershipId, UserPrincipal principal) {
        GroupMembership myMembership = requireMembership(groupId, requirePrincipal(principal));
        ensureManagerPrivileges(myMembership);
        GroupMembership target = membershipRepository.findById(membershipId)
                .filter(member -> member.getGroup().getId().equals(groupId))
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_MEMBER_NOT_FOUND));
        if (target.getRole() == GroupRole.OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN_OPERATION);
        }
        membershipRepository.delete(target);
    }

    public GroupDetailDto joinByInvitation(JoinGroupRequest request, UserPrincipal principal) {
        String normalized = request.invitationCode().trim().toUpperCase(Locale.ROOT);
        Group group = groupRepository.findByInvitationCode(normalized)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVITATION_INVALID));
        if (group.getStatus() == GroupStatus.ARCHIVED) {
            throw new BusinessException(ErrorCode.GROUP_ARCHIVED);
        }
        Long userId = requirePrincipal(principal);
        membershipRepository.findByGroupIdAndUserId(group.getId(), userId)
                .ifPresent(m -> {
                    throw new BusinessException(ErrorCode.GROUP_MEMBER_ALREADY_EXISTS);
                });
        User user = fetchCurrentUser(principal);
        membershipRepository.save(new GroupMembership(group, user, GroupRole.MEMBER));
        return toDetailDto(group, GroupRole.MEMBER);
    }

    public PageResponse<GroupSummaryDto> searchGroups(
            String keyword, GroupStatus status, int page, int size, UserPrincipal principal) {
        Long userId = requirePrincipal(principal);
        String normalizedKeyword = normalizeKeyword(keyword);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 5), 100);
        PageRequest pageable = PageRequest.of(safePage, safeSize);
        Page<GroupMembership> membershipPage =
                membershipRepository.searchMemberships(userId, status, normalizedKeyword, pageable);
        Page<GroupSummaryDto> dtoPage = membershipPage.map(member -> {
            long memberCount = membershipRepository.countByGroupId(member.getGroup().getId());
            return GroupSummaryDto.of(member.getGroup(), member.getRole(), memberCount);
        });
        return PageResponse.from(dtoPage);
    }

    public String regenerateInvitation(Long groupId, UserPrincipal principal) {
        GroupMembership membership = requireMembership(groupId, requirePrincipal(principal));
        if (membership.getRole() != GroupRole.OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN_OPERATION);
        }
        Group group = membership.getGroup();
        assignUniqueInvitationCode(group);
        return group.getInvitationCode();
    }

    private GroupDetailDto toDetailDto(Group group, GroupRole myRole) {
        long memberCount = membershipRepository.countByGroupId(group.getId());
        List<GroupMemberDto> members = membershipRepository.findAllByGroupId(group.getId()).stream()
                .sorted(MEMBER_COMPARATOR)
                .map(GroupMemberDto::from)
                .toList();
        return GroupDetailDto.of(group, myRole, memberCount, members);
    }

    private GroupMembership requireMembership(Long groupId, Long userId) {
        return membershipRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_ACCESS_DENIED));
    }

    private void ensureManagerPrivileges(GroupMembership membership) {
        if (membership.getRole() == GroupRole.MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN_OPERATION);
        }
    }

    private void assignUniqueInvitationCode(Group group) {
        String code;
        do {
            group.regenerateInvitationCode();
            code = group.getInvitationCode();
        } while (groupRepository.existsByInvitationCode(code));
    }

    private Long requirePrincipal(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return principal.getId();
    }

    private User fetchCurrentUser(UserPrincipal principal) {
        Long id = requirePrincipal(principal);
        return userRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String normalizeKeyword(String keyword) {
        return StringUtils.hasText(keyword) ? keyword.trim() : null;
    }
}
