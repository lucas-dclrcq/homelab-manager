package org.hoohoot.homelab.manager.it

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hoohoot.homelab.manager.infrastructure.api.resources.PlaylistResource
import org.hoohoot.homelab.manager.it.config.InjectWireMock
import org.hoohoot.homelab.manager.it.config.WiremockTestResource
import org.junit.jupiter.api.Test

@QuarkusTest
@TestHTTPEndpoint(PlaylistResource::class)
@QuarkusTestResource(WiremockTestResource::class)
class GetPlaylistTest {

    @InjectWireMock
    private val wireMockServer: WireMockServer? = null


    @Test
    fun testGetUserPlaylists() {
        wireMockServer!!.stubFor(
            WireMock.get(urlEqualTo("/v1/me/playlists"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("spotify_user_playlists_response.json")
                )
        )

        wireMockServer.stubFor(
            WireMock.get(urlPathEqualTo("/v1/playlists/6AwerF5LRpBRjvQcan8UDK"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("spotify_playlist_response.json")
                )
        )
        given()
            .`when`().get()
            .then()
            .statusCode(200)
    }
}