package az.unibank.smartorder.identity.application.command;

public record RegisterCommand(String email, String password, String role) {
}
