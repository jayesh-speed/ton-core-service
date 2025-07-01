package com.speed.toncore.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.scram.ScramLoginModule;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor(onConstructor_ = @ConstructorBinding)
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaConfig {

	private static final String JAAS_CONFIG = "%s required username=\"%s\" password=\"%s\";";

	private final String bootstrapServers;
	private final String username;
	private final String password;
	private final String schemaRegistryUrl;

	private static void setKeyDeserializerProperty(Map<String, Object> configProps) {
		configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerJson() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		return factory;
	}

	@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		Map<String, Object> props = HashMap.newHashMap(7);
		setKeyDeserializerProperty(props);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		setCommonAuthenticationProperties(props);
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, ?> exactlyOnceDeliveryKafkaListener() {
		ConcurrentKafkaListenerContainerFactory<String, ?> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(getDefaultExactlyOnceConsumerFactory());
		factory.setConcurrency(3);
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
		return factory;
	}

	@Bean
	public DefaultKafkaConsumerFactory<String, Object> getDefaultExactlyOnceConsumerFactory() {
		Map<String, Object> props = HashMap.newHashMap(10);
		props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
		props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		setCommonConsumerProperties(props);
		return new DefaultKafkaConsumerFactory<>(props);
	}

	private void setCommonConsumerProperties(Map<String, Object> props) {
		setCommonAuthenticationProperties(props);
		setKeyDeserializerProperty(props);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
		props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
	}

	private void setCommonAuthenticationProperties(Map<String, Object> props) {
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(JAAS_CONFIG, ScramLoginModule.class.getName(), username, password));
	}
}
