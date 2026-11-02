
package com.ecommerce.monolith.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> new TopicPartition(
                                KafkaTopicConfig.PAYMENT_FAILED_DLT_TOPIC,
                                record.partition()
                        )
                );

        ExponentialBackOff backOff = new ExponentialBackOff(
                1000L, 2.0
        );

        backOff.setMaxInterval(5000L);
        backOff.setMaxElapsedTime(15000L);

        return new DefaultErrorHandler(recoverer, backOff);
    }

    @Bean
    public NewTopic paymentFailedDltTopic() {
        return TopicBuilder.name(KafkaTopicConfig.PAYMENT_FAILED_DLT_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
