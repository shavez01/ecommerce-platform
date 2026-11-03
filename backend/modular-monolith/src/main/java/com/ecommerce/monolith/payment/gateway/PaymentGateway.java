
package com.ecommerce.monolith.payment.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {

    boolean charge(
            Long orderId,
            BigDecimal amount,
            String paymentMethod
    );
}
