package az.unibank.smartorder.identity.domain.port.inbound;

import az.unibank.smartorder.identity.application.command.RefreshCommand;
import az.unibank.smartorder.identity.domain.model.TokenPair;

public interface RefreshTokenUseCase {
    TokenPair refresh(RefreshCommand command);
}
