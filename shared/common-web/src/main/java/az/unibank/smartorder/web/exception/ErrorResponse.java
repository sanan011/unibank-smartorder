package az.unibank.smartorder.web.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.MDC;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String code;
    private String message;
    private List<FieldError> details;
    private String correlationId;
    private String path;

    private ErrorResponse(Instant timestamp, int status, String error, String code, String message, List<FieldError> details, String correlationId, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.details = details == null ? null : List.copyOf(details);
        this.correlationId = correlationId;
        this.path = path;
    }

    public List<FieldError> getDetails() {
        return details == null ? null : List.copyOf(details);
    }

    public static ErrorResponse of(int status, String code, String message, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status)
                .error(org.springframework.http.HttpStatus.valueOf(status).getReasonPhrase())
                .code(code)
                .message(message)
                .correlationId(MDC.get("correlationId"))
                .path(getPath(request))
                .build();
    }

    public static ErrorResponse validation(List<FieldError> errors, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(400)
                .error("Bad Request")
                .code("VALIDATION_ERROR")
                .message("Request validation failed")
                .details(errors)
                .correlationId(MDC.get("correlationId"))
                .path(getPath(request))
                .build();
    }

    public static ErrorResponse internal(WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(500)
                .error("Internal Server Error")
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .correlationId(MDC.get("correlationId"))
                .path(getPath(request))
                .build();
    }

    private static String getPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return request.getDescription(false);
    }

    public static class ErrorResponseBuilder {
        private List<FieldError> details;

        public ErrorResponseBuilder details(List<FieldError> details) {
            this.details = details == null ? null : List.copyOf(details);
            return this;
        }
    }
}
