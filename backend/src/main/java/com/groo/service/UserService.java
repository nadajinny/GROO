package com.groo.service;

import com.groo.common.exception.BusinessException;
import com.groo.common.exception.ErrorCode;
import com.groo.domain.user.User;
import com.groo.domain.user.UserRepository;
import com.groo.dto.DeactivateUserRequest;
import com.groo.dto.UpdateUserRoleRequest;
import com.groo.dto.UserResponse;
import com.groo.security.UserPrincipal;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getCurrentUser(UserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    public UserResponse updateRole(Long id, UpdateUserRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.setRole(request.role());
        return UserResponse.from(user);
    }

    public UserResponse deactivate(Long id, DeactivateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.deactivate();
        return UserResponse.from(user);
    }
}
