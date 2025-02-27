package org.hoohoot.homelab.manager.notifications.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hoohoot.homelab.manager.notifications.domain.EmptyNotificationException
import org.hoohoot.homelab.manager.notifications.domain.Notification
import org.hoohoot.homelab.manager.notifications.domain.NotificationBuilder
import org.junit.jupiter.api.Test

class NotificationBuilderTest {
    @Test
    fun `should prevent building an empty notification`() {
        // ARRANGE
        val notificationBuilder = NotificationBuilder()

        // ACT
        // ASSERT
        assertThatThrownBy { notificationBuilder.buildNotification() }
            .isInstanceOf(EmptyNotificationException::class.java)
            .hasMessage("Notification cannot be empty. It should have at least a title or an info line")
    }

    @Test
    fun `should build a notification with just a title`() {
        // ARRANGE
        val notificationBuilder = NotificationBuilder()

        // ACT
        val notification = notificationBuilder
            .addTitle("title")
            .buildNotification()

        // ASSERT
        assertThat(notification).isEqualTo(Notification("title", "<h1>title</h1>"))
    }

    @Test
    fun `should build a notification with a title and one info line`() {
        // ARRANGE
        val notificationBuilder = NotificationBuilder()

        // ACT
        val notification = notificationBuilder
            .addTitle("title")
            .addInfoLine("info line")
            .buildNotification()

        // ASSERT
        assertThat(notification).isEqualTo(Notification("title\ninfo line", "<h1>title</h1><p>info line</p>"))
    }

    @Test
    fun `should build a notification with a title and two info lines`() {
        // ARRANGE
        val notificationBuilder = NotificationBuilder()

        // ACT
        val notification = notificationBuilder
            .addTitle("title")
            .addInfoLine("info line")
            .addInfoLine("another info line")
            .buildNotification()

        // ASSERT
        assertThat(notification).isEqualTo(Notification("title\ninfo line\nanother info line", "<h1>title</h1><p>info line<br>another info line</p>"))
    }

    @Test
    fun `should build a notification with a title and three info lines`() {
        // ARRANGE
        val notificationBuilder = NotificationBuilder()

        // ACT
        val notification = notificationBuilder
            .addTitle("title")
            .addInfoLine("info line")
            .addInfoLine("another info line")
            .addInfoLine("encore")
            .buildNotification()

        // ASSERT
        assertThat(notification).isEqualTo(Notification("title\ninfo line\nanother info line\nencore", "<h1>title</h1><p>info line<br>another info line<br>encore</p>"))
    }
}


