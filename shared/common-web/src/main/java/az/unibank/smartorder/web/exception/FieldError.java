package az.unibank.smartorder.web.exception;

public record FieldError(
    String field,
    String message,
    Object rejectedValue
) {}
