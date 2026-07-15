package org.hoohoot.homelab.manager.cleanup.infra

import de.connect2x.trixnity.clientserverapi.client.MatrixClientServerApiClient
import de.connect2x.trixnity.core.model.EventId
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.events.m.ReactionEventContent
import de.connect2x.trixnity.core.model.events.m.RelationType
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.ports.SuggestionVetoes
import org.hoohoot.homelab.manager.shared.matrix.MatrixRoomProvider

@ApplicationScoped
class MatrixVetoReactionsAdapter(
    private val matrixClient: MatrixClientServerApiClient,
    private val roomProvider: MatrixRoomProvider,
) : SuggestionVetoes {
    companion object {
        const val VETO_EMOJI = "❌"

        // Certains clients Matrix suffixent l'emoji d'un variation selector
        private const val VARIATION_SELECTOR = "\uFE0F"
        private const val PAGE_SIZE = 100L
        private const val MAX_PAGES = 10

        fun isVetoEmoji(key: String): Boolean = key.replace(VARIATION_SELECTOR, "") == VETO_EMOJI
    }

    override suspend fun vetoers(announcementEventId: String): List<String> {
        val roomId = RoomId(roomProvider.media)
        val vetoers = mutableListOf<String>()
        var from: String? = null
        repeat(MAX_PAGES) {
            val response = matrixClient.room.getRelations(
                roomId = roomId,
                eventId = EventId(announcementEventId),
                relationType = RelationType.Annotation,
                eventType = "m.reaction",
                from = from,
                limit = PAGE_SIZE,
            ).getOrThrow()

            vetoers += response.chunk.mapNotNull { event ->
                val content = event.content as? ReactionEventContent ?: return@mapNotNull null
                val key = content.relatesTo?.key ?: return@mapNotNull null
                if (isVetoEmoji(key)) event.sender.localpart else null
            }

            from = response.end ?: return vetoers.distinct()
        }
        return vetoers.distinct()
    }
}
