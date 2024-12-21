package org.hoohoot.homelab.manager.notifications;

import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import me.ldclrcq.kafka.matrix.sink.IncomingMatrixMessage;
import me.ldclrcq.kafka.matrix.sink.IncomingMatrixMessageKey;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;


@ApplicationScoped
public class NotificationsTopologyProducer {
    private final static String INCOMING_NOTIFICATIONS_TOPIC = "incoming-notifications";
    private final static String SONARR_NOTIFICATIONS_TOPIC = "sonarr-notifications";
    private final static String RADARR_NOTIFICATIONS_TOPIC = "radarr-notifications";
    private final static String MATRIX_MESSAGES_TOPIC = "matrix-messages";

    private final Serde<IncomingMatrixMessageKey> matrixMessageKeySerde;
    private final Serde<IncomingMatrixMessage> matrixMessageSerde;
    private final String matrixRoomId;

    public NotificationsTopologyProducer(Serde<IncomingMatrixMessageKey> matrixMessageKeySerde, Serde<IncomingMatrixMessage> matrixMessageSerde, @ConfigProperty(name = "matrix.room_id") String matrixRoomId) {
        this.matrixMessageKeySerde = matrixMessageKeySerde;
        this.matrixMessageSerde = matrixMessageSerde;
        this.matrixRoomId = matrixRoomId;
    }

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        Branched<String, String> sonarrBranch = Branched.withConsumer(ks -> ks
                .selectKey((key, value) -> new JsonObject(value).getJsonObject("series").getString("titleSlug"))
                .to(SONARR_NOTIFICATIONS_TOPIC, Produced.with(Serdes.String(), Serdes.String())));

        Branched<String, String> radarrBranch = Branched.withConsumer(ks -> ks
                .selectKey((key, value) -> new JsonObject(value).getJsonObject("movie").getString("title"))
                .to(RADARR_NOTIFICATIONS_TOPIC, Produced.with(Serdes.String(), Serdes.String())));

        builder
                .stream(INCOMING_NOTIFICATIONS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))
                .split()
                .branch((key, value) -> key.equals("sonarr"), sonarrBranch)
                .branch((key, value) -> key.equals("radarr"), radarrBranch);

        builder.stream(SONARR_NOTIFICATIONS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))
                .filter((key, value) -> new JsonObject(value).getString("eventType").equals("Download"))
                .map((key, value) -> new KeyValue<>(IncomingMatrixMessageKey.newBuilder()
                        .setChannelId(matrixRoomId)
                        .build(), buildEpisodeDownloadedMatrixMessage(value)))
                .to(MATRIX_MESSAGES_TOPIC, Produced.with(matrixMessageKeySerde, matrixMessageSerde));

        builder.stream(RADARR_NOTIFICATIONS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))
                .filter((key, value) -> new JsonObject(value).getString("eventType").equals("Grab"))
                .map((key, value) -> new KeyValue<>(IncomingMatrixMessageKey.newBuilder()
                        .setChannelId(matrixRoomId)
                        .build(), buildMovieDownloadedMatrixMessage(value)))
                .to(MATRIX_MESSAGES_TOPIC, Produced.with(matrixMessageKeySerde, matrixMessageSerde));

        return builder.build();
    }

    private IncomingMatrixMessage buildEpisodeDownloadedMatrixMessage(String rawSonarrPayload) {
        JsonObject json = new JsonObject(rawSonarrPayload);
        JsonObject series = json.getJsonObject("series");
        String title = series.getString("title");
        String imdbId = series.getString("imdbId");

        JsonObject episodeInfo = json.getJsonArray("episodes")
                .stream()
                .map(episode -> (JsonObject) episode)
                .findFirst().get();

        String episodeTitle = episodeInfo.getString("title");
        String episodeOverview = episodeInfo.getString("overview");
        String episodeNumber = episodeInfo.getString("episodeNumber");
        String seasonNumber = episodeInfo.getString("seasonNumber");


        var matrixNotification = """
                <h1>Episode Downloaded : %s</h1>
                <p>
                    S%sE%s - %s<br>
                    %s<br>
                    https://www.imdb.com/title/%s/<br>
                </p>
                """.formatted(title, seasonNumber, episodeNumber, episodeTitle, episodeOverview, imdbId);

        return IncomingMatrixMessage.newBuilder()
                .setMsgType("m.text")
                .setFormattedBody(matrixNotification)
                .setFormat("org.matrix.custom.html")
                .setBody(matrixNotification).build();
    }

    private IncomingMatrixMessage buildMovieDownloadedMatrixMessage(String rawRadarrPayload) {
        JsonObject json = new JsonObject(rawRadarrPayload);
        String title = json.getJsonObject("movie").getString("title");
        String year = json.getJsonObject("movie").getString("year");
        String imdbId = json.getJsonObject("movie").getString("imdbId");
        List<String> tags = json.getJsonArray("tags").stream().map(Object::toString).toList();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<h1>Movie Downloaded</h1>");
        stringBuilder.append("<p>");
        stringBuilder.append("%s (%s) [WEBDL-1080p] https://www.imdb.com/title/%s/<br>".formatted(title, year, imdbId));
        stringBuilder.append("Requested by : %s".formatted(this.userTag(tags)));
        stringBuilder.append("</p>");

        return IncomingMatrixMessage.newBuilder()
                .setMsgType("m.text")
                .setFormattedBody(stringBuilder.toString())
                .setFormat("org.matrix.custom.html")
                .setBody(stringBuilder.toString()).build();
    }

    public String userTag(List<String> tags) {
        if (tags == null) return "unknown";

        return tags.stream()
                .filter(tag -> tag.matches("\\d+ - \\w+"))
                .map(tag -> tag.split(" - ")[1])
                .findFirst()
                .orElse("unknown");
    }

}