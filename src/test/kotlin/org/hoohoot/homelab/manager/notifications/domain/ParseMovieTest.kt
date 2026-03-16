package org.hoohoot.homelab.manager.notifications.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.notifications.Movie
import org.hoohoot.homelab.manager.notifications.RadarrWebhookPayload
import org.junit.jupiter.api.Test

class ParseMovieTest {

    private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    @Test
    fun `from should parse movie payload correctly when all fields are present`() {
        val payload = mapper.readValue<RadarrWebhookPayload>(
            """
            {
                "movie": {
                    "title": "Inception",
                    "year": 2010,
                    "imdbId": "tt1375666",
                    "tags": ["Action", "Sci-Fi", "1 - john_doe"]
                },
                "movieFile": {
                    "quality": "1080p"
                }
            }
            """
        )

        val movie = Movie.from(payload)

        assertThat(movie).isNotNull
        assertThat(movie.title).isEqualTo("Inception")
        assertThat(movie.year).isEqualTo("2010")
        assertThat(movie.imdbId).isEqualTo("tt1375666")
        assertThat(movie.quality).isEqualTo("1080p")
        assertThat(movie.requester).isEqualTo("john_doe")
    }

    @Test
    fun `from should handle missing movie object`() {
        val payload = mapper.readValue<RadarrWebhookPayload>(
            """
            {
                "movieFile": {
                    "quality": "720p"
                }
            }
            """
        )

        val movie = Movie.from(payload)

        assertThat(movie).isNotNull
        assertThat(movie.title).isEqualTo("unknown")
        assertThat(movie.year).isEqualTo("unknown")
        assertThat(movie.imdbId).isEqualTo("unknown")
        assertThat(movie.quality).isEqualTo("720p")
        assertThat(movie.requester).isEqualTo("unknown")
    }

    @Test
    fun `from should handle missing movieFile object`() {
        val payload = mapper.readValue<RadarrWebhookPayload>(
            """
            {
                "movie": {
                    "title": "Avatar",
                    "year": 2009,
                    "imdbId": "tt0499549",
                    "tags": ["Adventure", "1 - jane_doe"]
                }
            }
            """
        )

        val movie = Movie.from(payload)

        assertThat(movie).isNotNull
        assertThat(movie.title).isEqualTo("Avatar")
        assertThat(movie.year).isEqualTo("2009")
        assertThat(movie.imdbId).isEqualTo("tt0499549")
        assertThat(movie.quality).isEqualTo("unknown")
        assertThat(movie.requester).isEqualTo("jane_doe")
    }

    @Test
    fun `from should handle missing tags in movie object`() {
        val payload = mapper.readValue<RadarrWebhookPayload>(
            """
            {
                "movie": {
                    "title": "Titanic",
                    "year": 1997,
                    "imdbId": "tt0120338"
                },
                "movieFile": {
                    "quality": "4K"
                }
            }
            """
        )

        val movie = Movie.from(payload)

        assertThat(movie).isNotNull
        assertThat(movie.title).isEqualTo("Titanic")
        assertThat(movie.year).isEqualTo("1997")
        assertThat(movie.imdbId).isEqualTo("tt0120338")
        assertThat(movie.quality).isEqualTo("4K")
        assertThat(movie.requester).isEqualTo("unknown")
    }

    @Test
    fun `from should handle empty payload`() {
        val payload = mapper.readValue<RadarrWebhookPayload>("{}")

        val movie = Movie.from(payload)

        assertThat(movie).isNotNull
        assertThat(movie.title).isEqualTo("unknown")
        assertThat(movie.year).isEqualTo("unknown")
        assertThat(movie.imdbId).isEqualTo("unknown")
        assertThat(movie.quality).isEqualTo("unknown")
        assertThat(movie.requester).isEqualTo("unknown")
    }
}
