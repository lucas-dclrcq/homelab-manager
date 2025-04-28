import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.domain.AlbumInfo
import org.hoohoot.homelab.manager.domain.Artist
import org.hoohoot.homelab.manager.domain.Playlist
import org.hoohoot.homelab.manager.domain.Track
import org.hoohoot.homelab.manager.infrastructure.spotify.*
import org.junit.jupiter.api.Test

class SpotifyPlaylistExtensionsTest {

    @Test
    fun `test SpotifyTrack toDomainModel`() {
        val spotifyTrack = SpotifyTrack(
            name = "Track Name",
            album = SpotifyAlbum(
                totalTracks = 10,
                id = "albumId",
                name = "Album Name",
                images = emptyList(),
                releaseDate = "2020-01-01",
                releaseDatePrecision = "year",
                availableMarkets = emptyList(),
                restrictions = SpotifyRestrictions("restrictions"),
                type = "album",
                uri = "uri:album:albumId",
                href = "href:album:albumId",
                albumType = "album",
                externalUrls = null,
                artists = emptyList()
            ),
            artists = listOf(
                SpotifyArtist(
                    id = "artistId",
                    name = "Artist Name",
                    externalUrls = null,
                    href = "href:artist:artistId",
                    type = "artist",
                    uri = "uri:artist:artistId"
                )
            ),
            durationMs = 300000,
            trackNumber = 1,
            id = "trackId",
            discNumber = 1,
            explicit = false,
            isLocal = false,
            popularity = 100,
            previewUrl = "previewUrl",
            externalUrls = null,
            href = "href:track:trackId",
            type = "track",
            uri = "uri:track:trackId",
            isPlayable = true,
            linkedFrom = null,
            restrictions = SpotifyRestrictions("restrictions"),
            externalIds = SpotifyExternalIds("isrc", "ean", "upc"),
            availableMarkets = emptyList()
        )

        val domainModel = spotifyTrack.toDomainModel()

        assertThat(domainModel).isEqualTo(
            Track(
                name = "Track Name",
                album = AlbumInfo(
                    totalTracks = 10,
                    id = "albumId",
                    name = "Album Name"
                ),
                artists = listOf(
                    Artist(
                        id = "artistId",
                        name = "Artist Name"
                    )
                ),
                durationMs = 300000,
                trackNumber = 1,
                id = "trackId"
            )
        )
    }

    @Test
    fun `test SpotifyAlbum toDomainModel`() {
        val spotifyAlbum = SpotifyAlbum(
            totalTracks = 10,
            id = "albumId",
            name = "Album Name",
            images = emptyList(),
            releaseDate = "2020-01-01",
            releaseDatePrecision = "year",
            availableMarkets = emptyList(),
            restrictions = SpotifyRestrictions("restrictions"),
            type = "album",
            uri = "uri:album:albumId",
            href = "href:album:albumId",
            albumType = "album",
            externalUrls = null,
            artists = emptyList()
        )

        val domainModel = spotifyAlbum.toDomainModel()

        assertThat(domainModel).isEqualTo(
            AlbumInfo(
                totalTracks = 10,
                id = "albumId",
                name = "Album Name"
            )
        )
    }

    @Test
    fun `test SpotifyArtist toDomainModel`() {
        val spotifyArtist = SpotifyArtist(
            id = "artistId",
            name = "Artist Name",
            externalUrls = null,
            href = "href:artist:artistId",
            type = "artist",
            uri = "uri:artist:artistId"
        )

        val domainModel = spotifyArtist.toDomainModel()

        assertThat(domainModel).isEqualTo(
            Artist(
                id = "artistId",
                name = "Artist Name"
            )
        )
    }

    @Test
    fun `test SpotifyPlaylist toDomainModel`() {
        val spotifyPlaylist = SpotifyPlaylist(
            name = "Musique à partager",
            tracks = SpotifyTracks(
                items = listOf(
                    SpotifyTrackItem(
                        track = SpotifyTrack(
                            name = "250 Miles",
                            album = SpotifyAlbum(
                                totalTracks = 10,
                                id = "3w26tAxCUFYYzeFMq5qNJg",
                                name = "Brain Cycles",
                                albumType = "album",
                                availableMarkets = emptyList(),
                                externalUrls = SpotifyExternalUrl("spotify"),
                                href = "href",
                                images = listOf(SpotifyImage(1, "url", 1)),
                                releaseDate = "string",
                                releaseDatePrecision = "string",
                                restrictions = SpotifyRestrictions("reason"),
                                type = "string",
                                uri = "string",
                                artists = emptyList()
                            ),
                            artists = listOf(
                                SpotifyArtist(
                                    id = "692VvGTch5OLXj4zEE6H3y",
                                    name = "Radio Moscow",
                                    externalUrls = SpotifyExternalUrl("spotify"),
                                    href = "href",
                                    type = "string",
                                    uri = "string"
                                )
                            ),
                            durationMs = 292000,
                            id = "5zWsOD8aSwx5P7kGb6gQTw",
                            trackNumber = 6,
                            availableMarkets = emptyList(),
                            discNumber = 1,
                            explicit = true,
                            externalIds = SpotifyExternalIds("string", "string", "string"),
                            externalUrls = SpotifyExternalUrl("spotify"),
                            href = "href",
                            isPlayable = true,
                            linkedFrom = "string",
                            restrictions = SpotifyRestrictions("reason"),
                            popularity = 100,
                            previewUrl = "string",
                            type = "string",
                            uri = "string",
                            isLocal = true
                        ),
                        addedAt = "string",
                        addedBy = null,
                        isLocal = true
                    )
                ),
                href = "href",
                limit = 100,
                next = "string",
                offset = 0,
                previous = "string",
                total = 1
            ),
            collaborative = true,
            description = "string",
            externalUrls = SpotifyExternalUrl("spotify"),
            href = "href",
            id = "string",
            images = listOf(SpotifyImage(1, "url", 1)),
            owner = null,
            public = true,
            snapshotId = "string",
            type = "string",
            uri = "string"
        )

        val domainModel = spotifyPlaylist.toDomainModel()

        assertThat(domainModel).isEqualTo(
            Playlist(
                name = "Musique à partager",
                tracks = listOf(
                    Track(
                        name = "250 Miles",
                        album = AlbumInfo(
                            totalTracks = 10,
                            id = "3w26tAxCUFYYzeFMq5qNJg",
                            name = "Brain Cycles",
                        ),
                        artists = listOf(
                            Artist(
                                id = "692VvGTch5OLXj4zEE6H3y",
                                name = "Radio Moscow",
                            )
                        ),
                        durationMs = 292000,
                        id = "5zWsOD8aSwx5P7kGb6gQTw",
                        trackNumber = 6,
                    )
                )
            )
        )
    }
}
