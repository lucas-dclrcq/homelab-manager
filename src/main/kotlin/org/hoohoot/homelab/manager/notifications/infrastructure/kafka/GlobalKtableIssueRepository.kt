package org.hoohoot.homelab.manager.notifications.infrastructure.kafka

import io.smallrye.reactive.messaging.kafka.Record
import jakarta.enterprise.context.ApplicationScoped
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.hoohoot.homelab.manager.notifications.application.ports.IssueRepository
import org.hoohoot.homelab.manager.notifications.domain.NotificationId


@ApplicationScoped
class GlobalKtableIssueRepository(@param:Channel("issue-notifications-sent") private val notificationEmitter: Emitter<Record<String, String>>, private val kafkaStreams: KafkaStreams) : IssueRepository {

    private val issueNotificationStore: ReadOnlyKeyValueStore<String, String> by lazy {
        kafkaStreams.store(StoreQueryParameters.fromNameAndType(ISSUE_NOTIFICATIONS_SENT_STORE, QueryableStoreTypes.keyValueStore()))
    }

    override suspend fun saveNotificationIdForIssue(issueId: String, notification: NotificationId) {
        this.notificationEmitter.send(Record.of(issueId, notification.value))
    }

    override suspend fun getNotificationIdForIssue(issueId: String): NotificationId? {
       return this.issueNotificationStore.get(issueId)?.let { NotificationId(it) }
    }
}