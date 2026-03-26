package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.SynapseTestClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
internal class BotCommandsTest {

    @Inject
    lateinit var synapseTestClient: SynapseTestClient

    @Inject
    lateinit var wireMock: WireMock

    private lateinit var roomId: String

    @BeforeEach
    fun setUp() {
        roomId = synapseTestClient.createRoom("bot-test-${System.nanoTime()}")
        synapseTestClient.inviteUser(roomId, "@johnnybot:localhost")
        wireMock.resetMappings()
    }

    @Test
    fun `ping command should respond with Pong`() {
        synapseTestClient.sendMessage(roomId, "!bot ping")

        val response = synapseTestClient.waitForBotMessage(roomId)
        assertThat(response.get("body").asText()).isEqualTo("Pong!")

        val reaction = synapseTestClient.waitForReaction(roomId)
        assertThat(reaction).isNotNull
    }

    @Test
    fun `help command should list available commands`() {
        synapseTestClient.sendMessage(roomId, "!bot help")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("ping")
        assertThat(body).contains("help")
        assertThat(body).contains("skong")
        assertThat(body).contains("who-watched")
        assertThat(body).contains("top-watched")
        assertThat(body).contains("top-watchers")
    }

    @Test
    fun `skong believer should respond with patience message`() {
        synapseTestClient.sendMessage(roomId, "!bot skong believer")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("Patience, my child")
    }

    @Test
    fun `skong doubter should respond with days count`() {
        synapseTestClient.sendMessage(roomId, "!bot skong doubter")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("days")
        assertThat(body).contains("no release date")
    }

    @Test
    fun `skong invalid parameter should respond with error`() {
        synapseTestClient.sendMessage(roomId, "!bot skong invalid")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("An error occurred")
    }

    @Test
    fun `who-watched should return watcher info`() {
        stubJellyfinSearch()
        stubJellystatItemHistory()

        synapseTestClient.sendMessage(roomId, "!bot who-watched Breaking Bad")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("Who watched last episode of Breaking Bad")
        assertThat(body).contains("alice")
    }

    @Test
    fun `top-watched should return top watched media`() {
        stubJellystatMostPopular("Series", """[{"unique_viewers": "5", "Name": "The Bear"}]""")
        stubJellystatMostPopular("Movie", """[{"unique_viewers": "3", "Name": "Dune"}]""")
        stubJellystatMostViewed("Series", """[{"Plays": "10", "total_playback_duration": "36000", "Name": "The Bear"}]""")
        stubJellystatMostViewed("Movie", """[{"Plays": "8", "total_playback_duration": "18000", "Name": "Dune"}]""")

        synapseTestClient.sendMessage(roomId, "!bot top-watched last-week")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("Top watch")
        assertThat(body).contains("The Bear")
    }

    @Test
    fun `top-watchers should return top watchers list`() {
        stubJellystatUserActivity()

        synapseTestClient.sendMessage(roomId, "!bot top-watchers")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("Top ten watchers")
        assertThat(body).contains("alice")
        assertThat(body).contains("bob")
    }

    @Test
    fun `deadoo regex command should respond to c'est comment`() {
        synapseTestClient.sendMessage(roomId, "c'est comment")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        // The response is a random deado variant, just check it's not empty
        assertThat(body).isNotBlank()
    }

    // WireMock stub helpers

    private fun stubJellyfinSearch() {
        wireMock.register(
            get(urlPathEqualTo("/Search/Hints"))
                .withQueryParam("searchTerm", equalTo("Breaking Bad"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"SearchHints": [{"ItemId": "abc123", "Name": "Breaking Bad", "Type": "Series"}], "TotalRecordCount": 1}""")
                )
        )
    }

    private fun stubJellystatItemHistory() {
        wireMock.register(
            post(urlPathEqualTo("/api/getItemHistory"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                            "current_page": 1, "pages": 1, "size": 50, "sort": "", "desc": true,
                            "results": [
                                {"UserName": "alice", "EpisodeNumber": 1, "SeasonNumber": 5, "FullName": "S05E01 - Live Free or Die"}
                            ]
                        }"""
                        )
                )
        )
    }

    private fun stubJellystatMostPopular(type: String, responseBody: String) {
        wireMock.register(
            post(urlPathEqualTo("/stats/getMostPopularByType"))
                .withRequestBody(matchingJsonPath("$.type", equalTo(type)))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )
    }

    private fun stubJellystatMostViewed(type: String, responseBody: String) {
        wireMock.register(
            post(urlPathEqualTo("/stats/getMostViewedByType"))
                .withRequestBody(matchingJsonPath("$.type", equalTo(type)))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)
                )
        )
    }

    private fun stubJellystatUserActivity() {
        wireMock.register(
            get(urlPathEqualTo("/stats/getAllUserActivity"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """[
                            {"UserName": "alice", "TotalPlays": "50", "TotalWatchTime": "180000"},
                            {"UserName": "bob", "TotalPlays": "30", "TotalWatchTime": "90000"}
                        ]"""
                        )
                )
        )
    }
}
