package az.unibank.smartorder.order.adapter.inbound.web.admin;

import az.unibank.smartorder.order.application.dto.DlqMessage;
import az.unibank.smartorder.order.domain.port.inbound.DlqAdminUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import az.unibank.smartorder.security.JwtTokenProvider;
import az.unibank.smartorder.security.TokenBlocklist;
import az.unibank.smartorder.order.infrastructure.ratelimit.RedisRateLimiterService;
import az.unibank.smartorder.order.adapter.inbound.web.filter.RateLimitFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.redis.core.StringRedisTemplate;

@WebMvcTest(
    controllers = DlqAdminController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RateLimitFilter.class)
)
@Import({az.unibank.smartorder.security.SecurityConfig.class, az.unibank.smartorder.security.JwtAuthenticationFilter.class})
class DlqAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DlqAdminUseCase dlqAdminUseCase;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private TokenBlocklist tokenBlocklist;

    @MockBean
    private RedisRateLimiterService redisRateLimiterService;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListMessagesWithAdminRole() throws Exception {
        DlqMessage msg = DlqMessage.builder()
                .id("test-id")
                .queueName("test-dlq")
                .payload("{}")
                .reason("test reason")
                .build();
                
        when(dlqAdminUseCase.listMessages()).thenReturn(List.of(msg));

        mockMvc.perform(get("/admin/dlq/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("test-id"))
                .andExpect(jsonPath("$[0].queueName").value("test-dlq"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldForbidListMessagesWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/admin/dlq/messages"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRetryMessage() throws Exception {
        mockMvc.perform(post("/admin/dlq/messages/test-id/retry").with(csrf()))
                .andExpect(status().isOk());
                
        verify(dlqAdminUseCase).retryMessage("test-id");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDiscardMessage() throws Exception {
        mockMvc.perform(delete("/admin/dlq/messages/test-id").with(csrf()))
                .andExpect(status().isNoContent());
                
        verify(dlqAdminUseCase).discardMessage("test-id");
    }
}
