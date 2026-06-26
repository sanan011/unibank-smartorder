package az.unibank.smartorder.identity.domain.port.inbound;

import az.unibank.smartorder.identity.application.command.LoginCommand;
import az.unibank.smartorder.identity.domain.model.TokenPair;

public interface AuthenticateUserUseCase {
    TokenPair authenticate(LoginCommand command);
}
