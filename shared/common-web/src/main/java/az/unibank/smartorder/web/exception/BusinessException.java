package az.unibank.smartorder.web.exception;

public class BusinessException extends SmartOrderException {
    
    public BusinessException(String message, String code) {
        super(message, code, 400); // 400 Bad Request by default for business exceptions
    }

    public BusinessException(String message, String code, int httpStatus) {
        super(message, code, httpStatus);
    }
}
