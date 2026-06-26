package az.unibank.smartorder.payment.infrastructure.gateway;

public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message) {
        super(message);
    }
}
