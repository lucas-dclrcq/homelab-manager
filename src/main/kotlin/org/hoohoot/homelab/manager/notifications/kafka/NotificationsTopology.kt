package org.hoohoot.homelab.manager.notifications.kafk

import io.quarkus.kafka.client.serialization.JsonObjectSerde
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced

@ApplicationScoped
class NotificationsTopology {
    @Produces
    fun buildTopology(): Topology {
        val builder = StreamsBuilder()

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), JsonObjectSerde()))
            .filter { key, value -> "radarr" == key && value.getString("eventType") == "Download"}
            .map { _, value -> KeyValue.pair(value.getJsonObject("movie").getString("tmdbId"), value) }
            .to("radarr-notifications", Produced.with(Serdes.String(), JsonObjectSerde()))

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), JsonObjectSerde()))
            .filter { key, value -> "lidarr" == key && value.getString("eventType") == "Download"}
            .map { _, value -> KeyValue.pair(value.getJsonObject("album").getString("mbId"), value) }
            .to("lidarr-notifications", Produced.with(Serdes.String(), JsonObjectSerde()))

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), JsonObjectSerde()))
            .filter { key, value -> "sonarr" == key && value.getString("eventType") == "Download"}
            .map { _, value -> KeyValue.pair(value.getJsonObject("series").getString("tmdbId"), value) }
            .to("sonarr-notifications", Produced.with(Serdes.String(), JsonObjectSerde()))

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), JsonObjectSerde()))
            .filter { key, _ -> "jellyseerr" == key}
            .map { _, value -> KeyValue.pair(value.getJsonObject("media").getString("tmdbId"), value) }
            .to("jellyseerr-notifications", Produced.with(Serdes.String(), JsonObjectSerde()))

        return builder.build()
    }
}
