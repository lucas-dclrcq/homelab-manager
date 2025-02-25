package org.hoohoot.homelab.manager;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hoohoot.homelab.manager.config.InjectWireMock;
import org.hoohoot.homelab.manager.config.WiremockTestResource;
import org.hoohoot.homelab.manager.notifications.resource.NotificationsResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

@QuarkusTest
@TestHTTPEndpoint(NotificationsResource.class)
@QuarkusTestResource(WiremockTestResource.class)
class RadarrNotificationsTest {
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
                  "movie": {
                    "id": 686,
                    "title": "The Wild Robot",
                    "year": 2024,
                    "releaseDate": "yyyy-MM-dd",
                    "folderPath": "<pathToFolder>",
                    "tmdbId": 1,
                    "imdbId": "tt29623480",
                    "tags": [
                      "1 - lucasd"
                    ]
                  },
                  "remoteMovie": {
                    "tmdbId": 1,
                    "imdbId": "tt<id>",
                    "title": "<movieTitle>",
                    "year": 1
                  },
                  "movieFile": {
                    "id": 36745,
                    "relativePath": "<filenameAfterRename",
                    "path": "<pathToDownloadedFile>",
                    "quality": "WEBDL-720p",
                    "qualityVersion": 1,
                    "releaseGroup": "<rlsGroup>",
                    "sceneName": "The.Movie.Title.2012.1080p.BluRay.x265-<rlsGroup>",
                    "indexerFlags": "0",
                    "size": 1
                  },
                  "isUpgrade": false,
                  "downloadClient": "<downloadClient>",
                  "downloadClientType": "<type>",
                  "downloadId": "<downloadClient>_The.Movie.Title.2012.1080p.BluRay.x265-<rlsGroup>_<longInt>",
                  "eventType": "Download"
                }
                """;

        given().contentType(ContentType.JSON).body(notification)
                .when().post("radarr")
                .then().statusCode(200);

        await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).until(() -> !wireMockServer.getServeEvents().getRequests().isEmpty());

        wireMockServer.verify(1, putRequestedFor(urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withRequestBody(equalToJson("""
                        {
                          "msgtype": "m.text",
                          "body": "<h1>Movie Downloaded</h1><p>The Wild Robot (2024) [WEBDL-720p] https://www.imdb.com/title/tt29623480/<br>Requested by : lucasd</p>",
                          "format": "org.matrix.custom.html",
                          "formatted_body": "<h1>Movie Downloaded</h1><p>The Wild Robot (2024) [WEBDL-720p] https://www.imdb.com/title/tt29623480/<br>Requested by : lucasd</p>"
                        }
                        """)));
    }

    @Test
    void shouldSendNotificationToConfiguredRoom() {
        var notification = """
                {
                  "movie": {
                    "id": 686,
                    "title": "The Wild Robot",
                    "year": 2024,
                    "releaseDate": "yyyy-MM-dd",
                    "folderPath": "<pathToFolder>",
                    "tmdbId": 1,
                    "imdbId": "tt29623480",
                    "tags": [
                      "1 - lucasd"
                    ]
                  },
                  "remoteMovie": {
                    "tmdbId": 1,
                    "imdbId": "tt<id>",
                    "title": "<movieTitle>",
                    "year": 1
                  },
                  "movieFile": {
                    "id": 36745,
                    "relativePath": "<filenameAfterRename",
                    "path": "<pathToDownloadedFile>",
                    "quality": "WEBDL-1080p",
                    "qualityVersion": 1,
                    "releaseGroup": "<rlsGroup>",
                    "sceneName": "The.Movie.Title.2012.1080p.BluRay.x265-<rlsGroup>",
                    "indexerFlags": "0",
                    "size": 1
                  },
                  "isUpgrade": false,
                  "downloadClient": "<downloadClient>",
                  "downloadClientType": "<type>",
                  "downloadId": "<downloadClient>_The.Movie.Title.2012.1080p.BluRay.x265-<rlsGroup>_<longInt>",
                  "eventType": "Download"
                }
                """;

        given().contentType(ContentType.JSON).body(notification)
                .when().post("radarr")
                .then().statusCode(200);

        await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).until(() -> !wireMockServer.getServeEvents().getRequests().isEmpty());

        wireMockServer.verify(1, putRequestedFor(urlMatching("/_matrix/client/r0/rooms/!radarr:test-server.tld/send/m.room.message/.*")));
    }

    @Test
    void shouldUseUUIDAsRandomTransactionId() {
        var notification = """
                {
                  "movie": {
                    "id": 686,
                    "title": "The Wild Robot",
                    "year": 2024,
                    "releaseDate": "yyyy-MM-dd",
                    "folderPath": "<pathToFolder>",
                    "tmdbId": 1,
                    "imdbId": "tt29623480",
                    "tags": [
                      "1 - lucasd"
                    ]
                  },
                  "remoteMovie": {
                    "tmdbId": 1,
                    "imdbId": "tt<id>",
                    "title": "<movieTitle>",
                    "year": 1
                  },
                  "movieFile": {
                    "id": 36745,
                    "relativePath": "<filenameAfterRename",
                    "path": "<pathToDownloadedFile>",
                    "quality": "WEBDL-1080p",
                    "qualityVersion": 1,
                    "releaseGroup": "<rlsGroup>",
                    "sceneName": "The.Movie.Title.2012.1080p.BluRay.x265-<rlsGroup>",
                    "indexerFlags": "0",
                    "size": 1
                  },
                  "isUpgrade": false,
                  "downloadClient": "<downloadClient>",
                  "downloadClientType": "<type>",
                  "downloadId": "<downloadClient>_The.Movie.Title.2012.1080p.BluRay.x265-<rlsGroup>_<longInt>",
                  "eventType": "Download"
                }
                """;

        given().contentType(ContentType.JSON).body(notification)
                .when().post("radarr")
                .then().statusCode(200);

        await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).until(() -> !wireMockServer.getServeEvents().getRequests().isEmpty());

        wireMockServer.verify(1, putRequestedFor(urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));
    }

    @Test
    void shouldAddMatrixTokenAsBearer() {
        var notification = """
                {
                  "movie": {
                    "id": 686,
                    "title": "The Wild Robot",
                    "year": 2024,
                    "releaseDate": "yyyy-MM-dd",
                    "folderPath": "<pathToFolder>",
                    "tmdbId": 1,
                    "imdbId": "tt29623480",
                    "tags": [
                      "1 - lucasd"
                    ]
                  },
                  "remoteMovie": {
                    "tmdbId": 1,
                    "imdbId": "tt<id>",
                    "title": "<movieTitle>",
                    "year": 1
                  },
                  "movieFile": {
                    "id": 36745,
                    "relativePath": "<filenameAfterRename",
                    "path": "<pathToDownloadedFile>",
                    "quality": "WEBDL-1080p",
                    "qualityVersion": 1,
                    "releaseGroup": "<rlsGroup>",
                    "sceneName": "The.Movie.Title.2012.1080p.BluRay.x265-<rlsGroup>",
                    "indexerFlags": "0",
                    "size": 1
                  },
                  "isUpgrade": false,
                  "downloadClient": "<downloadClient>",
                  "downloadClientType": "<type>",
                  "downloadId": "<downloadClient>_The.Movie.Title.2012.1080p.BluRay.x265-<rlsGroup>_<longInt>",
                  "eventType": "Download"
                }
                """;

        given().contentType(ContentType.JSON).body(notification)
                .when().post("radarr")
                .then().statusCode(200);

        await().atMost(10, TimeUnit.SECONDS).pollInterval(100, TimeUnit.MILLISECONDS).until(() -> !wireMockServer.getServeEvents().getRequests().isEmpty());

        wireMockServer.verify(1, putRequestedFor(urlMatching("/_matrix/client/r0/rooms/.*/send/m.room.message/.*"))
                .withHeader("Authorization", equalTo("Bearer TOKEN")));
    }
}