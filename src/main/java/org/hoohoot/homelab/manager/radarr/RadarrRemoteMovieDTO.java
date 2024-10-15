package org.hoohoot.homelab.manager.radarr;

public record RadarrRemoteMovieDTO(long tmdbID, String imdbID, String title, long year) { }