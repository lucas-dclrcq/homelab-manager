package org.hoohoot.homelab.manager.shared.kafka;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.apache.avro.specific.SpecificRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;


@Dependent
public class SerdeProducer {

  @Inject
  SchemaRegistryClient registry;

  @ConfigProperty(name = "quarkus.kafka-streams.schema-registry-url")
  String schemaRegistryUrl;

  @Produces
  @DefaultBean
  public <T extends SpecificRecord> SpecificAvroSerde<T> produceSpecificAvroSerde() {
    final SpecificAvroSerde<T> valueSerde = new SpecificAvroSerde<>(this.registry);
    valueSerde.configure(
        Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, this.schemaRegistryUrl),
        false);
    return valueSerde;
  }
}