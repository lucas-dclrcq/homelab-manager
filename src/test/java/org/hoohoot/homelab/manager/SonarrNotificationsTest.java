package org.hoohoot.homelab.manager;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hoohoot.homelab.manager.config.InjectWireMock;
import org.hoohoot.homelab.manager.config.WiremockTestResource;
import org.hoohoot.homelab.manager.notifications.NotificationsResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

@QuarkusTest
@TestHTTPEndpoint(NotificationsResource.class)
@QuarkusTestResource(WiremockTestResource.class)
class SonarrNotificationsTest {
    @InjectWireMock
    private WireMockServer wireMockServer;

    @BeforeEach
    public void setup() {
        wireMockServer.resetAll();
        wireMockServer
                .stubFor(put(urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                        .willReturn(aResponse().withStatus(200)));
    }

    @Test
    void shouldSendMovieDownloadedNotification() {
        var notification = """
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
                 	"episodeFiles": [
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
                 		}
                 	],
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
                """;

        given().contentType(ContentType.JSON).body(notification)
                .when().post("sonarr")
                .then().statusCode(200);

        await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).until(() -> !wireMockServer.getServeEvents().getRequests().isEmpty());

        wireMockServer.verify(1, putRequestedFor(urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withRequestBody(equalToJson("""
                        {
                          "msgtype": "m.text",
                          "body": "<h1>Episode Downloaded</h1><p>Australian Survivor - 12x6 - Episode 6 [WEBDL-720p]<br>Requested by : flo<br>Source: SABnzbd (NZBFinder)</p>",
                          "format": "org.matrix.custom.html",
                          "formatted_body": "<h1>Episode Downloaded</h1><p>Australian Survivor - 12x6 - Episode 6 [WEBDL-720p]<br>Requested by : flo<br>Source: SABnzbd (NZBFinder)</p>"
                        }
                        """)));
    }

    @Test
    void shouldSendNotificationToConfiguredRoom() {
        var notification = """
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
                 	"episodeFiles": [
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
                 		}
                 	],
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
                """;

        given().contentType(ContentType.JSON).body(notification)
                .when().post("sonarr")
                .then().statusCode(200);

        await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).until(() -> !wireMockServer.getServeEvents().getRequests().isEmpty());

        wireMockServer.verify(1, putRequestedFor(urlMatching("/_matrix/client/r0/rooms/!sonarr:test-server.tld/send/m.room.message/.*")));
    }
}