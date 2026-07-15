package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.it.config.CleanupSeed
import org.hoohoot.homelab.manager.it.config.PlaybackSessionSeed
import org.hoohoot.homelab.manager.it.config.SynapseTestClient
import org.hoohoot.homelab.manager.statistics.domain.MediaType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@QuarkusTest
internal class BotCommandsTest {

    @Inject
    lateinit var synapseTestClient: SynapseTestClient

    @Inject
    lateinit var wireMock: WireMock

    @ConfigProperty(name = "matrix.bot.prefix")
    lateinit var botPrefix: String

    private lateinit var roomId: String

    @BeforeEach
    fun setUp() {
        roomId = synapseTestClient.createRoom("bot-test-${System.nanoTime()}")
        synapseTestClient.inviteUser(roomId, "@johnnybot:localhost")
        wireMock.resetMappings()
    }

    @Test
    fun `ping command should respond with Pong`() {
        synapseTestClient.sendMessage(roomId, "!$botPrefix ping")

        val response = synapseTestClient.waitForBotMessage(roomId)
        assertThat(response.get("body").asText()).isEqualTo("Pong!")

        val reaction = synapseTestClient.waitForReaction(roomId)
        assertThat(reaction).isNotNull
    }

    @Test
    fun `help command should list available commands`() {
        synapseTestClient.sendMessage(roomId, "!$botPrefix help")

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
        synapseTestClient.sendMessage(roomId, "!$botPrefix skong believer")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("Patience, my child")
    }

    @Test
    fun `skong doubter should respond with days count`() {
        synapseTestClient.sendMessage(roomId, "!$botPrefix skong doubter")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("days")
        assertThat(body).contains("no release date")
    }

    @Test
    fun `skong invalid parameter should respond with error`() {
        synapseTestClient.sendMessage(roomId, "!$botPrefix skong invalid")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("An error occurred")
    }

    @Test
    fun `who-watched should return watcher info`() {
        stubJellyfinSearch()
        PlaybackSessionSeed.insertSession(
            userName = "alice",
            userId = "bot-alice",
            itemId = "bb-s05e01",
            itemName = "S05E01 - Live Free or Die",
            mediaType = MediaType.EPISODE,
            seriesId = "abc123",
            seriesName = "Breaking Bad",
            seasonNumber = 5,
            episodeNumber = 1,
        )

        synapseTestClient.sendMessage(roomId, "!$botPrefix who-watched Breaking Bad")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("Who watched last episode of Breaking Bad")
        assertThat(body).contains("alice")
    }

    @Test
    fun `top-watched should return top watched media`() {
        repeat(3) { viewer ->
            PlaybackSessionSeed.insertSession(
                userName = "bear-viewer-$viewer",
                itemId = "the-bear-s01e01",
                itemName = "System",
                mediaType = MediaType.EPISODE,
                seriesId = "the-bear",
                seriesName = "The Bear",
                seasonNumber = 1,
                episodeNumber = 1,
            )
        }
        PlaybackSessionSeed.insertSession(userName = "bear-viewer-0", itemName = "Dune", mediaType = MediaType.MOVIE)

        synapseTestClient.sendMessage(roomId, "!$botPrefix top-watched last-week")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("Top watch")
        assertThat(body).contains("The Bear")
    }

    @Test
    fun `top-watchers should return top watchers list`() {
        PlaybackSessionSeed.insertSession(userName = "alice", userId = "bot-alice", itemName = "Marathon Movie", mediaType = MediaType.MOVIE, durationSeconds = 180000)
        PlaybackSessionSeed.insertSession(userName = "bob", userId = "bot-bob", itemName = "Marathon Movie", mediaType = MediaType.MOVIE, durationSeconds = 90000)

        synapseTestClient.sendMessage(roomId, "!$botPrefix top-watchers")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).contains("Top ten watchers")
        assertThat(body).contains("alice")
        assertThat(body).contains("bob")
    }

    @Test
    fun `garde command should protect a matching cleanup candidate`() {
        CleanupSeed.deleteAll()
        val campaignId = CleanupSeed.insertCampaign()
        val candidateId = CleanupSeed.insertCandidate(campaignId, "Inception", radarrMovieId = 101)

        synapseTestClient.sendMessage(roomId, "!$botPrefix garde Inception")

        val response = synapseTestClient.waitForBotMessage(roomId)
        assertThat(response.get("body").asText()).contains("Inception")
        assertThat(CleanupSeed.candidateStatus(candidateId))
            .isEqualTo(CleanupCandidateEntity.STATUS_PROTECTED)
    }

    @Test
    fun `garde command without active campaign should say there is nothing to keep`() {
        CleanupSeed.deleteAll()

        synapseTestClient.sendMessage(roomId, "!$botPrefix garde Inception")

        val response = synapseTestClient.waitForBotMessage(roomId)
        assertThat(response.get("body").asText()).contains("Aucune campagne")
    }

    @Test
    fun `garde command with an ambiguous title should list the matching medias`() {
        CleanupSeed.deleteAll()
        val campaignId = CleanupSeed.insertCampaign()
        CleanupSeed.insertCandidate(campaignId, "Batman Begins", radarrMovieId = 401)
        CleanupSeed.insertCandidate(campaignId, "Batman Returns", radarrMovieId = 402)

        synapseTestClient.sendMessage(roomId, "!$botPrefix garde Batman")

        val response = synapseTestClient.waitForBotMessage(roomId)
        assertThat(response.get("body").asText()).contains("Plusieurs médias")
    }

    @Test
    fun `deadoo regex command should respond to c'est comment`() {
        synapseTestClient.sendMessage(roomId, "c'est comment")

        val response = synapseTestClient.waitForBotMessage(roomId)
        val body = response.get("body").asText()
        assertThat(body).isNotBlank()
    }

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

}
