package org.hoohoot.homelab.manager.notifications.kafka

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
            .filter { key: String, _: JsonObject? -> "radarr" == key }
            .map { _: String?, value: JsonObject ->
                KeyValue.pair(
                    value.getJsonObject("movie").getString("id"),
                    value
                )
            }
            .to("radarr-notifications", Produced.with(Serdes.String(), JsonObjectSerde()))

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), JsonObjectSerde()))
            .filter { key: String, _: JsonObject? -> "lidarr" == key }
            .map { _: String?, value: JsonObject ->
                KeyValue.pair(
                    value.getJsonObject("artist").getString("id"),
                    value
                )
            }
            .to("lidarr-notifications", Produced.with(Serdes.String(), JsonObjectSerde()))

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), JsonObjectSerde()))
            .filter { key: String, _: JsonObject? -> "sonarr" == key }
            .map { _: String?, value: JsonObject ->
                KeyValue.pair(
                    value.getJsonObject("series").getString("titleSlug"),
                    value
                )
            }
            .to("sonarr-notifications", Produced.with(Serdes.String(), JsonObjectSerde()))

        return builder.build()
    }
}
