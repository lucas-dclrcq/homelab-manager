package org.hoohoot.homelab.manager.radarr;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public record RadarrMovieDTO
        (long id,
         String title,
         long year,
         String releaseDate,
         String folderPath,
         long tmdbId,
         String imdbId,
         List<String> tags) {
    public String userTag() {
        if (tags == null) return "unknown";

        return tags.stream()
                .filter(tag -> tag.matches("\\d+ - \\w+"))
                .map(tag -> tag.split(" - ")[1])
                .findFirst()
                .orElse("unknown");
    }
}