package org.hoohoot.homelab.manager.it

import io.agroal.api.AgroalDataSource
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.it.config.InjectSynapse
import org.hoohoot.homelab.manager.it.config.SynapseClient
import org.hoohoot.homelab.manager.it.config.SynapseTestResource
import org.hoohoot.homelab.manager.it.config.WiremockTestResource
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomProvider
import org.hoohoot.homelab.manager.notifications.resource.SonarrResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@QuarkusTest
@TestHTTPEndpoint(SonarrResource::class)
@QuarkusTestResource(WiremockTestResource::class)
@QuarkusTestResource(SynapseTestResource::class)
internal class SeriesNotificationsTest {
    @InjectSynapse
    private val synapseClient: SynapseClient? = null

    @Inject
    lateinit var dataSource: AgroalDataSource

    @Inject
    lateinit var roomProvider: MatrixRoomProvider

    private lateinit var mediaRoomId: String

    @BeforeEach
    fun setUp() {
        mediaRoomId = synapseClient!!.createRoom("media-${System.nanoTime()}")
        roomProvider.media = mediaRoomId
    }

    private fun notification(seriesId: Int = 301, episodeNumber: Int = 6) = """
        {
         	"series": {
         		"id": $seriesId,
         		"title": "Australian Survivor",
         		"titleSlug": "australian-survivor",
         		"path": "/media/TV/Australian Survivor (2002) [tvdbid-303904]",
         		"tvdbId": 303904,
         		"tvMazeId": 6796,
         		"tmdbId": 10957,
         		"imdbId": "tt0310416",
         		"type": "standard",
         		"year": 2002,
         		"genres": [
         			"Adventure",
         			"Game Show",
         			"Reality"
         		],
         		"images": [
         			{
         				"coverType": "banner",
         				"url": "/MediaCover/301/banner.jpg?lastWrite=638702369550108508",
         				"remoteUrl": "https://artworks.thetvdb.com/banners/graphical/303904-g.jpg"
         			},
         			{
         				"coverType": "poster",
         				"url": "/MediaCover/301/poster.jpg?lastWrite=638702369550308510",
         				"remoteUrl": "https://artworks.thetvdb.com/banners/posters/303904-4.jpg"
         			},
         			{
         				"coverType": "fanart",
         				"url": "/MediaCover/301/fanart.jpg?lastWrite=638702369550548512",
         				"remoteUrl": "https://artworks.thetvdb.com/banners/fanart/original/303904-5.jpg"
         			},
         			{
         				"coverType": "clearlogo",
         				"url": "/MediaCover/301/clearlogo.png?lastWrite=638702369550708513",
         				"remoteUrl": "https://artworks.thetvdb.com/banners/v4/series/303904/clearlogo/611c8c2d6b319.png"
         			}
         		],
         		"tags": [
         			"11 - flo",
         			"hoohoot",
         			"usenet",
         			"ygg"
         		],
         		"originalLanguage": {
         			"id": 1,
         			"name": "English"
         		}
         	},
         	"episodes": [
         		{
         			"id": 20905,
         			"episodeNumber": $episodeNumber,
         			"seasonNumber": 12,
         			"title": "Episode $episodeNumber",
         			"overview": "Some overview",
         			"airDate": "2025-02-25",
         			"airDateUtc": "2025-02-25T08:30:00Z",
         			"seriesId": $seriesId,
         			"tvdbId": 10958514
         		}
         	],
         	"episodeFile":
         		{
         			"id": 13457,
         			"relativePath": "Season 12/Australian Survivor (2002) - S12E06 - Episode 6 [WEBDL-720p][AAC 2.0][h264]-WH.mkv",
         			"path": "/media/TV/Australian Survivor (2002) [tvdbid-303904]/Season 12/Australian Survivor (2002) - S12E06 - Episode 6 [WEBDL-720p][AAC 2.0][h264]-WH.mkv",
         			"quality": "WEBDL-720p",
         			"qualityVersion": 1,
         			"releaseGroup": "WH",
         			"sceneName": "Australian.Survivor.S12E06.720p.WEB-DL.AAC2.0.H.264-WH",
         			"size": 1615646298,
         			"dateAdded": "2025-02-25T11:37:00.2308889Z",
         			"languages": [
         				{
         					"id": 1,
         					"name": "English"
         				}
         			],
         			"mediaInfo": {
         				"audioChannels": 2,
         				"audioCodec": "AAC",
         				"audioLanguages": [
         					"eng"
         				],
         				"height": 720,
         				"width": 1280,
         				"subtitles": [
         					"eng"
         				],
         				"videoCodec": "h264",
         				"videoDynamicRange": "",
         				"videoDynamicRangeType": ""
         			}
         		},
         	"downloadClient": "SABnzbd",
         	"downloadClientType": "SABnzbd",
         	"downloadId": "SABnzbd_nzo__sf78tpj",
         	"release": {
         		"releaseTitle": "Australian.Survivor.S12E06.720p.WEB-DL.AAC2.0.H.264-WH",
         		"indexer": "NZBFinder (Prowlarr)",
         		"size": 1834868309,
         		"releaseType": "singleEpisode"
         	},
         	"fileCount": 1,
         	"sourcePath": "/media/Downloads/sabnzbd/complete/Australian.Survivor.S12E06.720p.WEB-DL.AAC2.0.H.264-WH/",
         	"destinationPath": "/media/TV/Australian Survivor (2002) [tvdbid-303904]/Season 12",
         	"eventType": "Download",
         	"instanceName": "Sonarr",
         	"applicationUrl": ""
         }
    """.trimIndent()

    @Test
    fun `should send series episode downloaded notification`() {
        RestAssured.given().contentType(ContentType.JSON).body(notification())
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseClient!!.getLastMessage(mediaRoomId)
        assertThat(lastMessage.get("msgtype").asText()).isEqualTo("m.text")
        assertThat(lastMessage.get("body").asText()).isEqualTo(
            "📺 Episode Downloaded\n📡 Series : Australian Survivor [https://www.imdb.com/title/tt0310416/]\n🎞️ Episode : S12E06 - Episode 6 [WEBDL-720p]\n👤 Series requested by : flo\n📥 Source : SABnzbd (NZBFinder)"
        )
        assertThat(lastMessage.get("formatted_body").asText()).isEqualTo(
            "<h1>📺 Episode Downloaded</h1><p>📡 Series : Australian Survivor [https://www.imdb.com/title/tt0310416/]<br>🎞️ Episode : S12E06 - Episode 6 [WEBDL-720p]<br>👤 Series requested by : flo<br>📥 Source : SABnzbd (NZBFinder)</p>"
        )
    }

    @Test
    fun `should thread second episode notification under first for same series`() {
        RestAssured.given().contentType(ContentType.JSON).body(notification(seriesId = 500, episodeNumber = 1))
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val firstEventId = synapseClient!!.getLastMessageEvent(mediaRoomId).get("event_id").asText()

        RestAssured.given().contentType(ContentType.JSON).body(notification(seriesId = 500, episodeNumber = 2))
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseClient.getLastMessage(mediaRoomId)
        assertThat(lastMessage.get("m.relates_to").get("event_id").asText()).isEqualTo(firstEventId)
        assertThat(lastMessage.get("m.relates_to").get("rel_type").asText()).isEqualTo("m.thread")
    }

    @Test
    fun `should send standalone notification when last notification was more than 24h ago`() {
        RestAssured.given().contentType(ContentType.JSON).body(notification(seriesId = 600, episodeNumber = 1))
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        // Simulate 48h+ elapsed and trigger cleanup (cron job deletes entries older than 48h)
        dataSource.connection.use { conn ->
            conn.prepareStatement("UPDATE media_notification_thread SET last_notified_at = ? WHERE media_id = '600' AND media_type = 'series'").use { stmt ->
                stmt.setObject(1, LocalDateTime.now().minusHours(49))
                stmt.executeUpdate()
            }
            conn.prepareStatement("DELETE FROM media_notification_thread WHERE last_notified_at < ?").use { stmt ->
                stmt.setObject(1, LocalDateTime.now().minusHours(48))
                stmt.executeUpdate()
            }
        }

        RestAssured.given().contentType(ContentType.JSON).body(notification(seriesId = 600, episodeNumber = 2))
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post()
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        val lastMessage = synapseClient!!.getLastMessage(mediaRoomId)
        assertThat(lastMessage.get("m.relates_to")).isNull()
    }
}
