package org.hoohoot.homelab.manager.notifications.legacy.radarr;

public record RadarrRemoteMovieDTO(long tmdbID, String imdbID, String title, long year) { }