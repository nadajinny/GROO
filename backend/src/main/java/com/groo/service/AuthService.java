package com.groo.service;

import com.groo.common.exception.BusinessException;
import com.groo.common.exception.ErrorCode;
import com.groo.domain.auth.RefreshToken;
import com.groo.domain.auth.RefreshTokenRepository;
import com.groo.domain.user.Role;
import com.groo.domain.user.SocialProvider;
import com.groo.domain.user.User;
import com.groo.domain.user.UserRepository;
import com.groo.domain.user.UserStatus;
import com.groo.dto.AuthResponse;
import com.groo.dto.LoginRequest;
import com.groo.dto.RefreshTokenRequest;
import com.groo.dto.RegisterRequest;
import com.groo.dto.SocialLoginRequest;
import com.groo.security.JwtTokenProvider;
import com.groo.service.oauth.FirebaseTokenVerifier;
import com.groo.service.oauth.GoogleTokenVerifier;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            GoogleTokenVerifier googleTokenVerifier,
            FirebaseTokenVerifier firebaseTokenVerifier,
            TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleTokenVerifier = googleTokenVerifier;
        this.firebaseTokenVerifier = firebaseTokenVerifier;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public AuthResponse register(RegisterRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        });
        User user = new User(request.email(), passwordEncoder.encode(request.password()), request.displayName());
        user.setRole(Role.USER);
        user.setProvider(SocialProvider.LOCAL);
        userRepository.save(user);
        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new BusinessException(ErrorCode.FORBIDDEN_OPERATION);
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(user);
    }

    public AuthResponse loginWithGoogle(SocialLoginRequest request) {
        var profile = googleTokenVerifier.verify(request.idToken());
        if (!StringUtils.hasText(profile.email())) {
            throw new BusinessException(ErrorCode.INVALID_SOCIAL_TOKEN);
        }
        User user = userRepository.findByEmail(profile.email())
                .orElseGet(() -> createSocialUser(profile.email(), profile.name(), SocialProvider.GOOGLE, profile.id()));
        return issueTokens(user);
    }

    public AuthResponse loginWithFirebase(SocialLoginRequest request) {
        var profile = firebaseTokenVerifier.verify(request.idToken());
        String resolvedEmail = profile.email();
        if (resolvedEmail == null) {
            resolvedEmail = profile.uid() + "@firebase.local";
        }
        final String finalEmail = resolvedEmail;
        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> createSocialUser(finalEmail, profile.name(), SocialProvider.FIREBASE, profile.uid()));
        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
        if (refreshToken.isRevoked()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
        if (refreshToken.isExpired()) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        User user = refreshToken.getUser();
        return issueTokens(user);
    }

    public void logout(RefreshTokenRequest request, String accessToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));
        refreshToken.revoke();
        if (StringUtils.hasText(accessToken)) {
            tokenBlacklistService.blacklist(accessToken, jwtTokenProvider.getRemainingValidity(accessToken));
        }
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        refreshTokenRepository.save(new RefreshToken(user, refreshToken, jwtTokenProvider.getRefreshTokenExpiryInstant()));
        return new AuthResponse(accessToken, refreshToken, jwtTokenProvider.getAccessTokenExpirySeconds());
    }

    private User createSocialUser(String email, String displayName, SocialProvider provider, String providerId) {
        String resolvedName = StringUtils.hasText(displayName) ? displayName : email;
        User user = new User(email, passwordEncoder.encode(UUID.randomUUID().toString()), resolvedName);
        user.setProvider(provider);
        user.setProviderId(providerId);
        return userRepository.save(user);
    }
}
