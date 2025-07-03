package org.hoohoot.homelab.manager.infrastructure.kafka

import io.quarkus.kafka.client.serialization.JsonObjectSerde
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.KeyValueStore

const val ISSUE_NOTIFICATIONS_SENT_STORE = "issue-notifications-sent-store"
const val SERIES_NOTIFICATIONS_SENT_STORE = "series-notifications-sent-store"

@ApplicationScoped
class NotificationsTopology {
    @Produces
    fun buildTopology(): Topology {
        val builder = StreamsBuilder()

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), JsonObjectSerde()))
            .filter { key, value -> "radarr" == key && value.getString("eventType") == "Download" }
            .map { _, value -> KeyValue.pair(value.getJsonObject("movie").getString("tmdbId"), value) }
            .to("radarr-notifications", Produced.with(Serdes.String(), JsonObjectSerde()))

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), JsonObjectSerde()))
            .filter { key, value -> "lidarr" == key && value.getString("eventType") == "Download" }
            .map { _, value -> KeyValue.pair(value.getJsonObject("album").getString("mbId"), value) }
            .to("lidarr-notifications", Produced.with(Serdes.String(), JsonObjectSerde()))

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), JsonObjectSerde()))
            .filter { key, value -> "sonarr" == key && value.getString("eventType") == "Download" }
            .map { _, value -> KeyValue.pair(value.getJsonObject("series").getString("tmdbId"), value) }
            .to("sonarr-notifications", Produced.with(Serdes.String(), JsonObjectSerde()))

        builder.stream("incoming-notifications", Consumed.with(Serdes.String(), JsonObjectSerde()))
            .filter { key, _ -> "jellyseerr" == key }
            .map { _, value -> KeyValue.pair(value.getJsonObject("media").getString("tmdbId"), value) }
            .to("jellyseerr-notifications", Produced.with(Serdes.String(), JsonObjectSerde()))

        builder.globalTable("issue-notifications-sent",
            Materialized.`as`<String, String, KeyValueStore<Bytes, ByteArray>>(ISSUE_NOTIFICATIONS_SENT_STORE)
                .withKeySerde(Serdes.String()).withValueSerde(Serdes.String())
        )

//        builder.globalTable("series-notifications-sent",
//            Materialized.`as`<String, String, KeyValueStore<Bytes, ByteArray>>(SERIES_NOTIFICATIONS_SENT_STORE)
//                .withKeySerde(Serdes.String()).withValueSerde(Serdes.String())
//        )

        return builder.build()
    }
}
