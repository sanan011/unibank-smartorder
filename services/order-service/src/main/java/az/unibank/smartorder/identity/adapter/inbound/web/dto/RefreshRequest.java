package az.unibank.smartorder.identity.adapter.inbound.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    @Override
    public String toString() {
        return "RefreshRequest{" +
                "refreshToken='[PROTECTED]'" +
                '}';
    }
}
