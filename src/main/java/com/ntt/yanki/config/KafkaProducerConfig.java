package com.ntt.yanki.config;

import java.util.Map;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;

@Configuration
public class KafkaProducerConfig {

  @Bean
  public ReactiveKafkaProducerTemplate<String, Object> reactiveKafkaProducerTemplate(
      KafkaProperties properties) {
    Map<String, Object> props = properties.buildProducerProperties(null);
    return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
  }
}
