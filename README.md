# Homelab Manager

**Homelab Manager** is a self-hosted portal for a home media setup. It provides:

- An **application dashboard** listing the services running in the homelab, kept up to date automatically by a companion
  **Kubernetes operator** that watches Gateway API `HTTPRoute`s (see [operator/README.md](operator/README.md)).
- **Matrix notifications** for media events: webhooks from Sonarr, Radarr, Lidarr, Bazarr and Jellyseerr are enriched
  and forwarded to Matrix rooms, plus a weekly activity report.
- A **Matrix bot** for interacting with the setup from chat.
- A **downloads timeline and library stats**, built by periodically syncing the *arr apps, Jellyfin and Jellystat.

The web UI is served by the backend (Quarkus + Kotlin, React frontend), with OIDC login and PostgreSQL storage.

This app is quite specific to my needs, and since I also use it to experiment and keep current on the latest tech, it is
quite overkill :D

## Deployment

Container images and a Helm chart are published to GHCR on every release:

- `ghcr.io/lucas-dclrcq/homelab-manager` — the app
- `ghcr.io/lucas-dclrcq/homelab-manager-operator` — the operator
- `oci://ghcr.io/lucas-dclrcq/charts/homelab-manager` — the Helm chart (deploys both)

```bash
helm install homelab-manager oci://ghcr.io/lucas-dclrcq/charts/homelab-manager \
  --values my-values.yaml
```

Configuration is passed as environment variables through the chart's `app.env` / `app.envFrom` (and `operator.env` /
`operator.envFrom`) values. The API key shared between the app and the operator is injected into both containers from an
existing secret via `operatorApiKey.existingSecret`.
See [charts/homelab-manager/values.yaml](charts/homelab-manager/values.yaml) for all chart options (image, route,
probes, RBAC, resources...).

For local development:

```bash
mvn -pl app quarkus:dev       # app + web UI, with dev services (Keycloak, Postgres, WireMock, Synapse)
mvn -pl operator quarkus:dev  # operator, using your local kubeconfig
```

## Configuration

The app is configured entirely through environment variables (Quarkus maps `some.config-key` to `SOME_CONFIG_KEY`).

### Required

| Variable                    | Description                                                                                                          |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------|
| `OIDC_AUTH_SERVER_URL`      | OIDC provider URL (e.g. a Keycloak realm)                                                                            |
| `OIDC_CLIENT_ID`            | OIDC client id                                                                                                       |
| `OIDC_CLIENT_SECRET`        | OIDC client secret                                                                                                   |
| `DB_JDBC_URL`               | PostgreSQL JDBC URL (used by Flyway migrations)                                                                      |
| `DB_REACTIVE_URL`           | PostgreSQL reactive URL (used by the app)                                                                            |
| `DB_USERNAME`               | Database username                                                                                                    |
| `DB_PASSWORD`               | Database password                                                                                                    |
| `MATRIX_BASE_URL`           | Matrix homeserver URL                                                                                                |
| `MATRIX_ACCESS_TOKEN`       | Access token used to send notifications                                                                              |
| `MATRIX_ROOM_MEDIA`         | Room for movie/TV notifications (e.g. `!media:server.tld`)                                                           |
| `MATRIX_ROOM_MUSIC`         | Room for music notifications                                                                                         |
| `MATRIX_ROOM_SUPPORT`       | Room for support/report notifications                                                                                |
| `MATRIX_BOT_ENABLED`        | Enable the Matrix bot (`true`/`false`)                                                                               |
| `MATRIX_BOT_PREFIX`         | Command prefix the bot answers to                                                                                    |
| `MATRIX_BOT_BASE_URL`       | Homeserver URL for the bot account                                                                                   |
| `MATRIX_BOT_USERNAME`       | Bot account username                                                                                                 |
| `MATRIX_BOT_PASSWORD`       | Bot account password                                                                                                 |
| `MATRIX_BOT_DATA_DIRECTORY` | Directory for the bot's local state                                                                                  |
| `MATRIX_BOT_ADMINS`         | Comma-separated Matrix ids allowed to run admin commands                                                             |
| `MATRIX_BOT_USERS`          | Comma-separated Matrix ids allowed to use the bot                                                                    |
| `SONARR_API_KEY`            | Sonarr API key                                                                                                       |
| `RADARR_API_KEY`            | Radarr API key                                                                                                       |
| `LIDARR_API_KEY`            | Lidarr API key                                                                                                       |
| `BAZARR_API_KEY`            | Bazarr API key                                                                                                       |
| `JELLYFIN_API_KEY`          | Jellyfin API key                                                                                                     |
| `JELLYSTAT_API_TOKEN`       | Jellystat API token                                                                                                  |
| `GIPHY_API_KEY`             | Giphy API key (GIFs in notifications)                                                                                |
| `OPERATOR_API_KEY`          | Key shared with the operator for `/api/operator/*`; if unset, all operator calls are rejected with 401 (fail-closed) |

### Optional

| Variable                              | Default                 | Description                                     |
|---------------------------------------|-------------------------|-------------------------------------------------|
| `SONARR_BASE_URL`                     | `http://localhost:8080` | Sonarr URL                                      |
| `RADARR_BASE_URL`                     | `http://localhost:7878` | Radarr URL                                      |
| `LIDARR_BASE_URL`                     | `http://localhost:8686` | Lidarr URL                                      |
| `BAZARR_BASE_URL`                     | `http://localhost:6767` | Bazarr URL                                      |
| `JELLYFIN_BASE_URL`                   | `http://localhost:8081` | Jellyfin URL                                    |
| `JELLYSTAT_BASE_URL`                  | `http://localhost:8081` | Jellystat URL                                   |
| `GIPHY_BASE_URL`                      | `https://api.giphy.com` | Giphy API URL                                   |
| `GIPHY_RATING`                        | `g`                     | Content rating for GIF searches                 |
| `WEEKLY_REPORT_CRON`                  | `0 0 21 ? * SUN`        | Cron for the weekly Matrix report               |
| `SONARR_SYNC_EVERY`                   | `15m`                   | Sonarr stats sync interval                      |
| `RADARR_SYNC_EVERY`                   | `15m`                   | Radarr stats sync interval                      |
| `STATS_SYNC_INITIAL_DELAY`            | `0s`                    | Delay before the first stats sync               |
| `DOWNLOADS_SYNC_EVERY`                | `15m`                   | Downloads timeline sync interval                |
| `DOWNLOADS_SYNC_INITIAL_DELAY`        | `0s`                    | Delay before the first downloads sync           |
| `DOWNLOADS_SYNC_BACKFILL_DAYS`        | `30`                    | How far back to backfill the downloads timeline |
| `DOWNLOADS_SYNC_BAZARR_PAGE_LENGTH`   | `250`                   | Page size for Bazarr history queries            |
| `QUARKUS_OTEL_ENABLED`                | `true`                  | Enable OpenTelemetry tracing                    |
| `QUARKUS_OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4317` | OTLP collector endpoint                         |

### Operator

| Variable            | Default                       | Description                                               |
|---------------------|-------------------------------|-----------------------------------------------------------|
| `MANAGER_API_URL`   | `http://localhost:8080`       | URL of the homelab-manager API                            |
| `OPERATOR_API_KEY`  | `dev-operator-key`            | Key shared with the app (`X-Api-Key` header)              |
| `VPN_GATEWAYS`      | `internal`                    | Comma-separated gateway names implying `requiresVpn=true` |
| `ANNOTATION_PREFIX` | `homelab-manager.hoohoot.org` | Prefix of the `HTTPRoute` annotations                     |
| `DEFAULT_CATEGORY`  | `Uncategorized`               | Category for apps without a category annotation           |
| `SYNC_INTERVAL`     | `5m`                          | Full reconciliation sweep interval                        |
| `WATCH_NAMESPACES`  | all namespaces                | Comma-separated list of namespaces to watch               |

The annotation contract and RBAC requirements are documented in [operator/README.md](operator/README.md).

## License

This project is licensed under the [MIT License](LICENSE).
