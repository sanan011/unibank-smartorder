package az.unibank.smartorder.identity.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenPair {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
