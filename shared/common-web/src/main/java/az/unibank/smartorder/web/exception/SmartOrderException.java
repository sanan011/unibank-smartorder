package az.unibank.smartorder.web.exception;

import lombok.Getter;

@Getter
public abstract class SmartOrderException extends RuntimeException {
    private final String code;
    private final int httpStatus;

    protected SmartOrderException(String message, String code, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }
}
