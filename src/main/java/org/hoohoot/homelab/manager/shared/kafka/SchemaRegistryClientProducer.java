package org.hoohoot.homelab.manager.shared.kafka;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class SchemaRegistryClientProducer {

    private static final int MAX_NUM_OF_SCHEMAS = 10;

    @ConfigProperty(name = "quarkus.kafka-streams.schema-registry-url")
    String schemaRegistryUrl;

    @Produces
    @DefaultBean
    public SchemaRegistryClient produceSchemaRegistryClient() {
        return new CachedSchemaRegistryClient(this.schemaRegistryUrl, MAX_NUM_OF_SCHEMAS);
    }
}