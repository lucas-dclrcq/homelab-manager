package org.hoohoot.homelab.manager.notifications.legacy.radarr;

public record RadarrMovieDownloadedDTO(RadarrMovieDTO movie,
                                       RadarrRemoteMovieDTO remoteMovie,
                                       RadarrMovieFileDTO movieFile,
                                       boolean isUpgrade,
                                       String downloadClient,
                                       String downloadClientType,
                                       String downloadID,
                                       String eventType) {
}
