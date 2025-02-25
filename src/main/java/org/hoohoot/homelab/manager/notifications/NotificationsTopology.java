package org.hoohoot.homelab.manager.notifications;

import io.quarkus.kafka.client.serialization.JsonObjectSerde;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;

@ApplicationScoped
public class NotificationsTopology {
    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), new JsonObjectSerde()))
                .filter((key, value) -> "radarr".equals(key))
                .map((key,value) -> KeyValue.pair(value.getJsonObject("movie").getString("id"), value))
                .to("radarr-notifications", Produced.with(Serdes.String(), new JsonObjectSerde()));

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), new JsonObjectSerde()))
                .filter((key, value) -> "lidarr".equals(key))
                .map((key,value) -> KeyValue.pair(value.getJsonObject("artist").getString("id"), value))
                .to("lidarr-notifications", Produced.with(Serdes.String(), new JsonObjectSerde()));

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), new JsonObjectSerde()))
                .filter((key, value) -> "sonarr".equals(key))
                .map((key,value) -> KeyValue.pair(value.getJsonObject("series").getString("titleSlug"), value))
                .to("sonarr-notifications", Produced.with(Serdes.String(), new JsonObjectSerde()));

        return builder.build();
    }
}
