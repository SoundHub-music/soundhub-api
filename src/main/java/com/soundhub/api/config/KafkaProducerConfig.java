package com.soundhub.api.config;

import com.soundhub.api.Constants;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(recommendationProducerFactory());
    }

    @Bean
    public ProducerFactory<String, String> recommendationProducerFactory() {
        Map<String, Object> configProps = new HashMap<>() {{
            put(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    bootstrapAddress
            );

            put(
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class
            );

            put(
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    JsonSerializer.class
            );
        }};

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public NewTopic userRecommendationRequestTopic() {
        return TopicBuilder.name(Constants.USER_RECOMMENDATION_REQUEST_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userRecommendationResponseTopic() {
        return TopicBuilder.name(Constants.USER_RECOMMENDATION_RESPONSE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public KafkaTemplate<String, String> userRecommendationKafkaTemplate() {
        return new KafkaTemplate<>(recommendationProducerFactory());
    }
}
