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
class LidarrNotificationsTest {
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
                	"artist": {
                		"id": 188,
                		"name": "General Elektriks",
                		"disambiguation": "",
                		"path": "/media/Music/General Elektriks",
                		"mbId": "e9ec4424-2d6f-4332-b969-6c07caedf371",
                		"type": "Person",
                		"overview": "General Elektriks is the musical project of French keyboard player, composer, singer, and producer Hervé Salters. He also uses the moniker RV, a contraction of the pronunciation of his first name in French.\\n\\n",
                		"genres": [
                			"Alternative Rock",
                			"Electronic",
                			"Funk",
                			"Jazz"
                		],
                		"images": [
                			{
                				"coverType": "fanart",
                				"url": "/MediaCover/188/fanart.jpg?lastWrite=635338470670000000",
                				"remoteUrl": "https://imagecache.lidarr.audio/v1/tadb/artist/fanart/qqxwxv1395373279.jpg"
                			},
                			{
                				"coverType": "poster",
                				"url": "/MediaCover/188/poster.jpg?lastWrite=635671935840000000",
                				"remoteUrl": "https://imagecache.lidarr.audio/v1/tadb/artist/thumb/twytqr1395373211.jpg"
                			}
                		],
                		"tags": []
                	},
                	"album": {
                		"id": 2827,
                		"mbId": "06614bff-257b-3fbe-a45b-8d946a8d3019",
                		"title": "Cliquety Kliqk",
                		"disambiguation": "",
                		"overview": "2003 studio album by General Elektriks",
                		"albumType": "Album",
                		"secondaryAlbumTypes": [],
                		"releaseDate": "2003-09-01T00:00:00Z",
                		"genres": [
                			"Downtempo",
                			"Electro",
                			"Electronic",
                			"Hip Hop",
                			"Synth-Pop"
                		],
                		"images": [
                			{
                				"coverType": "cover",
                				"url": "/MediaCover/Albums/2827/cover.jpg?lastWrite=637538890040000000",
                				"remoteUrl": "https://imagecache.lidarr.audio/v1/caa/84282cd1-aa02-4363-95f0-5bf824fec528/15449764515-1200.jpg"
                			}
                		]
                	},
                	"tracks": [
                		{
                			"id": 258841,
                			"title": "C'est l'introduction",
                			"trackNumber": "1",
                			"qualityVersion": 0
                		},
                		{
                			"id": 258842,
                			"title": "Frost On Your Sunglasses",
                			"trackNumber": "2",
                			"qualityVersion": 0
                		},
                		{
                			"id": 258843,
                			"title": "F'acing That Void",
                			"trackNumber": "3",
                			"qualityVersion": 0
                		},
                		{
                			"id": 258844,
                			"title": "C'entral park",
                			"trackNumber": "4",
                			"qualityVersion": 0
                		},
                		{
                			"id": 258845,
                			"title": "Time To Undress",
                			"trackNumber": "5",
                			"qualityVersion": 0
                		},
                		{
                			"id": 258846,
                			"title": "Tu M'intrigues",
                			"trackNumber": "6",
                			"qualityVersion": 0
                		},
                		{
                			"id": 258847,
                			"title": "Terms And Conditions Apply",
                			"trackNumber": "7",
                			"qualityVersion": 0
                		},
                		{
                			"id": 258848,
                			"title": "Take You Out Tonight",
                			"trackNumber": "8",
                			"qualityVersion": 0
                		},
                		{
                			"id": 258849,
                			"title": "Le Carroussel Cosmique",
                			"trackNumber": "9",
                			"qualityVersion": 0
                		},
                		{
                			"id": 258850,
                			"title": "Brain Collage",
                			"trackNumber": "10",
                			"qualityVersion": 0
                		},
                		{
                			"id": 258851,
                			"title": "Parachute",
                			"trackNumber": "11",
                			"qualityVersion": 0
                		},
                		{
                			"id": 258852,
                			"title": "Techno Kid",
                			"trackNumber": "12",
                			"qualityVersion": 0
                		}
                	],
                	"trackFiles": [
                		{
                			"id": 27378,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 01 - C'est l'introduction.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 11968803,
                			"dateAdded": "2025-02-25T15:51:17.7543153Z"
                		},
                		{
                			"id": 27379,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 02 - Frost On Your Sunglasses.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 20471514,
                			"dateAdded": "2025-02-25T15:51:18.3171153Z"
                		},
                		{
                			"id": 27380,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 03 - F'acing That Void.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 22548497,
                			"dateAdded": "2025-02-25T15:51:18.7993328Z"
                		},
                		{
                			"id": 27381,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 04 - C'entral park.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 20292635,
                			"dateAdded": "2025-02-25T15:51:19.6369226Z"
                		},
                		{
                			"id": 27382,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 05 - Time To Undress.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 23514472,
                			"dateAdded": "2025-02-25T15:51:20.1429422Z"
                		},
                		{
                			"id": 27383,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 06 - Tu M'intrigues.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 33417570,
                			"dateAdded": "2025-02-25T15:51:20.7395037Z"
                		},
                		{
                			"id": 27384,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 07 - Terms And Conditions Apply.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 28369619,
                			"dateAdded": "2025-02-25T15:51:21.4021357Z"
                		},
                		{
                			"id": 27385,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 08 - Take You Out Tonight.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 34169191,
                			"dateAdded": "2025-02-25T15:51:21.9603223Z"
                		},
                		{
                			"id": 27386,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 09 - Le Carroussel Cosmique.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 9283083,
                			"dateAdded": "2025-02-25T15:51:22.5787747Z"
                		},
                		{
                			"id": 27387,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 10 - Brain Collage.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 43405891,
                			"dateAdded": "2025-02-25T15:51:22.8859445Z"
                		},
                		{
                			"id": 27388,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 11 - Parachute.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 16146512,
                			"dateAdded": "2025-02-25T15:51:23.6513207Z"
                		},
                		{
                			"id": 27389,
                			"path": "/media/Music/General Elektriks/Cliquety Kliqk (2003)/General Elektriks - Cliquety Kliqk - 12 - Techno Kid.flac",
                			"quality": "FLAC",
                			"qualityVersion": 1,
                			"size": 31017976,
                			"dateAdded": "2025-02-25T15:51:24.0779015Z"
                		}
                	],
                	"isUpgrade": false,
                	"downloadClient": "qBittorrent",
                	"downloadClientType": "qBittorrent",
                	"downloadId": "83F64DC507F346FF1AFDF2E3337FAA8737DDEE44",
                	"eventType": "Download",
                	"instanceName": "Lidarr",
                	"applicationUrl": ""
                }
                """;

        given().contentType(ContentType.JSON).body(notification)
                .when().post("lidarr")
                .then().statusCode(200);

        await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).until(() -> !wireMockServer.getServeEvents().getRequests().isEmpty());

        wireMockServer.verify(1, putRequestedFor(urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withRequestBody(equalToJson("""
                        {
                          "msgtype": "m.text",
                          "body": "<h1>Album Downloaded</h1><p>General Elektriks - Cliquety Kliqk<br>Cover: https://imagecache.lidarr.audio/v1/caa/84282cd1-aa02-4363-95f0-5bf824fec528/15449764515-1200.jpg<br>Genres: Downtempo, Electro, Electronic, Hip Hop, Synth-Pop<br>Source: qBittorrent</p>",
                          "format": "org.matrix.custom.html",
                          "formatted_body": "<h1>Album Downloaded</h1><p>General Elektriks - Cliquety Kliqk<br>Cover: https://imagecache.lidarr.audio/v1/caa/84282cd1-aa02-4363-95f0-5bf824fec528/15449764515-1200.jpg<br>Genres: Downtempo, Electro, Electronic, Hip Hop, Synth-Pop<br>Source: qBittorrent</p>"
                        }
                        """)));
    }
}