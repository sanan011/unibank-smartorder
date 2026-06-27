package az.unibank.smartorder.identity.adapter.inbound.web;

import az.unibank.smartorder.identity.adapter.inbound.web.dto.LoginRequest;
import az.unibank.smartorder.identity.application.command.LoginCommand;
import az.unibank.smartorder.identity.domain.model.TokenPair;
import az.unibank.smartorder.identity.domain.port.inbound.AuthenticateUserUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
class LogoutIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("order_db")
            .withUsername("order_user")
            .withPassword("order_pass");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management-alpine"));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockBean
    private AuthenticateUserUseCase authenticateUseCase;

    @Autowired
    private az.unibank.smartorder.security.JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void tearDown() {
        if (redisTemplate.getConnectionFactory() != null) {
            Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushAll();
        }
    }

    @Test
    void shouldRejectTokenAfterLogoutAndAcceptNewToken() throws Exception {
        // Arrange - generate two valid tokens
        UUID userId = UUID.randomUUID();
        az.unibank.smartorder.security.UserPrincipal principal = new az.unibank.smartorder.security.UserPrincipal(
                userId, "test@unibank.az", "password", "ROLE_CUSTOMER", false);
        String token1 = jwtTokenProvider.generateToken(principal);
        String token2 = jwtTokenProvider.generateToken(principal);

        when(authenticateUseCase.authenticate(any(LoginCommand.class))).thenReturn(
                az.unibank.smartorder.identity.domain.model.TokenPair.builder()
                        .accessToken(token2)
                        .refreshToken("refresh")
                        .expiresIn(3600L)
                        .build()
        );

        // Act & Assert 1: Token1 is valid before logout
        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());

        // Act 2: Logout with Token1
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isNoContent());

        // Assert 3: Token1 is now rejected
        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isUnauthorized());

        // Act 4: Login again to get new Token2
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk());

        // Assert 5: Token2 is accepted
        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());
    }
}
