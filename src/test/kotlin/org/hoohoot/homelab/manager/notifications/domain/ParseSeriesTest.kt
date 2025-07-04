package org.hoohoot.homelab.manager.notifications.domain

import io.vertx.core.json.JsonObject
import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.domain.media_notifications.ParseSeries.Companion.from
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ParseSeriesTest {
    private val payload = JsonObject(
        """
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
                 	"episodeFile":                  		{
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
    )

    @Test
    fun `should parse quality`() {
        val quality = from(payload).quality
        assertThat(quality).isEqualTo("WEBDL-720p")
    }

    @Test
    fun `should parse quality when no episode files`() {
        val payload = JsonObject(
            """
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
        )
        val quality = from(payload).quality
        assertThat(quality).isEqualTo("unknown")
    }

    @Test
    fun `should parse series name`() {
        val seriesName = from(payload).seriesName
        assertThat(seriesName).isEqualTo("Australian Survivor")
    }

    @Test
    fun `should parse episode name`() {
        val episodeName = from(payload).episodeName
        assertThat(episodeName).isEqualTo("Episode 6")
    }

    @Test
    fun `should parse download client`() {
        val downloadClient = from(payload).downloadClient
        assertThat(downloadClient).isEqualTo("SABnzbd")
    }

    @Test
    fun `should parse indexer`() {
        val indexer = from(payload).indexer
        assertThat(indexer).isEqualTo("NZBFinder")
    }

    @Test
    fun `should parse requester`() {
        val requester = from(payload).requester
        assertThat(requester).isEqualTo("flo")
    }

    @Test
    fun `should parse imdb link`() {
        val imdbLink = from(payload).imdbId
        assertThat(imdbLink).isEqualTo("tt0310416")
    }

    @ParameterizedTest
    @CsvSource(
        "12,6,S12E06", "2,24,S02E24", "0,123,S00E123"
    )
    fun `should parse various season and episode number formats`(
        seasonNumber: String?,
        episodeNumber: String?,
        expected: String?
    ) {
        // ASSERT
        val payload = JsonObject(
            """
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
                 			"episodeNumber": %s,
                 			"seasonNumber": %s,
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
                
                """.trimIndent().format(episodeNumber, seasonNumber)
        )

        // ACT
        val seasonAndEpisodeNumber = from(payload).seasonAndEpisodeNumber

        // ASSERT
        assertThat(seasonAndEpisodeNumber).isEqualTo(expected)
    }
}