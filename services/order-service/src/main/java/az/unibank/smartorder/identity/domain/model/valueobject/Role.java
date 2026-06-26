package az.unibank.smartorder.identity.domain.model.valueobject;

public enum Role {
    CUSTOMER,
    ADMIN;

    public static Role fromString(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Invalid role: null");
        }
        try {
            return Role.valueOf(role.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}
