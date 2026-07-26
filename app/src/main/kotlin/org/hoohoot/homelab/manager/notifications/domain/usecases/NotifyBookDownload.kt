package org.hoohoot.homelab.manager.notifications.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.domain.NotificationRoom
import org.hoohoot.homelab.manager.notifications.domain.notificationMessage
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationSender

data class BookDownload(
    val title: String,
    val extension: String,
    val requestedBy: String,
    val size: String,
    val succeeded: Boolean,
    val errorMessage: String?,
)

@ApplicationScoped
class NotifyBookDownload(private val sender: NotificationSender) {
    suspend operator fun invoke(book: BookDownload) {
        val message = if (book.succeeded) {
            notificationMessage(
                "📚 Ebook téléchargé",
                listOf(
                    "${book.title} (${book.extension})",
                    "👤 Demandé par : ${book.requestedBy}",
                    "💾 Taille : ${book.size}",
                ),
            )
        } else {
            notificationMessage(
                "❌ Échec du téléchargement",
                listOf(
                    book.title,
                    "👤 Demandé par : ${book.requestedBy}",
                    "⚠️ ${book.errorMessage ?: "erreur inconnue"}",
                ),
            )
        }
        sender.send(NotificationRoom.BOOKS, message)
    }
}
