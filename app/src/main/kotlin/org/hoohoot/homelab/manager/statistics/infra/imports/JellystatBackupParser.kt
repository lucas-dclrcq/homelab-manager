package org.hoohoot.homelab.manager.statistics.infra.imports

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.statistics.domain.MediaType
import org.hoohoot.homelab.manager.statistics.domain.PlaybackSessionRecord
import org.hoohoot.homelab.manager.statistics.domain.Platforms
import org.hoohoot.homelab.manager.statistics.domain.SessionSource
import org.hoohoot.homelab.manager.statistics.domain.ports.JellystatBackupContent
import org.hoohoot.homelab.manager.statistics.domain.ports.JellystatBackupReader
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@ApplicationScoped
class JellystatBackupParser(
    private val objectMapper: ObjectMapper,
    @param:ConfigProperty(name = "statistics.min-play-seconds") private val minPlaySeconds: Long,
    @param:ConfigProperty(name = "statistics.completed-threshold") private val completedThreshold: Double,
) : JellystatBackupReader {

    private data class EpisodeInfo(
        val seriesId: String?,
        val seriesName: String?,
        val seasonNumber: Int?,
        val episodeNumber: Int?,
        val runTimeTicks: Long?,
    )

    private data class ItemInfo(val type: String?, val runTimeTicks: Long?)

    override fun read(file: Path): JellystatBackupContent {
        val episodes = HashMap<String, EpisodeInfo>()
        val items = HashMap<String, ItemInfo>()

        scan(file, setOf("jf_library_episodes", "jf_library_items")) { table, row ->
            when (table) {
                "jf_library_episodes" -> row.text("EpisodeId")?.let { id ->
                    episodes[id] = EpisodeInfo(
                        seriesId = row.text("SeriesId"),
                        seriesName = row.text("SeriesName"),
                        seasonNumber = row.int("ParentIndexNumber"),
                        episodeNumber = row.int("IndexNumber"),
                        runTimeTicks = row.long("RunTimeTicks"),
                    )
                }
                "jf_library_items" -> row.text("Id")?.let { id ->
                    items[id] = ItemInfo(type = row.text("Type"), runTimeTicks = row.long("RunTimeTicks"))
                }
            }
        }

        val records = mutableListOf<PlaybackSessionRecord>()
        var ignored = 0
        scan(file, setOf("jf_playback_activity")) { _, row ->
            when (val record = toRecord(row, episodes, items)) {
                null -> ignored++
                else -> records += record
            }
        }
        return JellystatBackupContent(records, ignored)
    }

    private fun toRecord(
        row: JsonNode,
        episodes: Map<String, EpisodeInfo>,
        items: Map<String, ItemInfo>,
    ): PlaybackSessionRecord? {
        val importKey = row.text("Id") ?: return null
        val userId = row.text("UserId") ?: return null
        val userName = row.text("UserName") ?: return null
        val nowPlayingItemId = row.text("NowPlayingItemId") ?: return null
        val itemName = row.text("NowPlayingItemName") ?: return null
        val durationSeconds = row.text("PlaybackDuration")?.toLongOrNull() ?: return null
        if (durationSeconds < minPlaySeconds) return null

        val endedAt = row.text("ActivityDateInserted")?.let { Instant.parse(it) } ?: return null
        val startedAt = endedAt.minusSeconds(durationSeconds)
        val client = row.text("Client")

        val episodeId = row.text("EpisodeId")
        val mediaType: MediaType
        val itemId: String
        var seriesId: String? = null
        var seriesName: String? = null
        var seasonNumber: Int? = null
        var episodeNumber: Int? = null
        val runTimeTicks: Long?

        if (episodeId != null) {
            mediaType = MediaType.EPISODE
            itemId = episodeId
            // Pour un épisode, NowPlayingItemId pointe la série (vérifié sur backup réel)
            seriesId = nowPlayingItemId
            val info = episodes[episodeId]
            // Les SeriesName du backup traînent parfois des suffixes [tvdbid-...], y compris
            // dans jf_library_episodes : on strip quelle que soit la source
            seriesName = (info?.seriesName ?: row.text("SeriesName"))?.replace(ID_SUFFIX_REGEX, "")
            seasonNumber = info?.seasonNumber
            episodeNumber = info?.episodeNumber
            runTimeTicks = info?.runTimeTicks
        } else {
            val info = items[nowPlayingItemId]
            if (info?.type != null && info.type != "Movie") return null
            mediaType = MediaType.MOVIE
            itemId = nowPlayingItemId
            runTimeTicks = info?.runTimeTicks
        }

        val runtimeSeconds = runTimeTicks?.let { (it / TICKS_PER_SECOND).toInt() }?.takeIf { it > 0 }
        val progress = runtimeSeconds?.let { (durationSeconds.toDouble() / it * 100).coerceAtMost(100.0) }

        return PlaybackSessionRecord(
            userId = userId,
            userName = userName,
            itemId = itemId,
            itemName = itemName,
            seriesId = seriesId,
            seriesName = seriesName,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            mediaType = mediaType,
            client = client,
            deviceName = row.text("DeviceName"),
            platform = Platforms.fromClient(client),
            startedAt = startedAt.toUtcLocalDateTime(),
            endedAt = endedAt.toUtcLocalDateTime(),
            playDurationSeconds = durationSeconds.toInt(),
            runtimeSeconds = runtimeSeconds,
            progressPercent = progress,
            completed = progress != null && progress >= completedThreshold * 100,
            source = SessionSource.IMPORT,
            importKey = importKey,
        )
    }

    private fun scan(file: Path, tables: Set<String>, onRow: (String, JsonNode) -> Unit) {
        objectMapper.factory.createParser(file.toFile()).use { parser ->
            check(parser.nextToken() == JsonToken.START_ARRAY) { "Backup Jellystat invalide : tableau racine attendu" }
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                while (parser.nextToken() == JsonToken.FIELD_NAME) {
                    val table = parser.currentName()
                    parser.nextToken()
                    if (table in tables && parser.currentToken() == JsonToken.START_ARRAY) {
                        while (parser.nextToken() == JsonToken.START_OBJECT) {
                            onRow(table, objectMapper.readTree(parser))
                        }
                    } else {
                        parser.skipChildren()
                    }
                }
            }
        }
    }

    private fun JsonNode.text(field: String): String? = get(field)?.takeUnless { it.isNull }?.asText()
    private fun JsonNode.int(field: String): Int? = get(field)?.takeUnless { it.isNull }?.asInt()
    private fun JsonNode.long(field: String): Long? = get(field)?.takeUnless { it.isNull }?.asText()?.toLongOrNull()

    private fun Instant.toUtcLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneOffset.UTC)

    companion object {
        private const val TICKS_PER_SECOND = 10_000_000L
        private val ID_SUFFIX_REGEX = Regex("""\s*\[\w+id-[^\]]+]$""")
    }
}
