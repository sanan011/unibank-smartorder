package az.unibank.smartorder.identity.domain.service;

import az.unibank.smartorder.identity.adapter.inbound.web.dto.AuthResponse;
import az.unibank.smartorder.identity.application.command.LoginCommand;
import az.unibank.smartorder.identity.application.command.RefreshCommand;
import az.unibank.smartorder.identity.application.command.RegisterCommand;
import az.unibank.smartorder.identity.domain.event.UserRegisteredEvent;
import az.unibank.smartorder.identity.domain.model.aggregate.User;
import az.unibank.smartorder.identity.domain.model.valueobject.Email;
import az.unibank.smartorder.identity.domain.model.valueobject.PasswordHash;
import az.unibank.smartorder.identity.domain.model.valueobject.Role;
import az.unibank.smartorder.identity.domain.model.valueobject.UserId;
import az.unibank.smartorder.identity.domain.port.inbound.AuthenticateUserUseCase;
import az.unibank.smartorder.identity.domain.port.inbound.RefreshTokenUseCase;
import az.unibank.smartorder.identity.domain.port.inbound.RegisterUserUseCase;
import az.unibank.smartorder.identity.domain.port.outbound.RefreshTokenRepository;
import az.unibank.smartorder.identity.domain.port.outbound.UserRepository;
import az.unibank.smartorder.security.JwtTokenProvider;
import az.unibank.smartorder.security.UserPrincipal;
import az.unibank.smartorder.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthDomainService implements RegisterUserUseCase, AuthenticateUserUseCase, RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void register(RegisterCommand command) {
        Email email = new Email(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("User with this email already exists", "EMAIL_ALREADY_EXISTS", 409);
        }

        PasswordHash passwordHash = new PasswordHash(passwordEncoder.encode(command.password()));
        Role role = Role.fromString(command.role());

        User user = User.create(email, passwordHash, role);
        userRepository.save(user);

        UserRegisteredEvent event = new UserRegisteredEvent(user.getId(), user.getEmail(), user.getRole());
        eventPublisher.publishEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse authenticate(LoginCommand command) {
        Email email = new Email(command.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Invalid email or password", "INVALID_CREDENTIALS", 401));

        if (!user.isActive()) {
            throw new BusinessException("User account is inactive", "USER_INACTIVE", 403);
        }

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash().value())) {
            throw new BusinessException("Invalid email or password", "INVALID_CREDENTIALS", 401);
        }

        return generateTokens(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshCommand command) {
        if (!jwtTokenProvider.validateToken(command.refreshToken())) {
            throw new BusinessException("Refresh token is invalid or expired", "INVALID_TOKEN", 401);
        }

        UserId userId = UserId.of(jwtTokenProvider.getUserIdFromJWT(command.refreshToken()).toString());
        String jti = jwtTokenProvider.getJtiFromJWT(command.refreshToken());

        if (!refreshTokenRepository.exists(userId, jti)) {
            throw new BusinessException("Refresh token has been revoked or does not exist", "INVALID_TOKEN", 401);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", "USER_NOT_FOUND", 404));

        if (!user.isActive()) {
            throw new BusinessException("User account is inactive", "USER_INACTIVE", 403);
        }

        // Revoke the old refresh token (Refresh Token Rotation)
        refreshTokenRepository.revoke(userId, jti);

        return generateTokens(user);
    }

    private AuthResponse generateTokens(User user) {
        UserPrincipal principal = UserPrincipal.builder()
                .id(user.getId().value())
                .email(user.getEmail().value())
                .passwordHash(user.getPasswordHash().value())
                .role(user.getRole().name())
                .active(user.isActive())
                .build();

        String accessToken = jwtTokenProvider.generateToken(principal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(principal);
        String jti = jwtTokenProvider.getJtiFromJWT(refreshToken);

        refreshTokenRepository.save(user.getId(), jti, jwtTokenProvider.getRefreshExpirationMs() / 1000);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getJwtExpirationMs() / 1000)
                .tokenType("Bearer")
                .build();
    }
}
