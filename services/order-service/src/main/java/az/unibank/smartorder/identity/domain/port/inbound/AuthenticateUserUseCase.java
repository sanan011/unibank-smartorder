package az.unibank.smartorder.identity.domain.port.inbound;

import az.unibank.smartorder.identity.adapter.inbound.web.dto.AuthResponse;
import az.unibank.smartorder.identity.application.command.LoginCommand;

public interface AuthenticateUserUseCase {
    AuthResponse authenticate(LoginCommand command);
}
