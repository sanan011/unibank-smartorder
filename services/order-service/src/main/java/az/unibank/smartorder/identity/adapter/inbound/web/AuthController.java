package az.unibank.smartorder.identity.adapter.inbound.web;

import az.unibank.smartorder.identity.adapter.inbound.web.dto.AuthResponse;
import az.unibank.smartorder.identity.adapter.inbound.web.dto.LoginRequest;
import az.unibank.smartorder.identity.adapter.inbound.web.dto.RefreshRequest;
import az.unibank.smartorder.identity.adapter.inbound.web.dto.RegisterRequest;
import az.unibank.smartorder.identity.application.command.LoginCommand;
import az.unibank.smartorder.identity.application.command.RefreshCommand;
import az.unibank.smartorder.identity.application.command.RegisterCommand;
import az.unibank.smartorder.identity.domain.model.TokenPair;
import az.unibank.smartorder.identity.domain.port.inbound.AuthenticateUserUseCase;
import az.unibank.smartorder.identity.domain.port.inbound.RefreshTokenUseCase;
import az.unibank.smartorder.identity.domain.port.inbound.RegisterUserUseCase;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUseCase;
    private final AuthenticateUserUseCase authenticateUseCase;
    private final RefreshTokenUseCase refreshUseCase;
    private final az.unibank.smartorder.security.JwtTokenProvider tokenProvider;
    private final az.unibank.smartorder.security.TokenBlocklist tokenBlocklist;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        registerUseCase.register(new RegisterCommand(request.getEmail(), request.getPassword(), request.getRole()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenPair tokenPair = authenticateUseCase.authenticate(new LoginCommand(request.getEmail(), request.getPassword()));
        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .expiresIn(tokenPair.getExpiresIn())
                .tokenType("Bearer")
                .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenPair tokenPair = refreshUseCase.refresh(new RefreshCommand(request.getRefreshToken()));
        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .refreshToken(tokenPair.getRefreshToken())
                .expiresIn(tokenPair.getExpiresIn())
                .tokenType("Bearer")
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@org.springframework.web.bind.annotation.RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            io.jsonwebtoken.Claims claims = tokenProvider.getValidatedClaims(token);
            if (claims != null && claims.getId() != null && claims.getExpiration() != null) {
                long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    tokenBlocklist.blockToken(claims.getId(), ttl);
                }
            }
        }
        return ResponseEntity.noContent().build();
    }
}
