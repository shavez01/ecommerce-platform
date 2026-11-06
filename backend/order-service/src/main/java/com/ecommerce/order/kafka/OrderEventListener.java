package com.ecommerce.order.kafka;

import com.ecommerce.order.event.OrderConfirmedEvent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventListener {

    private final OrderEventProducer orderEventProducer;

    public OrderEventListener(OrderEventProducer orderEventProducer) {
        this.orderEventProducer = orderEventProducer;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderConfirmed(OrderConfirmedEvent event) {

        orderEventProducer.publishOrderConfirmed(event);
    }
}