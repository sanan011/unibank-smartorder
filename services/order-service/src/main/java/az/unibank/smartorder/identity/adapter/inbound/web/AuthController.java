package az.unibank.smartorder.identity.adapter.inbound.web;

import az.unibank.smartorder.identity.adapter.inbound.web.dto.AuthResponse;
import az.unibank.smartorder.identity.adapter.inbound.web.dto.LoginRequest;
import az.unibank.smartorder.identity.adapter.inbound.web.dto.RefreshRequest;
import az.unibank.smartorder.identity.adapter.inbound.web.dto.RegisterRequest;
import az.unibank.smartorder.identity.application.command.LoginCommand;
import az.unibank.smartorder.identity.application.command.RefreshCommand;
import az.unibank.smartorder.identity.application.command.RegisterCommand;
import az.unibank.smartorder.identity.domain.port.inbound.AuthenticateUserUseCase;
import az.unibank.smartorder.identity.domain.port.inbound.RefreshTokenUseCase;
import az.unibank.smartorder.identity.domain.port.inbound.RegisterUserUseCase;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        registerUseCase.register(new RegisterCommand(request.getEmail(), request.getPassword(), request.getRole()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authenticateUseCase.authenticate(new LoginCommand(request.getEmail(), request.getPassword()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        AuthResponse response = refreshUseCase.refresh(new RefreshCommand(request.getRefreshToken()));
        return ResponseEntity.ok(response);
    }
}
