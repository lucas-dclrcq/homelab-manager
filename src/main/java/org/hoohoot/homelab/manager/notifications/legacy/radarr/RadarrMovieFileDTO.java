package org.hoohoot.homelab.manager.notifications.legacy.radarr;

public record RadarrMovieFileDTO
        (long id,
         String relativePath,
         String path,
         String quality,
         long qualityVersion,
         String releaseGroup,
         String sceneName,
         String indexerFlags,
         long size) {
}