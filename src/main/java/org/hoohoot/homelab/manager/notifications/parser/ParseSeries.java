package org.hoohoot.homelab.manager.notifications.parser;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ParseSeries extends ArrParser {
    private final Optional<JsonObject> series;
    private final Optional<JsonArray> episodes;
    private final Optional<JsonArray> episodeFiles;
    private final Optional<JsonObject> release;
    private final Optional<String> downloadClient;

    private static final String DEFAULT_VALUE = "unknown";

    private ParseSeries(JsonObject payload) {
        super(Optional.ofNullable(payload.getJsonObject("series")).map(series -> series.getJsonArray("tags")));
        this.series = Optional.ofNullable(payload.getJsonObject("series"));
        this.episodes = Optional.ofNullable(payload.getJsonArray("episodes"));
        this.episodeFiles = Optional.ofNullable(payload.getJsonArray("episodeFiles"));
        this.release = Optional.ofNullable(payload.getJsonObject("release"));
        this.downloadClient = Optional.ofNullable(payload.getString("downloadClient"));
    }

    public static ParseSeries from(JsonObject payload) {
        return new ParseSeries(payload);
    }

    public String quality() {
        return this.episodeFiles.map(jsonArray -> jsonArray.getJsonObject(0))
                .stream().findFirst()
                .map(e -> e.getString("quality"))
                .orElse(DEFAULT_VALUE);
    }

    public String seriesName() {
        return this.series.map(json -> json.getString("title"))
                .orElse(DEFAULT_VALUE);
    }

    public String seasonAndEpisodeNumber() {
        return this.episodes.map(jsonArray -> jsonArray.getJsonObject(0))
                .map(e -> {
                    var episodeNumber = e.getInteger("episodeNumber");
                    var seasonNumber = e.getInteger("seasonNumber");

                    return "S%02dE%02d".formatted(seasonNumber, episodeNumber);
                }).orElse(DEFAULT_VALUE);
    }

    public String episodeName() {
        return this.episodes.map(jsonArray -> jsonArray.getJsonObject(0))
                .map(episode -> episode.getString("title"))
                .orElse(DEFAULT_VALUE);
    }

    public String downloadClient() {
        return this.downloadClient.orElse(DEFAULT_VALUE);
    }

    public String indexer() {
        return release
                .map(r -> r.getString("indexer").replace(" (Prowlarr)", ""))
                .orElse(DEFAULT_VALUE);
    }
}