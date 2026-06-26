package az.unibank.smartorder.identity.domain.port.inbound;

import az.unibank.smartorder.identity.application.command.RegisterCommand;

public interface RegisterUserUseCase {
    void register(RegisterCommand command);
}
