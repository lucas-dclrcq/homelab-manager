package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.ContentType
import jakarta.ws.rs.core.Response
import net.minidev.json.annotate.JsonIgnore
import org.awaitility.Awaitility
import org.hoohoot.homelab.manager.it.config.InjectWireMock
import org.hoohoot.homelab.manager.it.config.WiremockTestResource
import org.hoohoot.homelab.manager.infrastructure.api.resources.NotificationsResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.TimeUnit

@QuarkusTest
@TestHTTPEndpoint(NotificationsResource::class)
@QuarkusTestResource(WiremockTestResource::class)
internal class SeriesNotificationsTest {
    @InjectWireMock
    private val wireMockServer: WireMockServer? = null

    @BeforeEach
    fun setup() {
        wireMockServer!!.resetAll()
        wireMockServer
            .stubFor(
                WireMock.put(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                    .willReturn(
                        WireMock.aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withStatus(200)
                            .withBody("{\"event_id\": \"${UUID.randomUUID()}\"}")
                    )
            )
    }

    @Test
    fun `should send series episode downloaded notification`() {
        val notification = """
                {
                 	"series": {
                 		"id": 301,
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
                 			"episodeNumber": 6,
                 			"seasonNumber": 12,
                 			"title": "Episode 6",
                 			"overview": "There's a bit of a pest problem in one tribe. And has someone finally found peace, or will their reckless behaviour come back to haunt them? As alliances are tested, who will be going home tonight?",
                 			"airDate": "2025-02-25",
                 			"airDateUtc": "2025-02-25T08:30:00Z",
                 			"seriesId": 301,
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

        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/sonarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.verify(
            1, WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withRequestBody(
                    WireMock.equalToJson(
                        """
                            {
                              "msgtype" : "m.text",
                              "body" : "Episode Downloaded\nSeries : Australian Survivor [https://www.imdb.com/title/tt0310416/]\nEpisode : S12E06 - Episode 6 [WEBDL-720p]\nSeries requested by : flo\nSource : SABnzbd (NZBFinder)",
                              "format" : "org.matrix.custom.html",
                              "formatted_body" : "<h1>Episode Downloaded</h1><p>Series : Australian Survivor [https://www.imdb.com/title/tt0310416/]<br>Episode : S12E06 - Episode 6 [WEBDL-720p]<br>Series requested by : flo<br>Source : SABnzbd (NZBFinder)</p>",
                              "m.relates_to" : null
                            }
                        """.trimIndent()
                    )
                )
        )
    }
//
//    @Test
//    fun `should send series episode downloaded notification in previous series thread`() {
//        val notification = """
//                {
//                 	"series": {
//                 		"id": 301,
//                 		"title": "Australian Survivor",
//                 		"titleSlug": "australian-survivor",
//                 		"path": "/media/TV/Australian Survivor (2002) [tvdbid-303904]",
//                 		"tvdbId": 303904,
//                 		"tvMazeId": 6796,
//                 		"tmdbId": 10957,
//                 		"imdbId": "tt0310417",
//                 		"type": "standard",
//                 		"year": 2002,
//                 		"genres": [
//                 			"Adventure",
//                 			"Game Show",
//                 			"Reality"
//                 		],
//                 		"images": [
//                 			{
//                 				"coverType": "banner",
//                 				"url": "/MediaCover/301/banner.jpg?lastWrite=638702369550108508",
//                 				"remoteUrl": "https://artworks.thetvdb.com/banners/graphical/303904-g.jpg"
//                 			},
//                 			{
//                 				"coverType": "poster",
//                 				"url": "/MediaCover/301/poster.jpg?lastWrite=638702369550308510",
//                 				"remoteUrl": "https://artworks.thetvdb.com/banners/posters/303904-4.jpg"
//                 			},
//                 			{
//                 				"coverType": "fanart",
//                 				"url": "/MediaCover/301/fanart.jpg?lastWrite=638702369550548512",
//                 				"remoteUrl": "https://artworks.thetvdb.com/banners/fanart/original/303904-5.jpg"
//                 			},
//                 			{
//                 				"coverType": "clearlogo",
//                 				"url": "/MediaCover/301/clearlogo.png?lastWrite=638702369550708513",
//                 				"remoteUrl": "https://artworks.thetvdb.com/banners/v4/series/303904/clearlogo/611c8c2d6b319.png"
//                 			}
//                 		],
//                 		"tags": [
//                 			"11 - flo",
//                 			"hoohoot",
//                 			"usenet",
//                 			"ygg"
//                 		],
//                 		"originalLanguage": {
//                 			"id": 1,
//                 			"name": "English"
//                 		}
//                 	},
//                 	"episodes": [
//                 		{
//                 			"id": 20905,
//                 			"episodeNumber": 6,
//                 			"seasonNumber": 12,
//                 			"title": "Episode 6",
//                 			"overview": "There's a bit of a pest problem in one tribe. And has someone finally found peace, or will their reckless behaviour come back to haunt them? As alliances are tested, who will be going home tonight?",
//                 			"airDate": "2025-02-25",
//                 			"airDateUtc": "2025-02-25T08:30:00Z",
//                 			"seriesId": 301,
//                 			"tvdbId": 10958514
//                 		}
//                 	],
//                 	"episodeFile":
//                 		{
//                 			"id": 13457,
//                 			"relativePath": "Season 12/Australian Survivor (2002) - S12E06 - Episode 6 [WEBDL-720p][AAC 2.0][h264]-WH.mkv",
//                 			"path": "/media/TV/Australian Survivor (2002) [tvdbid-303904]/Season 12/Australian Survivor (2002) - S12E06 - Episode 6 [WEBDL-720p][AAC 2.0][h264]-WH.mkv",
//                 			"quality": "WEBDL-720p",
//                 			"qualityVersion": 1,
//                 			"releaseGroup": "WH",
//                 			"sceneName": "Australian.Survivor.S12E06.720p.WEB-DL.AAC2.0.H.264-WH",
//                 			"size": 1615646298,
//                 			"dateAdded": "2025-02-25T11:37:00.2308889Z",
//                 			"languages": [
//                 				{
//                 					"id": 1,
//                 					"name": "English"
//                 				}
//                 			],
//                 			"mediaInfo": {
//                 				"audioChannels": 2,
//                 				"audioCodec": "AAC",
//                 				"audioLanguages": [
//                 					"eng"
//                 				],
//                 				"height": 720,
//                 				"width": 1280,
//                 				"subtitles": [
//                 					"eng"
//                 				],
//                 				"videoCodec": "h264",
//                 				"videoDynamicRange": "",
//                 				"videoDynamicRangeType": ""
//                 			}
//                 		},
//                 	"downloadClient": "SABnzbd",
//                 	"downloadClientType": "SABnzbd",
//                 	"downloadId": "SABnzbd_nzo__sf78tpj",
//                 	"release": {
//                 		"releaseTitle": "Australian.Survivor.S12E06.720p.WEB-DL.AAC2.0.H.264-WH",
//                 		"indexer": "NZBFinder (Prowlarr)",
//                 		"size": 1834868309,
//                 		"releaseType": "singleEpisode"
//                 	},
//                 	"fileCount": 1,
//                 	"sourcePath": "/media/Downloads/sabnzbd/complete/Australian.Survivor.S12E06.720p.WEB-DL.AAC2.0.H.264-WH/",
//                 	"destinationPath": "/media/TV/Australian Survivor (2002) [tvdbid-303904]/Season 12",
//                 	"eventType": "Download",
//                 	"instanceName": "Sonarr",
//                 	"applicationUrl": ""
//                 }
//
//                """.trimIndent()
//
//        RestAssured.given().contentType(ContentType.JSON).body(notification)
//            .and().header("X-Api-Key", "secureapikey")
//            .`when`().post("/incoming/sonarr")
//            .then().statusCode(Response.Status.NO_CONTENT.statusCode)
//
//        Awaitility.await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
//            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }
//
//        wireMockServer!!.resetAll()
//
//        RestAssured.given().contentType(ContentType.JSON).body(notification)
//            .and().header("X-Api-Key", "secureapikey")
//            .`when`().post("/incoming/sonarr")
//            .then().statusCode(Response.Status.NO_CONTENT.statusCode)
//
//        wireMockServer!!.verify(
//            1, WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
//                .withRequestBody(
//                    WireMock.equalToJson(
//                        """
//                            {
//                              "msgtype" : "m.text",
//                              "body" : "Episode Downloaded\nSeries : Australian Survivor [https://www.imdb.com/title/tt0310416/]\nEpisode : S12E06 - Episode 6 [WEBDL-720p]\nSeries requested by : flo\nSource : SABnzbd (NZBFinder)",
//                              "format" : "org.matrix.custom.html",
//                              "formatted_body" : "<h1>Episode Downloaded</h1><p>Series : Australian Survivor [https://www.imdb.com/title/tt0310417/]<br>Episode : S12E06 - Episode 6 [WEBDL-720p]<br>Series requested by : flo<br>Source : SABnzbd (NZBFinder)</p>",
//                              "m.relates_to" : null
//                            }
//                        """.trimIndent()
//                    )
//                )
//        )
//    }

    @Test
    fun `should send notification to configured room`() {
        val notification = """
                {
                 	"series": {
                 		"id": 301,
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
                 			"episodeNumber": 6,
                 			"seasonNumber": 12,
                 			"title": "Episode 6",
                 			"overview": "There's a bit of a pest problem in one tribe. And has someone finally found peace, or will their reckless behaviour come back to haunt them? As alliances are tested, who will be going home tonight?",
                 			"airDate": "2025-02-25",
                 			"airDateUtc": "2025-02-25T08:30:00Z",
                 			"seriesId": 301,
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

        RestAssured.given().contentType(ContentType.JSON).body(notification)
            .and().header("X-Api-Key", "secureapikey")
            .`when`().post("/incoming/sonarr")
            .then().statusCode(Response.Status.NO_CONTENT.statusCode)

        Awaitility.await().atMost(500, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS)
            .until { wireMockServer!!.serveEvents.requests.isNotEmpty() }

        wireMockServer!!.verify(
            1,
            WireMock.putRequestedFor(WireMock.urlMatching("/_matrix/client/r0/rooms/!media:test-server.tld/send/m.room.message/.*"))
        )
    }
}