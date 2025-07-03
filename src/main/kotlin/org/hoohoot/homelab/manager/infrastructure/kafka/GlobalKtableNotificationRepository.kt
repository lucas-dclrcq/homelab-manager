package org.hoohoot.homelab.manager.infrastructure.kafka

import io.smallrye.reactive.messaging.kafka.Record
import jakarta.enterprise.context.ApplicationScoped
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationRepository
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationId


@ApplicationScoped
class GlobalKtableNotificationRepository(
    @param:Channel("issue-notifications-sent") private val issueNotificationEmitter: Emitter<Record<String, String>>,
    @param:Channel("series-notifications-sent") private val seriesNotificationEmitter: Emitter<Record<String, String>>,
    private val kafkaStreams: KafkaStreams
) :
    NotificationRepository {

    private val issueNotificationStore: ReadOnlyKeyValueStore<String, String> by lazy {
        kafkaStreams.store(
            StoreQueryParameters.fromNameAndType(
                ISSUE_NOTIFICATIONS_SENT_STORE,
                QueryableStoreTypes.keyValueStore()
            )
        )
    }

//    private val seriesNotificationStore: ReadOnlyKeyValueStore<String, String> by lazy {
//        kafkaStreams.store(
//            StoreQueryParameters.fromNameAndType(
//                SERIES_NOTIFICATIONS_SENT_STORE,
//                QueryableStoreTypes.keyValueStore()
//            )
//        )
//    }

    override suspend fun saveNotificationIdForIssue(issueId: String, notification: NotificationId) {
        this.issueNotificationEmitter.send(Record.of(issueId, notification.value))
    }

    override suspend fun getNotificationIdForIssue(issueId: String): NotificationId? {
        return this.issueNotificationStore.get(issueId)?.let { NotificationId(it) }
    }

    override suspend fun saveNotificationIdForSeries(seriesId: String, notification: NotificationId) {
        this.seriesNotificationEmitter.send(Record.of(seriesId, notification.value))
    }

//    override suspend fun getNotificationIdForSeries(seriesId: String): NotificationId? {
//        return this.seriesNotificationStore.get(seriesId)?.let { NotificationId(it) }
//    }
}