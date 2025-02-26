package org.hoohoot.homelab.manager.notifications.kafka;

import io.vertx.core.json.JsonObject;
import org.hoohoot.homelab.manager.notifications.parser.ParseSeries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ParseSeriesTest {

    @Test
    void shouldParseQuality() {
        // ASSERT
        var payload = new JsonObject("""
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
                """);

        // ACT
        String seriesName = ParseSeries.from(payload).seriesName();

        // ASSERT
        assertThat(seriesName).isEqualTo("Australian Survivor");
    }

    @Test
    void shouldParseQualityWhenNoEpisodeFiles() {
        // ASSERT
        var payload = new JsonObject("""
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
                """);

        // ACT
        String quality = ParseSeries.from(payload).quality();

        // ASSERT
        assertThat(quality).isEqualTo("unknown");
    }

    @Test
    void shouldParseSeriesName() {
        // ASSERT
        var payload = new JsonObject("""
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
                """);

        // ACT
        String seriesName = ParseSeries.from(payload).seriesName();

        // ASSERT
        assertThat(seriesName).isEqualTo("Australian Survivor");
    }

    @Test
    void shouldParseEpisodeName() {
        // ASSERT
        var payload = new JsonObject("""
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
                """);

        // ACT
        String episodeName = ParseSeries.from(payload).episodeName();

        // ASSERT
        assertThat(episodeName).isEqualTo("Episode 6");
    }

    @Test
    void shouldParseDownloadClient() {
        // ASSERT
        var payload = new JsonObject("""
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
                """);

        // ACT
        String downloadClient = ParseSeries.from(payload).downloadClient();

        // ASSERT
        assertThat(downloadClient).isEqualTo("SABnzbd");
    }

    @Test
    void shouldParseIndexer() {
        // ASSERT
        var payload = new JsonObject("""
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
                """);

        // ACT
        String indexer = ParseSeries.from(payload).indexer();

        // ASSERT
        assertThat(indexer).isEqualTo("NZBFinder");
    }

    @Test
    void shouldParseRequester() {
        // ASSERT
        var payload = new JsonObject("""
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
                """);

        // ACT
        String requester = ParseSeries.from(payload).requester();

        // ASSERT
        assertThat(requester).isEqualTo("flo");
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "12,6,S12E06",
                    "2,24,S02E24",
                    "0,123,S00E123"
            })
    void shouldParseSeasonAndEpisodeNumber(String seasonNumber, String episodeNumber, String expected) {
        // ASSERT
        var payload = new JsonObject("""
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
                """.formatted(episodeNumber, seasonNumber));

        // ACT
        String seasonAndEpisodeNumber = ParseSeries.from(payload).seasonAndEpisodeNumber();

        // ASSERT
        assertThat(seasonAndEpisodeNumber).isEqualTo(expected);
    }
}