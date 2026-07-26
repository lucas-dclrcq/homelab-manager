# Module « Téléchargements » — Plan d'implémentation

> **Pour Hermes :** utiliser le skill `subagent-driven-development` pour exécuter ce plan tâche par tâche.

**Objectif :** remplacer Jellyseerr — permettre aux utilisateurs du portail de découvrir des films/séries, d'en demander le téléchargement (Radarr/Sonarr), de suivre l'avancement et l'historique complet de chaque média, sans quitter le portail.

**Architecture :** nouveau module hexagonal `downloads` côté backend (`api` / `domain` / `infra`), alimenté par les APIs Radarr/Sonarr existantes (étendues : lookup, add, queue détaillée), un nouveau client TMDB (casting, bande-annonce, similaires), la table `playback_session` (visionnages) et les historiques *arr/Bazarr déjà synchronisés. Trois nouvelles tables : `media_request`, `media_event` (timeline à rétention illimitée), `media_issue` (signalements). Frontend : nouvelle page `/telechargements` (onglets Films/Séries + recherche commune) et page fiche média, branchées sur le client Orval régénéré.

**Stack :** Quarkus 3 + Kotlin, Hibernate Panache Reactive, PostgreSQL/Flyway, MicroProfile REST Client, Vue 3 + TS + Pinia + TanStack Query + Tailwind 4, Orval (client API généré), Vitest, WireMock.

---

## Contexte actuel (vérifié dans le dépôt)

- Clients `RadarrRestClient` / `SonarrRestClient` (`app/src/main/kotlin/org/hoohoot/homelab/manager/shared/arr/`) : ont déjà `getMovies`, `getSeries`, `getHistorySince`, `getQualityProfiles`, `getQueue` (Radarr, paginé), `deleteMovie`, `deleteSeries`, `updateSeries`. **Manquent** : lookup (recherche catalogue), add (POST movie/series), queue Sonarr, suppression d'items de queue, champs de progression (`size`, `sizeleft`) dans les modèles de queue.
- `DownloadsSyncService` (`library/infra/DownloadsSyncService.kt`) : synchronise toutes les 15 min les historiques Radarr/Sonarr/Lidarr/Bazarr vers `media_download` (dashboard « Activité »). Ne conserve que `downloadFolderImported` — les événements `grabbed`, `movieFileDeleted`, etc. sont ignorés.
- `playback_session` (module `statistics`) : sessions de visionnage Jellyfin avec `user_name`, `item_id`, `series_id`, `media_type`, `completed`.
- Webhooks `/api/notifications/{radarr,sonarr,bazarr,seerr}` : notifications Matrix uniquement, aucune persistance. **Aucune nouvelle notification Matrix** (décision spec).
- Module `problems` : workflow lourd de re-téléchargement (recherche release + grab). Le « Signaler un problème » de la spec est un signalement léger type issue Jellyseerr → nouvelle table `media_issue`, sans lien avec `problems` en v1.
- Frontend : pages dans `app/src/main/webui/src/pages/`, wrappers TanStack dans `src/lib/` (ex. `cleanupApi.ts`), client généré dans `src/api/` (Orval, regénéré au build/dev), composants UI réutilisables dans `src/components/ui/` (`BaseBadge`, `BaseModal`, `BaseTimeline`, `BaseSpinner`…), sidenav dans `src/components/app/AppShell.vue`, routes dans `src/router/index.ts`.
- Dernière migration : `V21`. Prochaines : `V22`, `V23`, `V24`.
- Tests d'intégration backend : `app/src/test/kotlin/org/hoohoot/homelab/manager/it/`, `@QuarkusTest` + `@TestSecurity` + WireMock injecté (`it/config/WireMockServerProducer.kt`), seeds via Panache dans `@BeforeEach` (voir `TimelineTest.kt`).
- Commentaires de code, descriptions d'API et résumés de tags OpenAPI en français ; messages de commit en anglais (Conventional Commits).

## Décisions de conception

1. **Identité média canonique = `tmdbId`** (films et séries ; Sonarr expose aussi `tmdbId`). `tvdbId` stocké en plus pour les séries (requis par Sonarr à l'ajout). Les événements, demandes et signalements sont indexés par `(media_type, tmdb_id)`.
2. **Timeline alimentée par polling**, pas par webhooks : extension de `DownloadsSyncService` (déjà schedulé) pour enregistrer aussi dans `media_event` les événements `grabbed` (début DL), `downloadFolderImported` (import ; 2ᵉ import d'un même média = `UPGRADED`), `movieFileDeleted`/`episodeFileDeleted`/`seriesDeleted` (suppression), historique Bazarr (sous-titres). Déduplication par `source_ref` unique (`radarr:history:1234`, `bazarr:episode:567`…).
3. **Visionnages** : job périodique qui agrège `playback_session` → un événement `WATCHED` par (média, utilisateur) au premier visionnage complété (`completed = true`), dédup `jellyfin:watch:{tmdbId}:{user}`.
4. **Statuts des listes** calculés côté serveur en fusionnant : bibliothèque *arr (`hasFile` / stats saisons), queue *arr (progression `sizeleft/size`, `trackedDownloadState` = warning/error → `PROBLEM`), `media_request` locales (demande en attente → `REQUESTED`). Tri « activité récente » = `MAX(media_event.occurred_at)` par média, repli sur la date d'ajout *arr. Cache en mémoire TTL 60 s pour ne pas marteler les *arr à chaque page.
5. **Demande en un clic** : le backend ajoute le média à Radarr/Sonarr avec profil qualité + dossier racine configurés côté serveur (`downloads.radarr.*`, `downloads.sonarr.*`), `monitored = true`, recherche immédiate (`searchForMovie` / `searchForMissingEpisodes`). Séries : `seasons[].monitored` selon cases cochées ; « suivre la série » → `monitorNewItems = "all"`.
6. **Annulation** : un endpoint `POST /api/downloads/{movies|series}/{tmdbId}/cancel` qui (a) retire les items de queue (`DELETE /queue/{id}?removeFromClient=true&blocklist=true`), (b) dé-monitore ce qui n'est pas encore téléchargé, (c) supprime l'entrée *arr si aucun fichier importé (`deleteFiles=false`), (d) marque la demande locale `CANCELLED`. Permission : le demandeur ou un admin si le demandeur est connu ; sinon tout utilisateur authentifié (médias historiques).
7. **Fiche média** : agrégation serveur d'un seul appel `GET /api/downloads/{movies|series}/{tmdbId}` → données *arr (fichier : qualité, taille, langues) + TMDB (`append_to_response=credits,videos,similar`) + demandes + signalements + lien Jellyfin (résolution `tmdbId` → item Jellyfin via `GET /Items?fields=ProviderIds`, URL `{JELLYFIN_PUBLIC_URL}/web/index.html#!/details?id=…`). Dégradation gracieuse si `TMDB_API_KEY` absent (sections casting/trailer/similaires vides).
8. **Événements séries regroupés** : endpoint timeline des séries avec regroupement SQL par `(event_type, season_number, jour)` — « 8 épisodes de la saison 2 importés ».

## Questions ouvertes (choix par défaut retenus, à valider)

- **Q1 — Annulation par un tiers :** par défaut, seuls le demandeur et un admin peuvent annuler une demande identifiée ; les téléchargements sans demandeur connu (ajoutés hors portail) sont annulables par tous. OK ?
- **Q2 — Résolution des signalements :** par défaut, rapporteur + admin peuvent résoudre. Pas de lien avec le module `problems` en v1 (un admin peut ensuite ouvrir un workflow de re-téléchargement à la main). OK ?
- **Q3 — Backfill timeline :** la sync historique repart de `downloads-sync.backfill-days` (30 j) comme le dashboard. Les événements plus anciens (avant déploiement du module) n'apparaîtront pas. OK ?
- **Q4 — Recherche commune :** un seul endpoint `GET /api/downloads/search?query=` renvoyant deux sections (films, séries), quelle que soit l'onglet actif. OK ?

## Fichiers créés / modifiés (vue d'ensemble)

**Backend — nouveau module `downloads`** (`app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/`) :
- `api/DownloadsResource.kt`, `api/DownloadsDtos.kt`
- `domain/MediaEvent.kt`, `domain/MediaStatus.kt`
- `domain/ports/MediaRequests.kt`, `MediaEvents.kt`, `MediaIssues.kt`, `MovieCatalog.kt`, `SeriesCatalog.kt`, `TmdbMetadata.kt`, `JellyfinItems.kt`
- `domain/usecases/ListLibraryMedia.kt`, `SearchCatalog.kt`, `RequestMedia.kt`, `CancelMedia.kt`, `GetMediaDetails.kt`, `ReportIssue.kt`, `ResolveIssue.kt`, `RecordArrHistoryEvents.kt`, `RecordWatchEvents.kt`
- `infra/MediaRequestEntity.kt`, `MediaRequestRepository.kt`, `MediaEventEntity.kt`, `MediaEventRepository.kt`, `MediaIssueEntity.kt`, `MediaIssueRepository.kt`, `RadarrMovieCatalog.kt`, `SonarrSeriesCatalog.kt`, `TmdbMetadataAdapter.kt`, `JellyfinItemsAdapter.kt`, `ArrCatalogCache.kt`

**Backend — modifications :**
- `shared/arr/radarr/RadarrRestClient.kt` + `radarr/Models.kt` (lookup, add, queue étendue, delete queue)
- `shared/arr/sonarr/SonarrRestClient.kt` + `sonarr/Models.kt` (lookup, add, queue, delete queue)
- `shared/tmdb/TmdbRestClient.kt` + `Models.kt` (nouveau)
- `library/infra/DownloadsSyncService.kt` (enregistrement des événements timeline)
- `jobs/MediaWatchEventsJob.kt` (nouveau)
- `app/src/main/resources/application.properties` (config module + client TMDB)
- `app/src/main/resources/db/migration/V22__create_media_request.sql`, `V23__create_media_event.sql`, `V24__create_media_issue.sql`

**Frontend** (`app/src/main/webui/src/`) : `pages/DownloadsPage.vue`, `pages/MediaDetailPage.vue`, `components/downloads/{MediaCard,MediaStatusBadge,MediaProgressBar,RequestModal,ReportIssueModal,MediaTimeline,IssuesPanel,FileDetails,SeasonList}.vue`, `lib/downloadsApi.ts`, `router/index.ts`, `components/app/AppShell.vue` + tests.

**DevOps/docs :** stubs WireMock (`app/src/main/resources/wiremock/mappings/`), `README.md` (table des variables), `charts/homelab-manager/` (env).

---

# Phase 0 — Fondations : migrations et persistance

## Tâche 1 : Table `media_request` + entité + repository

**Objectif :** persister les demandes utilisateur (qui, quand, quelles saisons, suivi de série).

**Fichiers :**
- Créer : `app/src/main/resources/db/migration/V22__create_media_request.sql`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/infra/MediaRequestEntity.kt`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/infra/MediaRequestRepository.kt`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/it/DownloadsRequestsRepositoryTest.kt`

**Étape 1 : écrire la migration**

```sql
CREATE TABLE media_request
(
    id            BIGSERIAL PRIMARY KEY,
    media_type    VARCHAR(10)  NOT NULL,
    tmdb_id       INT          NOT NULL,
    tvdb_id       INT,
    arr_id        INT,
    title         VARCHAR(512) NOT NULL,
    year          INT,
    poster_url    VARCHAR(1024),
    seasons       VARCHAR(255),
    follow_series BOOLEAN      NOT NULL DEFAULT FALSE,
    requested_by  VARCHAR(255) NOT NULL,
    requested_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    cancelled_at  TIMESTAMP,
    cancelled_by  VARCHAR(255)
);

CREATE INDEX idx_media_request_media ON media_request (media_type, tmdb_id);
CREATE INDEX idx_media_request_requester ON media_request (requested_by);
```

**Étape 2 : écrire l'entité**

```kotlin
package org.hoohoot.homelab.manager.downloads.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "media_request")
class MediaRequestEntity : PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "media_type", nullable = false, length = 10)
    lateinit var mediaType: String

    @Column(name = "tmdb_id", nullable = false)
    var tmdbId: Int = 0

    @Column(name = "tvdb_id")
    var tvdbId: Int? = null

    @Column(name = "arr_id")
    var arrId: Int? = null

    @Column(name = "title", nullable = false, length = 512)
    lateinit var title: String

    @Column(name = "year")
    var year: Int? = null

    @Column(name = "poster_url", length = 1024)
    var posterUrl: String? = null

    /** Saisons demandées, CSV ('1,2,3') ; NULL = toutes (films et séries complètes) */
    @Column(name = "seasons", length = 255)
    var seasons: String? = null

    @Column(name = "follow_series", nullable = false)
    var followSeries: Boolean = false

    @Column(name = "requested_by", nullable = false)
    lateinit var requestedBy: String

    @Column(name = "requested_at", nullable = false)
    var requestedAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "status", nullable = false, length = 20)
    var status: String = STATUS_ACTIVE

    @Column(name = "cancelled_at")
    var cancelledAt: LocalDateTime? = null

    @Column(name = "cancelled_by")
    var cancelledBy: String? = null

    companion object : PanacheCompanionBase<MediaRequestEntity, Long> {
        const val MEDIA_TYPE_MOVIE = "movie"
        const val MEDIA_TYPE_SERIES = "series"
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}
```

**Étape 3 : écrire le repository** (style aligné sur `MediaDownloadRepository`)

```kotlin
package org.hoohoot.homelab.manager.downloads.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MediaRequestRepository {

    suspend fun save(request: MediaRequestEntity): MediaRequestEntity =
        Panache.withTransaction { request.persistAndFlush() }.awaitSuspending().let { request }

    suspend fun findActiveFor(mediaType: String, tmdbId: Int): List<MediaRequestEntity> =
        Panache.withSession {
            MediaRequestEntity.find(
                "mediaType = ?1 and tmdbId = ?2 and status = ?3",
                mediaType, tmdbId, MediaRequestEntity.STATUS_ACTIVE,
            ).list()
        }.awaitSuspending()

    suspend fun findById(id: Long): MediaRequestEntity? =
        Panache.withSession { MediaRequestEntity.findById(id) }.awaitSuspending()

    suspend fun cancel(request: MediaRequestEntity, cancelledBy: String) =
        Panache.withTransaction {
            request.status = MediaRequestEntity.STATUS_CANCELLED
            request.cancelledAt = java.time.LocalDateTime.now()
            request.cancelledBy = cancelledBy
            request.persist()
        }.awaitSuspending()
}
```

**Étape 4 : test d'intégration** — seed + lecture, style `TimelineTest.kt` (Panache.withTransaction dans `VertxContextSupport.subscribeAndAwait`) : persister une demande, vérifier `findActiveFor` puis `cancel`.

Run : `./mvnw -pl app test -Dtest=DownloadsRequestsRepositoryTest`
Attendu : PASS (la migration V22 est appliquée au démarrage du test).

**Étape 5 : commit**

```bash
git add app/src/main/resources/db/migration/V22__create_media_request.sql app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/infra/MediaRequest*.kt app/src/test/kotlin/org/hoohoot/homelab/manager/it/DownloadsRequestsRepositoryTest.kt
git commit -m "feat(downloads): add media_request table and repository"
```

---

## Tâche 2 : Table `media_event` + entité + repository

**Objectif :** timeline média à rétention illimitée, dédupliquée par `source_ref`.

**Fichiers :**
- Créer : `app/src/main/resources/db/migration/V23__create_media_event.sql`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/infra/MediaEventEntity.kt`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/infra/MediaEventRepository.kt`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/it/MediaEventRepositoryTest.kt`

**Étape 1 : migration**

```sql
CREATE TABLE media_event
(
    id             BIGSERIAL PRIMARY KEY,
    media_type     VARCHAR(10)  NOT NULL,
    tmdb_id        INT          NOT NULL,
    tvdb_id        INT,
    arr_id         INT,
    event_type     VARCHAR(40)  NOT NULL,
    actor          VARCHAR(255),
    title          VARCHAR(512) NOT NULL,
    season_number  INT,
    episode_number INT,
    details        JSONB        NOT NULL DEFAULT '{}',
    source_ref     VARCHAR(255),
    occurred_at    TIMESTAMP    NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_media_event_source_ref UNIQUE (source_ref)
);

CREATE INDEX idx_media_event_media ON media_event (media_type, tmdb_id, occurred_at DESC, id DESC);
CREATE INDEX idx_media_event_arr ON media_event (media_type, arr_id);
CREATE INDEX idx_media_event_occurred ON media_event (occurred_at DESC, id DESC);
```

Types d'événements (constantes) : `REQUESTED`, `REQUEST_CANCELLED`, `DOWNLOAD_STARTED`, `DOWNLOADED`, `UPGRADED`, `SUBTITLES_DOWNLOADED`, `ISSUE_REPORTED`, `ISSUE_RESOLVED`, `MEDIA_DELETED`, `WATCHED`.

**Étape 2 : entité**

```kotlin
package org.hoohoot.homelab.manager.downloads.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "media_event")
class MediaEventEntity : PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "media_type", nullable = false, length = 10)
    lateinit var mediaType: String

    @Column(name = "tmdb_id", nullable = false)
    var tmdbId: Int = 0

    @Column(name = "tvdb_id")
    var tvdbId: Int? = null

    @Column(name = "arr_id")
    var arrId: Int? = null

    @Column(name = "event_type", nullable = false, length = 40)
    lateinit var eventType: String

    /** Utilisateur à l'origine de l'événement ; NULL = système / *arr */
    @Column(name = "actor")
    var actor: String? = null

    @Column(name = "title", nullable = false, length = 512)
    lateinit var title: String

    @Column(name = "season_number")
    var seasonNumber: Int? = null

    @Column(name = "episode_number")
    var episodeNumber: Int? = null

    /** quality, sizeBytes, languages, category, watcher… sérialisé en JSON */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", nullable = false, columnDefinition = "jsonb")
    var details: MutableMap<String, String> = mutableMapOf()

    /** Clé de dédup : 'radarr:history:1234', 'portal:request:55', 'jellyfin:watch:…' */
    @Column(name = "source_ref")
    var sourceRef: String? = null

    @Column(name = "occurred_at", nullable = false)
    lateinit var occurredAt: LocalDateTime

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    companion object : PanacheCompanionBase<MediaEventEntity, Long> {
        const val MEDIA_TYPE_MOVIE = "movie"
        const val MEDIA_TYPE_SERIES = "series"

        const val REQUESTED = "REQUESTED"
        const val REQUEST_CANCELLED = "REQUEST_CANCELLED"
        const val DOWNLOAD_STARTED = "DOWNLOAD_STARTED"
        const val DOWNLOADED = "DOWNLOADED"
        const val UPGRADED = "UPGRADED"
        const val SUBTITLES_DOWNLOADED = "SUBTITLES_DOWNLOADED"
        const val ISSUE_REPORTED = "ISSUE_REPORTED"
        const val ISSUE_RESOLVED = "ISSUE_RESOLVED"
        const val MEDIA_DELETED = "MEDIA_DELETED"
        const val WATCHED = "WATCHED"
    }
}
```

**Étape 3 : repository** — méthodes :
- `saveAllNew(candidates: List<MediaEventEntity>): Int` — dédup sur `sourceRef` (copier le pattern `saveNewDownloads` de `MediaDownloadRepository` : fetch des refs existantes, filtre, persist).
- `pageFor(mediaType: String, tmdbId: Int, page: Int, pageSize: Int): MediaEventPage` — tri `occurredAt DESC, id DESC` (pattern `findPage`).
- `groupedPageForSeries(tmdbId: Int, page: Int, pageSize: Int): GroupedEventPage` — requête native : `GROUP BY event_type, season_number, occurred_at::date, actor` avec `COUNT(*)`, `MIN(episode_number)`, `MAX(episode_number)`, `MAX(details::text)` ; pagination en SQL (`LIMIT/OFFSET`) + `COUNT(DISTINCT …)` pour le total. Projection vers un data class `GroupedMediaEvent(eventType, seasonNumber, day, episodeCount, firstEpisode, lastEpisode, details, actor)`.
- `latestActivityByMedia(): Map<Pair<String, Int>, LocalDateTime>` — `SELECT media_type, tmdb_id, MAX(occurred_at) FROM media_event GROUP BY 1, 2` (native query, une ligne par média) pour le tri « activité récente » des listes.
- `existsDownloadFor(mediaType, tmdbId): Boolean` — utilisé pour distinguer `DOWNLOADED` vs `UPGRADED`.

**Étape 4 : test IT** — persistance, dédup (même `source_ref` insérée deux fois → 1 ligne), regroupement séries (3 événements épisodes S02 même jour → 1 ligne groupée `episodeCount=3`), `latestActivityByMedia`.

Run : `./mvnw -pl app test -Dtest=MediaEventRepositoryTest` — attendu : PASS.

**Étape 5 : commit** `feat(downloads): add media_event table and repository`

---

## Tâche 3 : Table `media_issue` + entité + repository

**Objectif :** signalements utilisateur visibles de tous (équivalent issues Jellyseerr).

**Fichiers :**
- Créer : `app/src/main/resources/db/migration/V24__create_media_issue.sql`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/infra/MediaIssueEntity.kt`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/infra/MediaIssueRepository.kt`

**Étape 1 : migration**

```sql
CREATE TABLE media_issue
(
    id          BIGSERIAL PRIMARY KEY,
    media_type  VARCHAR(10) NOT NULL,
    tmdb_id     INT         NOT NULL,
    tvdb_id     INT,
    category    VARCHAR(30) NOT NULL,
    comment     TEXT,
    reported_by VARCHAR(255) NOT NULL,
    reported_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    status      VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    resolved_by VARCHAR(255),
    resolved_at TIMESTAMP
);

CREATE INDEX idx_media_issue_media ON media_issue (media_type, tmdb_id);
```

Catégories : `QUALITY`, `LANGUAGE`, `AUDIO`, `SUBTITLES`, `PLAYBACK`, `OTHER`.

**Étape 2 : entité + repository** — mêmes patterns que les tâches 1-2. Repository : `save`, `listFor(mediaType, tmdbId)` (OPEN d'abord puis date desc), `findById`, `resolve(issue, resolvedBy)`.

**Étape 3 : test IT** minimal (cycle report → resolve).

Run : `./mvnw -pl app test -Dtest=MediaIssueRepositoryTest` — attendu : PASS.

**Étape 4 : commit** `feat(downloads): add media_issue table and repository`

---

# Phase 1 — Extensions des clients REST

## Tâche 4 : Radarr — lookup, ajout, queue détaillée, suppression de queue

**Objectif :** tout ce qu'il faut pour chercher au catalogue, ajouter un film, suivre et annuler un téléchargement.

**Fichiers :**
- Modifier : `app/src/main/kotlin/org/hoohoot/homelab/manager/shared/arr/radarr/RadarrRestClient.kt`
- Modifier : `app/src/main/kotlin/org/hoohoot/homelab/manager/shared/arr/radarr/Models.kt`
- Test : couvert via les IT des tâches 12-17 (stubs WireMock).

**Étape 1 : modèles à ajouter dans `Models.kt`**

```kotlin
data class RadarrAddMovieRequest(
    val tmdbId: Int,
    val title: String,
    val titleSlug: String? = null,
    val year: Int? = null,
    val images: List<RadarrImage> = emptyList(),
    val qualityProfileId: Int,
    val rootFolderPath: String,
    val monitored: Boolean = true,
    val minimumAvailability: String = "released",
    val addOptions: RadarrAddOptions = RadarrAddOptions(),
)

data class RadarrAddOptions(val searchForMovie: Boolean = true)

data class RadarrRootFolder(val id: Int? = null, val path: String? = null, val freeSpace: Long? = null)
```

Étendre `RadarrQueueRecord` avec les champs de progression :

```kotlin
// ajouter à RadarrQueueRecord :
val size: Double? = null,
val sizeleft: Double? = null,
val timeleft: String? = null,
val status: String? = null,
val trackedDownloadStatus: String? = null,
val errorMessage: String? = null,
```

**Étape 2 : méthodes client à ajouter dans `RadarrRestClient`**

```kotlin
@GET
@Path("/movie/lookup")
suspend fun lookupMovies(@QueryParam("term") term: String): List<RadarrMovie>?

@POST
@Path("/movie")
@Produces(MediaType.APPLICATION_JSON)
@Retry(maxRetries = 0) // non-idempotent : un retry recréerait le film
suspend fun addMovie(request: RadarrAddMovieRequest): RadarrMovie?

@GET
@Path("/rootfolder")
suspend fun getRootFolders(): List<RadarrRootFolder>?

@DELETE
@Path("/queue/{id}")
suspend fun deleteQueueItem(
    @PathParam("id") id: Long,
    @QueryParam("removeFromClient") removeFromClient: Boolean,
    @QueryParam("blocklist") blocklist: Boolean,
)
```

**Étape 3 : vérifier la compilation** — `./mvnw -pl app compile` — attendu : BUILD SUCCESS.

**Étape 4 : commit** `feat(downloads): extend radarr client with lookup, add and queue management`

---

## Tâche 5 : Sonarr — lookup, ajout, queue, suppression de queue

**Objectif :** idem tâche 4 pour les séries, avec gestion des saisons et du suivi.

**Fichiers :**
- Modifier : `app/src/main/kotlin/org/hoohoot/homelab/manager/shared/arr/sonarr/SonarrRestClient.kt`
- Modifier : `app/src/main/kotlin/org/hoohoot/homelab/manager/shared/arr/sonarr/Models.kt`

**Étape 1 : modèles**

```kotlin
data class SonarrAddSeriesRequest(
    val tvdbId: Int,
    val title: String,
    val titleSlug: String? = null,
    val year: Int? = null,
    val images: List<Image> = emptyList(),
    val seasons: List<Season>,           // monitored positionné par saison
    val qualityProfileId: Int,
    val rootFolderPath: String,
    val seasonFolder: Boolean = true,
    val monitored: Boolean = true,
    val seriesType: String = "standard",
    val monitorNewItems: String = "none", // "all" = « suivre la série »
    val addOptions: SonarrAddOptions = SonarrAddOptions(),
)

data class SonarrAddOptions(val searchForMissingEpisodes: Boolean = true)

data class SonarrQueuePage(
    val totalRecords: Int? = null,
    val records: List<SonarrQueueRecord> = emptyList(),
)

data class SonarrQueueRecord(
    val id: Long? = null,
    val seriesId: Int? = null,
    val episodeId: Int? = null,
    val downloadId: String? = null,
    val title: String? = null,
    val size: Double? = null,
    val sizeleft: Double? = null,
    val timeleft: String? = null,
    val status: String? = null,
    val trackedDownloadState: String? = null,
    val trackedDownloadStatus: String? = null,
    val statusMessages: List<SonarrQueueStatusMessage> = emptyList(),
)

data class SonarrQueueStatusMessage(val title: String? = null, val messages: List<String> = emptyList())
```

`Season` existe déjà (`seasonNumber`, `monitored`) — réutilisé tel quel dans la requête d'ajout.

**Étape 2 : méthodes client**

```kotlin
@GET
@Path("/series/lookup")
suspend fun lookupSeries(@QueryParam("term") term: String): List<Series>?

@POST
@Path("/series")
@Produces(MediaType.APPLICATION_JSON)
@Retry(maxRetries = 0) // non-idempotent
suspend fun addSeries(request: SonarrAddSeriesRequest): Series?

@GET
@Path("/queue")
suspend fun getQueue(
    @QueryParam("page") page: Int,
    @QueryParam("pageSize") pageSize: Int,
    @QueryParam("includeUnknownSeriesItems") includeUnknownSeriesItems: Boolean,
): SonarrQueuePage?

@DELETE
@Path("/queue/{id}")
suspend fun deleteQueueItem(
    @PathParam("id") id: Long,
    @QueryParam("removeFromClient") removeFromClient: Boolean,
    @QueryParam("blocklist") blocklist: Boolean,
)
```

(Import `jakarta.ws.rs.POST` à ajouter — le client n'a que GET/PUT/DELETE aujourd'hui.)

**Étape 3 :** `./mvnw -pl app compile` — BUILD SUCCESS.

**Étape 4 : commit** `feat(downloads): extend sonarr client with lookup, add and queue management`

---

## Tâche 6 : Client TMDB (`shared/tmdb`)

**Objectif :** casting, réalisateurs, note, bande-annonce, similaires pour la fiche média.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/shared/tmdb/TmdbRestClient.kt`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/shared/tmdb/Models.kt`
- Modifier : `app/src/main/resources/application.properties`

**Étape 1 : client (API v3, clé en query param)**

```kotlin
package org.hoohoot.homelab.manager.shared.tmdb

import io.quarkus.rest.client.reactive.ClientQueryParam
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient(configKey = "tmdb-api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ClientQueryParam(name = "api_key", value = ["\${tmdb.api_key}"])
interface TmdbRestClient {

    @GET
    @Path("/3/movie/{id}")
    suspend fun getMovie(
        @PathParam("id") id: Int,
        @QueryParam("append_to_response") appendToResponse: String = "credits,videos,similar",
        @QueryParam("language") language: String = "fr-FR",
    ): TmdbMovieDetails?

    @GET
    @Path("/3/tv/{id}")
    suspend fun getSeries(
        @PathParam("id") id: Int,
        @QueryParam("append_to_response") appendToResponse: String = "credits,videos,similar",
        @QueryParam("language") language: String = "fr-FR",
    ): TmdbSeriesDetails?
}
```

**Étape 2 : modèles (`Models.kt`)**

```kotlin
package org.hoohoot.homelab.manager.shared.tmdb

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbMovieDetails(
    val id: Int? = null,
    val title: String? = null,
    val overview: String? = null,
    @param:JsonProperty("vote_average") val voteAverage: Double? = null,
    val credits: TmdbCredits? = null,
    val videos: TmdbVideos? = null,
    val similar: TmdbSimilarPage? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbSeriesDetails(
    val id: Int? = null,
    val name: String? = null,
    val overview: String? = null,
    @param:JsonProperty("vote_average") val voteAverage: Double? = null,
    val credits: TmdbCredits? = null,
    val videos: TmdbVideos? = null,
    val similar: TmdbSimilarPage? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbCredits(val cast: List<TmdbCastMember> = emptyList(), val crew: List<TmdbCrewMember> = emptyList())

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbCastMember(
    val name: String? = null,
    val character: String? = null,
    @param:JsonProperty("profile_path") val profilePath: String? = null,
    val order: Int? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbCrewMember(val name: String? = null, val job: String? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbVideos(val results: List<TmdbVideo> = emptyList())

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbVideo(val key: String? = null, val site: String? = null, val type: String? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbSimilarPage(val results: List<TmdbSimilarItem> = emptyList())

@JsonIgnoreProperties(ignoreUnknown = true)
data class TmdbSimilarItem(
    val id: Int? = null,
    val title: String? = null,        // films
    val name: String? = null,         // séries
    @param:JsonProperty("poster_path") val posterPath: String? = null,
    @param:JsonProperty("release_date") val releaseDate: String? = null,
    @param:JsonProperty("first_air_date") val firstAirDate: String? = null,
)

fun tmdbPosterUrl(path: String?): String? = path?.let { "https://image.tmdb.org/t/p/w342$it" }
fun tmdbProfileUrl(path: String?): String? = path?.let { "https://image.tmdb.org/t/p/w185$it" }
```

**Étape 3 : config (`application.properties`)**

```properties
# TMDB API (fiche média : casting, bande-annonce, similaires — optionnel)
quarkus.rest-client.tmdb-api.url=${TMDB_BASE_URL:https://api.themoviedb.org}
quarkus.rest-client.tmdb-api.scope=jakarta.inject.Singleton
tmdb.api_key=${TMDB_API_KEY:}
# En dev/test, pointer sur WireMock comme les autres clients :
# %dev.quarkus.rest-client.tmdb-api.url=http://localhost:${quarkus.wiremock.devservices.port}
# %test.quarkus.rest-client.tmdb-api.url=http://localhost:${quarkus.wiremock.devservices.port}
```

**Étape 4 :** `./mvnw -pl app compile` — BUILD SUCCESS.

**Étape 5 : commit** `feat(downloads): add tmdb rest client`

---

# Phase 2 — Timeline média (enregistrement des événements)

## Tâche 7 : Modèle domaine + ports de la timeline

**Objectif :** définir le contrat domaine du module `downloads` pour les événements.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/MediaEvent.kt`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/ports/MediaEvents.kt`

**Étape 1 : modèle domaine**

```kotlin
package org.hoohoot.homelab.manager.downloads.domain

import java.time.LocalDateTime

enum class MediaKind(val wire: String) {
    MOVIE("movie"), SERIES("series");

    companion object {
        fun fromWire(value: String): MediaKind = entries.first { it.wire == value }
    }
}

/** Événement de timeline avant persistance (domaine, sans annotation JPA) */
data class NewMediaEvent(
    val mediaKind: MediaKind,
    val tmdbId: Int,
    val tvdbId: Int? = null,
    val arrId: Int? = null,
    val eventType: String,           // constantes de MediaEventEntity (frontière infra acceptable, sinon enum dédié)
    val actor: String? = null,
    val title: String,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val details: Map<String, String> = emptyMap(),
    val sourceRef: String,
    val occurredAt: LocalDateTime,
)
```

**Étape 2 : port**

```kotlin
package org.hoohoot.homelab.manager.downloads.domain.ports

import org.hoohoot.homelab.manager.downloads.domain.MediaKind
import org.hoohoot.homelab.manager.downloads.domain.NewMediaEvent
import java.time.LocalDateTime

interface MediaEvents {
    /** Insère les événements nouveaux (dédup sur sourceRef), retourne le nombre inséré */
    suspend fun recordAll(events: List<NewMediaEvent>): Int
    suspend fun latestActivity(): Map<Pair<MediaKind, Int>, LocalDateTime>
}
```

L'adapter `downloads/infra/MediaEventRepository.kt` (tâche 2) implémente ce port (ajouter `implements MediaEvents` + mapping entité ↔ domaine). CDI résout l'injection (pattern du skill : port + adapter `@ApplicationScoped`).

**Étape 3 : commit** `feat(downloads): add media event domain model and port`

---

## Tâche 8 : Use case `RecordArrHistoryEvents` (mapping historique *arr → événements)

**Objectif :** transformer les enregistrements d'historique Radarr/Sonarr/Bazarr en événements de timeline dédupliqués.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/usecases/RecordArrHistoryEvents.kt`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/downloads/RecordArrHistoryEventsTest.kt` (test unitaire du mapping, sans Quarkus — AssertJ)

**Étape 1 : écrire le use case** (logique complète)

```kotlin
package org.hoohoot.homelab.manager.downloads.domain.usecases

import org.hoohoot.homelab.manager.downloads.domain.MediaKind
import org.hoohoot.homelab.manager.downloads.domain.NewMediaEvent
import org.hoohoot.homelab.manager.downloads.domain.ports.MediaEvents
import org.hoohoot.homelab.manager.downloads.infra.MediaEventEntity
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrHistoryRecord
import org.hoohoot.homelab.manager.shared.arr.sonarr.SonarrHistoryRecord
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime
import java.time.OffsetDateTime

@ApplicationScoped
class RecordArrHistoryEvents(private val mediaEvents: MediaEvents) {

    suspend fun recordRadarr(records: List<RadarrHistoryRecord>) {
        val events = records.mapNotNull { record ->
            val movie = record.movie ?: return@mapNotNull null
            val tmdbId = movie.tmdbId ?: return@mapNotNull null
            val title = movie.title ?: return@mapNotNull null
            val occurredAt = record.date.toLocalDateTime() ?: return@mapNotNull null
            val base = NewMediaEvent(
                mediaKind = MediaKind.MOVIE,
                tmdbId = tmdbId,
                arrId = record.movieId,
                title = title,
                sourceRef = "radarr:history:${record.id ?: return@mapNotNull null}",
                occurredAt = occurredAt,
                eventType = "", // positionné par le when ci-dessous
                details = buildMap {
                    record.quality?.quality?.name?.let { put("quality", it) }
                    movie.year?.let { put("year", it.toString()) }
                },
            )
            when (record.eventType) {
                "grabbed" -> base.copy(eventType = MediaEventEntity.DOWNLOAD_STARTED)
                "downloadFolderImported" -> base.copy(eventType = MediaEventEntity.DOWNLOADED)
                "movieFileDeleted" -> base.copy(eventType = MediaEventEntity.MEDIA_DELETED)
                else -> null
            }
        }
        mediaEvents.recordAll(events)
    }

    suspend fun recordSonarr(records: List<SonarrHistoryRecord>) {
        val events = records.mapNotNull { record ->
            val series = record.series ?: return@mapNotNull null
            val tmdbId = series.tmdbId ?: return@mapNotNull null
            val title = series.title ?: return@mapNotNull null
            val occurredAt = record.date.toLocalDateTime() ?: return@mapNotNull null
            val base = NewMediaEvent(
                mediaKind = MediaKind.SERIES,
                tmdbId = tmdbId,
                tvdbId = series.tvdbId,
                arrId = record.seriesId,
                title = title,
                seasonNumber = record.episode?.seasonNumber,
                episodeNumber = record.episode?.episodeNumber,
                sourceRef = "sonarr:history:${record.id ?: return@mapNotNull null}",
                occurredAt = occurredAt,
                eventType = "",
                details = buildMap {
                    record.quality?.quality?.name?.let { put("quality", it) }
                    record.episode?.title?.let { put("episodeTitle", it) }
                },
            )
            when (record.eventType) {
                "grabbed" -> base.copy(eventType = MediaEventEntity.DOWNLOAD_STARTED)
                "downloadFolderImported" -> base.copy(eventType = MediaEventEntity.DOWNLOADED)
                "episodeFileDeleted", "seriesDeleted" -> base.copy(eventType = MediaEventEntity.MEDIA_DELETED)
                else -> null
            }
        }
        mediaEvents.recordAll(events)
    }
}

private fun String?.toLocalDateTime(): LocalDateTime? =
    runCatching { OffsetDateTime.parse(this).toLocalDateTime() }.getOrNull()
```

Note : la distinction `DOWNLOADED` vs `UPGRADED` est faite dans `MediaEventRepository.recordAll` : pour un candidat `DOWNLOADED`, si un événement `DOWNLOADED`/`UPGRADED` existe déjà pour `(media_type, tmdb_id)`, retyper en `UPGRADED` avant insertion. (Requête `existsDownloadFor` par tmdbId présent dans les candidats — une seule requête `IN`.)

**Étape 2 : test unitaire** — vérifier le mapping (grabbed → DOWNLOAD_STARTED, import → DOWNLOADED, deleted → MEDIA_DELETED, records sans tmdbId ignorés, sourceRef formaté) avec un faux `MediaEvents` en mémoire.

Run : `./mvnw -pl app test -Dtest=RecordArrHistoryEventsTest` — attendu : PASS.

**Étape 3 : commit** `feat(downloads): map arr history records to media timeline events`

---

## Tâche 9 : Brancher la sync existante + événements Bazarr

**Objectif :** alimenter la timeline depuis `DownloadsSyncService` (polling existant, 15 min) — pas de nouveau scheduler.

**Fichiers :**
- Modifier : `app/src/main/kotlin/org/hoohoot/homelab/manager/library/infra/DownloadsSyncService.kt`
- Modifier : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/usecases/RecordArrHistoryEvents.kt` (ajout Bazarr)
- Test : modifier/étendre `app/src/test/kotlin/org/hoohoot/homelab/manager/it/TimelineTest.kt` ou nouveau `MediaEventsSyncTest.kt`

**Étape 1 :** injecter `RecordArrHistoryEvents` dans `DownloadsSyncService` ; dans `syncRadarr()`, après le fetch des records : `recordArrHistoryEvents.recordRadarr(records)` (dans un try/catch — un échec timeline ne casse jamais la sync du dashboard, pattern déjà utilisé pour `forceBlockedImports`). Idem dans `syncSonarr()`.

**Étape 2 : Bazarr** — dans `RecordArrHistoryEvents`, ajouter :

```kotlin
suspend fun recordBazarr(episodeItems: List<BazarrHistoryItem>, movieItems: List<BazarrHistoryItem>, arrIdResolver: suspend (MediaKind, Int) -> Pair<Int, Int>?) // (arrId) -> (tmdbId, tvdbId)?
```

Les items d'historique Bazarr portent `radarrId` / `sonarrSeriesId` (vérifier les modèles dans `shared/arr/bazarr/Models.kt` — ajouter les champs si absents). La résolution `arr_id → tmdb_id` passe par `ArrCatalogCache` (tâche 11) : map construite depuis `getMovies()`/`getSeries()`. Événement : `SUBTITLES_DOWNLOADED`, `details = {language, provider}`, `sourceRef = "bazarr:{movie|episode}:{id}"`, `seasonNumber`/`episodeNumber` pour les épisodes. Si la résolution échoue (média plus dans l'*arr) → log debug, skip.

**Étape 3 : test IT** — stub WireMock `/api/v3/history/since` avec `grabbed` + `downloadFolderImported` + `movieFileDeleted` (jeu déjà proche dans `TimelineTest`), lancer la sync via injection du service, vérifier les lignes `media_event` insérées avec les bons types et la dédup au second run.

Run : `./mvnw -pl app test -Dtest=MediaEventsSyncTest` — attendu : PASS.

**Étape 4 : commit** `feat(downloads): record timeline events during downloads sync`

---

## Tâche 10 : Job des visionnages agrégés (`WATCHED`)

**Objectif :** un événement « vu par X » au premier visionnage complété de chaque utilisateur.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/usecases/RecordWatchEvents.kt`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/jobs/MediaWatchEventsJob.kt`
- Modifier : `app/src/main/resources/application.properties`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/it/MediaWatchEventsTest.kt`

**Étape 1 : use case** — requête native sur `playback_session` :

```sql
SELECT media_type, item_id, series_id, user_name, MIN(started_at) AS first_watched_at
FROM playback_session
WHERE completed = true
GROUP BY media_type, item_id, series_id, user_name
```

Résolution Jellyfin `itemId` → `tmdbId` via `JellyfinItems` port (tâche 20 — anticiper : si la tâche 20 n'est pas faite, implémenter ici une version minimale du port `JellyfinItems.resolveTmdbIds(): Map<String, Pair<MediaKind, Int>>` basée sur `GET /Items?includeItemTypes=Movie,Series&recursive=true&fields=ProviderIds` ; pour les épisodes, `series_id` pointe l'item Série). Insérer `NewMediaEvent(eventType = WATCHED, actor = userName, sourceRef = "jellyfin:watch:{mediaType}:{tmdbId}:{userName}", occurredAt = firstWatchedAt)` — la dédup `source_ref` rend le job rejouable sans état.

**Étape 2 : job** (pattern `RadarrSyncJob`) :

```kotlin
@ApplicationScoped
class MediaWatchEventsJob(
    private val recordWatchEvents: RecordWatchEvents,
    private val jobRunner: JobRunner,
    @param:ConfigProperty(name = "media-watch-events.every") private val every: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Agrégation des visionnages (timeline médias)"
    override val schedule get() = "every $every"

    override suspend fun execute() = recordWatchEvents()

    @Scheduled(identity = IDENTITY, every = "{media-watch-events.every}", delayed = "{media-watch-events.initial-delay}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    @ActivateRequestContext
    suspend fun run() = jobRunner.runScheduled(this)

    companion object { const val IDENTITY = "media-watch-events" }
}
```

Config : `media-watch-events.every=1h`, `media-watch-events.initial-delay=2m`, `%test.media-watch-events.initial-delay=1h`.

**Étape 3 : test IT** — seed `playback_session` (pattern `it/config/PlaybackSessionSeed.kt`) : 2 sessions complétées du même user sur le même film + 1 d'un autre user → 2 événements `WATCHED`. Stub WireMock `/Items` (existe : `jellyfin-items-movies.json` — vérifier la présence de `ProviderIds`, sinon adapter le stub).

Run : `./mvnw -pl app test -Dtest=MediaWatchEventsTest` — attendu : PASS.

**Étape 4 : commit** `feat(downloads): aggregate first-watch events from playback sessions`

---

# Phase 3 — Listes et recherche

## Tâche 11 : Cache catalogue *arr (`ArrCatalogCache`)

**Objectif :** éviter d'appeler Radarr/Sonarr à chaque pagination/recherche ; source unique pour statuts et résolution d'IDs.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/infra/ArrCatalogCache.kt`

**Étape 1 : implémentation**

```kotlin
package org.hoohoot.homelab.manager.downloads.infra

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrMovie
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrQueueRecord
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrRestClient
import org.hoohoot.homelab.manager.shared.arr.sonarr.Series
import org.hoohoot.homelab.manager.shared.arr.sonarr.SonarrQueueRecord
import org.hoohoot.homelab.manager.shared.arr.sonarr.SonarrRestClient
import java.time.Instant

@ApplicationScoped
class ArrCatalogCache(
    @param:RestClient private val radarr: RadarrRestClient,
    @param:RestClient private val sonarr: SonarrRestClient,
) {
    private val ttlSeconds = 60L

    @Volatile private var moviesCache: Pair<Instant, List<RadarrMovie>>? = null
    @Volatile private var seriesCache: Pair<Instant, List<Series>>? = null
    @Volatile private var radarrQueueCache: Pair<Instant, List<RadarrQueueRecord>>? = null
    @Volatile private var sonarrQueueCache: Pair<Instant, List<SonarrQueueRecord>>? = null

    suspend fun movies(): List<RadarrMovie> = cached({ moviesCache }, { moviesCache = it }) {
        radarr.getMovies().orEmpty()
    }

    suspend fun series(): List<Series> = cached({ seriesCache }, { seriesCache = it }) {
        sonarr.getSeries().orEmpty()
    }

    suspend fun radarrQueue(): List<RadarrQueueRecord> = cached({ radarrQueueCache }, { radarrQueueCache = it }) {
        // pageSize large : la queue d'un homelab tient dans une page
        radarr.getQueue(1, 500, includeUnknownMovieItems = false)?.records.orEmpty()
    }

    suspend fun sonarrQueue(): List<SonarrQueueRecord> = cached({ sonarrQueueCache }, { sonarrQueueCache = it }) {
        sonarr.getQueue(1, 500, includeUnknownSeriesItems = false)?.records.orEmpty()
    }

    /** Invalide tout (appelé après demande/annulation pour refléter l'état frais) */
    fun invalidate() {
        moviesCache = null; seriesCache = null; radarrQueueCache = null; sonarrQueueCache = null
    }

    private suspend fun <T> cached(
        read: () -> Pair<Instant, List<T>>?,
        write: (Pair<Instant, List<T>>) -> Unit,
        fetch: suspend () -> List<T>,
    ): List<T> {
        val current = read()
        if (current != null && current.first.plusSeconds(ttlSeconds).isAfter(Instant.now())) return current.second
        val fresh = fetch()
        write(Instant.now() to fresh)
        return fresh
    }
}
```

**Étape 2 : commit** `feat(downloads): add arr catalog cache`

---

## Tâche 12 : Use case `ListLibraryMedia` (statuts, tri activité, filtre, pagination)

**Objectif :** liste paginée des médias connus de Radarr/Sonarr avec statut et progression.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/MediaStatus.kt`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/usecases/ListLibraryMedia.kt`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/downloads/ListLibraryMediaTest.kt` (unitaire, fakes en mémoire)

**Étape 1 : modèle de statut**

```kotlin
package org.hoohoot.homelab.manager.downloads.domain

enum class MediaStatus {
    REQUESTED,    // demandé, pas encore de fichier ni d'item en queue
    DOWNLOADING,  // présent dans la queue *arr
    PARTIAL,      // série : une partie des épisodes monitorés importée
    AVAILABLE,    // film : hasFile ; série : épisodes monitorés complets
    PROBLEM,      // item de queue en warning/error
}

data class LibraryMediaItem(
    val mediaKind: MediaKind,
    val tmdbId: Int,
    val tvdbId: Int? = null,
    val arrId: Int,
    val title: String,
    val year: Int? = null,
    val posterUrl: String? = null,
    val status: MediaStatus,
    val progressPercent: Int? = null,    // si DOWNLOADING
    val requestedBy: List<String> = emptyList(),
    val lastActivityAt: java.time.LocalDateTime? = null,
)

data class LibraryMediaPage(
    val items: List<LibraryMediaItem>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Int,
)
```

**Étape 2 : logique du use case (complète)**

```kotlin
package org.hoohoot.homelab.manager.downloads.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.downloads.domain.LibraryMediaItem
import org.hoohoot.homelab.manager.downloads.domain.LibraryMediaPage
import org.hoohoot.homelab.manager.downloads.domain.MediaKind
import org.hoohoot.homelab.manager.downloads.domain.MediaStatus
import org.hoohoot.homelab.manager.downloads.domain.ports.MediaEvents
import org.hoohoot.homelab.manager.downloads.infra.ArrCatalogCache
import org.hoohoot.homelab.manager.downloads.infra.MediaRequestRepository
import java.time.LocalDateTime
import java.time.OffsetDateTime

@ApplicationScoped
class ListLibraryMedia(
    private val catalog: ArrCatalogCache,
    private val requests: MediaRequestRepository,
    private val mediaEvents: MediaEvents,
) {
    suspend fun movies(status: MediaStatus?, page: Int, pageSize: Int): LibraryMediaPage {
        val queueByMovie = catalog.radarrQueue().groupBy { it.movieId }
        val activity = mediaEvents.latestActivity()
        val items = catalog.movies().mapNotNull { movie ->
            val arrId = movie.id ?: return@mapNotNull null
            val tmdbId = movie.tmdbId ?: return@mapNotNull null
            val queue = queueByMovie[arrId].orEmpty()
            LibraryMediaItem(
                mediaKind = MediaKind.MOVIE,
                tmdbId = tmdbId,
                arrId = arrId,
                title = movie.title ?: "Sans titre",
                year = movie.year,
                posterUrl = movie.images.firstOrNull { it.coverType == "poster" }?.remoteUrl,
                status = computeStatus(queue.map { it.trackedDownloadState to it.statusMessages.isNotEmpty() }, queue.isNotEmpty(), movie.hasFile == true, null),
                progressPercent = progressOf(queue.map { it.size to it.sizeleft }),
                lastActivityAt = activity[MediaKind.MOVIE to tmdbId] ?: movie.added.toLocalDateTime(),
            )
        }
        return filterSortPaginate(items, status, page, pageSize)
    }

    suspend fun series(status: MediaStatus?, page: Int, pageSize: Int): LibraryMediaPage {
        val queueBySeries = catalog.sonarrQueue().groupBy { it.seriesId }
        val activity = mediaEvents.latestActivity()
        val items = catalog.series().mapNotNull { series ->
            val arrId = series.id ?: return@mapNotNull null
            val tmdbId = series.tmdbId ?: return@mapNotNull null
            val queue = queueBySeries[arrId].orEmpty()
            val monitoredSeasons = series.seasons.orEmpty().filter { it.monitored == true && (it.seasonNumber ?: 0) > 0 }
            val complete = monitoredSeasons.isNotEmpty() && monitoredSeasons.all {
                val s = it.statistics
                s != null && (s.episodeFileCount ?: 0) >= (s.episodeCount ?: Int.MAX_VALUE)
            }
            val hasAnyFile = series.statistics?.let { (it.episodeFileCount ?: 0) > 0 } == true
            LibraryMediaItem(
                mediaKind = MediaKind.SERIES,
                tmdbId = tmdbId,
                tvdbId = series.tvdbId,
                arrId = arrId,
                title = series.title ?: "Sans titre",
                year = series.year,
                posterUrl = series.images?.firstOrNull { it.coverType == "poster" }?.remoteUrl,
                status = computeStatus(queue.map { it.trackedDownloadState to it.statusMessages.isNotEmpty() }, queue.isNotEmpty(), complete, hasAnyFile),
                progressPercent = progressOf(queue.map { it.size to it.sizeleft }),
                lastActivityAt = activity[MediaKind.SERIES to tmdbId] ?: series.added.toLocalDateTime(),
            )
        }
        return filterSortPaginate(items, status, page, pageSize)
    }

    private fun computeStatus(
        queueProblems: List<Pair<String?, Boolean>>,   // (trackedDownloadState, hasStatusMessages)
        inQueue: Boolean,
        fullyAvailable: Boolean,
        hasAnyFile: Boolean?,
    ): MediaStatus = when {
        queueProblems.any { it.first.equals("warning", true) || it.first.equals("error", true) || it.second } -> MediaStatus.PROBLEM
        inQueue -> MediaStatus.DOWNLOADING
        fullyAvailable -> MediaStatus.AVAILABLE
        hasAnyFile == true -> MediaStatus.PARTIAL
        else -> MediaStatus.REQUESTED
    }

    private fun progressOf(sizes: List<Pair<Double?, Double?>>): Int? {
        val total = sizes.sumOf { it.first ?: 0.0 }
        if (total <= 0.0) return null
        val left = sizes.sumOf { it.second ?: 0.0 }
        return (((total - left) / total) * 100).toInt().coerceIn(0, 100)
    }

    private fun filterSortPaginate(items: List<LibraryMediaItem>, status: MediaStatus?, page: Int, pageSize: Int): LibraryMediaPage {
        val filtered = items
            .filter { status == null || matchesFilter(it.status, status) }
            .sortedWith(compareByDescending<LibraryMediaItem> { it.lastActivityAt }.thenBy { it.title.lowercase() })
        val from = (page * pageSize).coerceAtMost(filtered.size)
        val to = (from + pageSize).coerceAtMost(filtered.size)
        return LibraryMediaPage(filtered.subList(from, to), page, pageSize, filtered.size)
    }

    private fun matchesFilter(actual: MediaStatus, filter: MediaStatus): Boolean = when (filter) {
        // « En cours » regroupe demandé, en téléchargement et partiel
        MediaStatus.DOWNLOADING -> actual in setOf(MediaStatus.DOWNLOADING, MediaStatus.REQUESTED, MediaStatus.PARTIAL)
        MediaStatus.REQUESTED -> actual == MediaStatus.REQUESTED
        else -> actual == filter
    }
}

private fun String?.toLocalDateTime(): LocalDateTime? =
    runCatching { OffsetDateTime.parse(this).toLocalDateTime() }.getOrNull()
```

Le filtre exposé côté API : `status=IN_PROGRESS|AVAILABLE|PROBLEM` — mapper `IN_PROGRESS → DOWNLOADING` dans la couche api (tâche 14).

**Étape 3 : test unitaire** — fakes : catalogue avec 1 film hasFile, 1 film en queue à 42 %, 1 film en queue warning ; 1 série complète, 1 série partielle. Vérifier statuts, progression, tri (activité récente d'abord), filtre `IN_PROGRESS`, pagination.

Run : `./mvnw -pl app test -Dtest=ListLibraryMediaTest` — attendu : PASS.

**Étape 4 : commit** `feat(downloads): list library media with status, progress and activity sort`

---

## Tâche 13 : Use case `SearchCatalog` (découverte + statut des médias présents)

**Objectif :** recherche commune films+séries mêlant médias déjà présents (avec statut) et absents (demandables).

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/usecases/SearchCatalog.kt`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/downloads/SearchCatalogTest.kt`

**Étape 1 : implémentation**

```kotlin
package org.hoohoot.homelab.manager.downloads.domain.usecases

data class CatalogSearchResult(
    val mediaKind: MediaKind,
    val tmdbId: Int,
    val tvdbId: Int?,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
    val overview: String?,
    val inLibrary: Boolean,
    val status: MediaStatus?,       // null si absent de la bibliothèque
)

data class CatalogSearchResults(val movies: List<CatalogSearchResult>, val series: List<CatalogSearchResult>)
```

Logique :
1. Appeler en parallèle `radarr.lookupMovies(term)` et `sonarr.lookupSeries(term)` (Kotlin `coroutineScope { async {} ; async {} }`).
2. Croiser avec `ArrCatalogCache.movies()/series()` (par `tmdbId`) : résultat présent → `inLibrary = true` + statut calculé (réutiliser `ListLibraryMedia` — extraire `computeStatus` dans un objet partagé `MediaStatusResolver` pour rester DRY).
3. Film lookup : `tmdbId`, `title`, `year`, poster (`images.remoteUrl`), `overview`. Série lookup : `tmdbId` (absent de certains résultats TVDB-only → les ignorer en v1 et logger), `tvdbId`, `title`, `firstAired` → année.
4. Limiter chaque section à 20 résultats. `query < 2 caractères` → listes vides (garde côté resource, pattern `ProblemsResource.searchMovies`).

**Étape 2 : test unitaire** — faux clients : résultat déjà présent (statut remonté), résultat absent (`inLibrary=false`, `status=null`), série sans `tmdbId` (ignorée).

Run : `./mvnw -pl app test -Dtest=SearchCatalogTest` — attendu : PASS.

**Étape 3 : commit** `feat(downloads): search radarr/sonarr catalog with in-library status`

---

## Tâche 14 : Resource REST — listes et recherche

**Objectif :** exposer `GET /api/downloads/movies`, `/series`, `/search`.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/api/DownloadsDtos.kt`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/api/DownloadsResource.kt`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/it/DownloadsLibraryTest.kt`

**Étape 1 : DTOs** (`DownloadsDtos.kt`, complétés aux tâches suivantes)

```kotlin
package org.hoohoot.homelab.manager.downloads.api

data class LibraryMediaDto(
    val mediaType: String,
    val tmdbId: Int,
    val tvdbId: Int? = null,
    val title: String,
    val year: Int? = null,
    val posterUrl: String? = null,
    val status: String,               // REQUESTED | DOWNLOADING | PARTIAL | AVAILABLE | PROBLEM
    val progressPercent: Int? = null,
    val requestedBy: List<String> = emptyList(),
)

data class LibraryMediaPageDto(
    val items: List<LibraryMediaDto>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalCount: Int,
)

data class CatalogSearchResultDto(
    val mediaType: String,
    val tmdbId: Int,
    val tvdbId: Int? = null,
    val title: String,
    val year: Int? = null,
    val posterUrl: String? = null,
    val overview: String? = null,
    val inLibrary: Boolean,
    val status: String? = null,
)

data class CatalogSearchDto(val movies: List<CatalogSearchResultDto>, val series: List<CatalogSearchResultDto>)
```

**Étape 2 : resource**

```kotlin
package org.hoohoot.homelab.manager.downloads.api

import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.downloads.domain.MediaStatus
import org.hoohoot.homelab.manager.downloads.domain.usecases.ListLibraryMedia
import org.hoohoot.homelab.manager.downloads.domain.usecases.SearchCatalog

@Path("/api/downloads")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Téléchargements", description = "Découverte, demandes et suivi des téléchargements films/séries")
class DownloadsResource(
    private val listLibraryMedia: ListLibraryMedia,
    private val searchCatalog: SearchCatalog,
) {
    companion object {
        private const val DEFAULT_PAGE_SIZE = 24
        private const val MAX_PAGE_SIZE = 100
    }

    @GET
    @Path("/movies")
    suspend fun movies(
        @QueryParam("status") status: String?,
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("pageSize") @DefaultValue("24") pageSize: Int,
    ): LibraryMediaPageDto =
        listLibraryMedia.movies(status.toStatusFilter(), page.coerceAtLeast(0), pageSize.coerceIn(1, MAX_PAGE_SIZE)).toDto()

    @GET
    @Path("/series")
    suspend fun series(
        @QueryParam("status") status: String?,
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("pageSize") @DefaultValue("24") pageSize: Int,
    ): LibraryMediaPageDto =
        listLibraryMedia.series(status.toStatusFilter(), page.coerceAtLeast(0), pageSize.coerceIn(1, MAX_PAGE_SIZE)).toDto()

    @GET
    @Path("/search")
    suspend fun search(@QueryParam("query") query: String?): CatalogSearchDto {
        if (query.isNullOrBlank() || query.trim().length < 2) return CatalogSearchDto(emptyList(), emptyList())
        return searchCatalog(query.trim()).toDto()
    }

    private fun String?.toStatusFilter(): MediaStatus? = when (this?.uppercase()) {
        "IN_PROGRESS" -> MediaStatus.DOWNLOADING   // regroupe REQUESTED/DOWNLOADING/PARTIAL
        "AVAILABLE" -> MediaStatus.AVAILABLE
        "PROBLEM" -> MediaStatus.PROBLEM
        else -> null
    }
}
```

(+ fonctions `toDto()` de mapping domaine → DTO dans `DownloadsDtos.kt`.)

**Étape 3 : test IT** (`DownloadsLibraryTest`) — `@TestSecurity(user = "alice", roles = ["user"])`, stubs WireMock `GET /api/v3/movie`, `/api/v3/queue`, `/api/v3/series` (les stubs `radarr-movies.json`, `radarr-queue.json`, `sonarr-series.json` existent pour le dev — pour les tests, enregistrer des stubs dédiés via `wireMock.register(...)` comme dans `TimelineTest`) ; vérifier : liste films paginée avec statuts, filtre `status=PROBLEM`, recherche (stub `/api/v3/movie/lookup` + `/api/v3/series/lookup`), recherche < 2 caractères → vide, non-authentifié → 401.

Run : `./mvnw -pl app test -Dtest=DownloadsLibraryTest` — attendu : PASS.

**Étape 4 : commit** `feat(downloads): expose library lists and catalog search endpoints`

---

# Phase 4 — Demandes de téléchargement

## Tâche 15 : Use case `RequestMedia` — films

**Objectif :** ajout du film à Radarr avec le profil par défaut serveur + événement `REQUESTED` + demande persistée.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/ports/MovieCatalog.kt` + `SeriesCatalog.kt` (ports lookup/add/queue/cancel)
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/infra/RadarrMovieCatalog.kt` + `SonarrSeriesCatalog.kt`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/usecases/RequestMedia.kt`
- Modifier : `app/src/main/resources/application.properties`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/downloads/RequestMediaTest.kt`

**Étape 1 : config**

```properties
# Module Téléchargements : valeurs par défaut appliquées aux demandes (l'utilisateur ne choisit rien)
downloads.radarr.quality-profile-id=${DOWNLOADS_RADARR_QUALITY_PROFILE_ID:0}
downloads.radarr.root-folder=${DOWNLOADS_RADARR_ROOT_FOLDER:}
downloads.sonarr.quality-profile-id=${DOWNLOADS_SONARR_QUALITY_PROFILE_ID:0}
downloads.sonarr.root-folder=${DOWNLOADS_SONARR_ROOT_FOLDER:}
downloads.jellyfin-public-url=${JELLYFIN_PUBLIC_URL:}
```

Si `quality-profile-id = 0` ou root-folder vide : au premier usage, résolution automatique = premier profil qualité + premier root folder de l'*arr (log explicite au démarrage). Ça évite un blocage en dev tout en restant configurable en prod. (Documenter dans le README.)

**Étape 2 : ports**

```kotlin
interface MovieCatalog {
    suspend fun lookup(term: String): List<CatalogMovie>
    suspend fun findByTmdbId(tmdbId: Int): CatalogMovie?       // depuis ArrCatalogCache
    suspend fun add(request: AddMovie): CatalogMovie
    suspend fun queueItems(arrMovieId: Int): List<QueueItem>
    suspend fun removeFromQueue(queueItemId: Long)
    suspend fun setMonitored(arrMovieId: Int, monitored: Boolean)
    suspend fun deleteIfNoFile(arrMovieId: Int): Boolean
    suspend fun defaultQualityProfileId(): Int
    suspend fun defaultRootFolder(): String
}
// SeriesCatalog symétrique + seasons/monitored + monitorNewItems
```

Data classes domaine : `CatalogMovie(tmdbId, arrId?, title, year, posterUrl, overview, hasFile)`, `AddMovie(...)`, `QueueItem(id, sizeBytes, sizeLeftBytes, state, messages)`.

**Étape 3 : use case `RequestMedia`**

```kotlin
sealed interface RequestResult {
    data class Granted(val request: MediaRequestEntity) : RequestResult
    data object AlreadyPresent : RequestResult      // déjà dans la bibliothèque
    data object AlreadyRequested : RequestResult    // demande ACTIVE existante sur le même média
    data object NotFound : RequestResult            // tmdbId inconnu du lookup *arr
}

suspend fun movie(username: String, tmdbId: Int): RequestResult {
    movieCatalog.findByTmdbId(tmdbId)?.let { if (it.hasFile || it.arrId != null) return RequestResult.AlreadyPresent }
    if (requests.findActiveFor(MediaKind.MOVIE.wire, tmdbId).isNotEmpty()) return RequestResult.AlreadyRequested
    val found = movieCatalog.lookup("tmdb:$tmdbId").firstOrNull { it.tmdbId == tmdbId } ?: return RequestResult.NotFound
    val added = movieCatalog.add(AddMovie(found, movieCatalog.defaultQualityProfileId(), movieCatalog.defaultRootFolder()))
    val saved = requests.save(MediaRequestEntity().apply {
        mediaType = MediaRequestEntity.MEDIA_TYPE_MOVIE
        this.tmdbId = tmdbId; arrId = added.arrId; title = added.title; year = added.year
        posterUrl = added.posterUrl; requestedBy = username
    })
    mediaEvents.recordAll(listOf(NewMediaEvent(
        mediaKind = MediaKind.MOVIE, tmdbId = tmdbId, arrId = added.arrId,
        eventType = MediaEventEntity.REQUESTED, actor = username, title = added.title,
        sourceRef = "portal:request:${saved.id}", occurredAt = LocalDateTime.now(),
    )))
    catalogCache.invalidate()
    return RequestResult.Granted(saved)
}
```

Astuce : le lookup Radarr accepte `term = "tmdb:{id}"` (recherche par ID TMDB exact) — parfait pour récupérer le payload d'ajout complet (titleSlug, images, année).

**Étape 4 : test unitaire** — chemins Granted / AlreadyPresent / AlreadyRequested / NotFound avec fakes ; vérifier payload `add` (profil par défaut, `searchForMovie = true`) et l'événement enregistré.

Run : `./mvnw -pl app test -Dtest=RequestMediaTest` — attendu : PASS.

**Étape 5 : commit** `feat(downloads): request movie downloads with server-side defaults`

---

## Tâche 16 : `RequestMedia` — séries (saisons cochées + suivi)

**Objectif :** demande par saison avec option « suivre la série ».

**Fichiers :** modifier `RequestMedia.kt` + `SonarrSeriesCatalog.kt` ; test : ajouter cas dans `RequestMediaTest.kt`.

**Étape 1 : logique**

```kotlin
suspend fun series(username: String, tmdbId: Int, seasons: Set<Int>?, followSeries: Boolean): RequestResult {
    // seasons = null → toutes les saisons ; sinon sous-ensemble coché (saison 0 = spéciaux : exclue sauf demande explicite)
    // lookup "tvdb:{tvdbId}" impossible sans tvdbId → lookup par tmdbId côté portail :
    //   le port résout tmdbId → série lookup Sonarr (Sonarr lookup accepte aussi un terme libre ;
    //   on filtre les résultats par tmdbId exact)
    // add : seasons mappées avec monitored = (seasons == null || it.seasonNumber in seasons)
    //       monitorNewItems = if (followSeries) "all" else "none"
    // persist MediaRequestEntity(seasons = seasons?.joinToString(","), followSeries = followSeries)
    // événement REQUESTED avec details["seasons"] = "1,2" si sous-ensemble, details["follow"] = "true"
}
```

**Étape 2 : tests** — demande saisons 1 et 3 (payload `monitored` correct), suivi (`monitorNewItems = "all"`), série déjà présente → AlreadyPresent.

Run : `./mvnw -pl app test -Dtest=RequestMediaTest` — attendu : PASS.

**Étape 3 : commit** `feat(downloads): request series by season with follow option`

---

## Tâche 17 : Use case `CancelMedia` + endpoints demandes

**Objectif :** annulation complète (queue + monitoring + suppression si rien d'importé + statut demande + événement) et exposition REST des demandes.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/usecases/CancelMedia.kt`
- Modifier : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/api/DownloadsResource.kt` + `DownloadsDtos.kt`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/downloads/CancelMediaTest.kt` (unitaire) + cas IT dans `DownloadsRequestsTest.kt`

**Étape 1 : logique `CancelMedia`**

```kotlin
suspend fun cancel(username: String, isAdmin: Boolean, kind: MediaKind, tmdbId: Int): CancelResult {
    val activeRequests = requests.findActiveFor(kind.wire, tmdbId)
    val knownRequesters = activeRequests.map { it.requestedBy }.toSet()
    if (knownRequesters.isNotEmpty() && username !in knownRequesters && !isAdmin) return CancelResult.Forbidden

    when (kind) {
        MediaKind.MOVIE -> movieCatalog.findByTmdbId(tmdbId)?.arrId?.let { arrId ->
            movieCatalog.queueItems(arrId).forEach { movieCatalog.removeFromQueue(it.id) } // removeFromClient + blocklist
            if (!movieCatalog.deleteIfNoFile(arrId)) movieCatalog.setMonitored(arrId, false)
        }
        MediaKind.SERIES -> seriesCatalog.findByTmdbId(tmdbId)?.arrId?.let { arrId ->
            seriesCatalog.queueItems(arrId).forEach { seriesCatalog.removeFromQueue(it.id) }
            if (!seriesCatalog.deleteIfNoFile(arrId)) seriesCatalog.unmonitorUndownloadedSeasons(arrId)
        }
    }
    activeRequests.forEach { requests.cancel(it, username) }
    mediaEvents.recordAll(listOf(NewMediaEvent(kind, tmdbId, eventType = MediaEventEntity.REQUEST_CANCELLED, actor = username, title = activeRequests.firstOrNull()?.title ?: titleFromCatalog, sourceRef = "portal:cancel:{kind}:{tmdbId}:{epochMillis}", occurredAt = now)))
    catalogCache.invalidate()
    return CancelResult.Cancelled
}
```

`sourceRef` unique par annulation (epoch) — une annulation n'est pas dédupliquable. `deleteIfNoFile` : `DELETE /movie/{id}?deleteFiles=false&addImportExclusion=false` seulement si `hasFile == false` (film) / aucune saison avec fichier (série) ; retourne `true` si supprimé.

**Étape 2 : endpoints REST**

```kotlin
data class CreateMediaRequest(
    val mediaType: String?,          // "movie" | "series"
    val tmdbId: Int?,
    val seasons: List<Int>? = null,  // séries : saisons cochées ; null/absent = toutes
    val followSeries: Boolean = false,
)

data class MediaRequestDto(
    val id: Long, val mediaType: String, val tmdbId: Int, val title: String,
    val year: Int?, val posterUrl: String?, val seasons: List<Int>?, val followSeries: Boolean,
    val requestedBy: String, val requestedAt: java.time.LocalDateTime,
)
```

```kotlin
@POST
@Path("/requests")
@APIResponseSchema(value = MediaRequestDto::class, responseCode = "201")
suspend fun createRequest(@Valid request: CreateMediaRequest): Response = when (request.mediaType) {
    "movie" -> requestMedia.movie(username, requireNotNull(request.tmdbId)).toResponse()
    "series" -> requestMedia.series(username, requireNotNull(request.tmdbId), request.seasons?.toSet(), request.followSeries).toResponse()
    else -> badRequest("mediaType doit valoir 'movie' ou 'series'")
}
// RequestResult.AlreadyPresent → 409 "Déjà présent dans la bibliothèque"
// RequestResult.AlreadyRequested → 409 "Déjà demandé"
// RequestResult.NotFound → 404
// (helpers badRequest/conflict/notFound de shared/api/Responses.kt)

@POST
@Path("/{kind}/{tmdbId}/cancel")
suspend fun cancel(@PathParam("kind") kind: String, @PathParam("tmdbId") tmdbId: Int): Response =
    cancelMedia(username, isAdmin, MediaKind.fromWire(kind), tmdbId).toResponse()
// CancelResult.Forbidden → 403 ; Cancelled → 204
```

`isAdmin` : `identity.roles.contains("admin")` (pattern du user store frontend).

**Étape 3 : tests** — unitaire : chemins Cancelled / Forbidden / sans demandeur connu ; IT : POST demande film (stub lookup `tmdb:` + POST `/api/v3/movie` + GET qualityprofile/rootfolder), 409 au second POST, cancel (vérifier `DELETE /api/v3/queue/…` et `DELETE /api/v3/movie/…` appelés via `wireMock.verify(...)`), non-admin annulant la demande d'autrui → 403.

Run : `./mvnw -pl app test -Dtest='CancelMediaTest,DownloadsRequestsTest'` — attendu : PASS.

**Étape 4 : commit** `feat(downloads): request and cancel download endpoints`

---

# Phase 5 — Signalements

## Tâche 18 : Use cases `ReportIssue` / `ResolveIssue` + endpoints

**Objectif :** signalement visible de tous, présent dans la timeline ; résolution tracée.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/usecases/ReportIssue.kt`, `ResolveIssue.kt`
- Modifier : `api/DownloadsResource.kt`, `api/DownloadsDtos.kt`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/it/DownloadsIssuesTest.kt`

**Étape 1 : use cases**

`ReportIssue(username, kind, tmdbId, category, comment?)` :
- valide `category` ∈ {QUALITY, LANGUAGE, AUDIO, SUBTITLES, PLAYBACK, OTHER} (sinon 400)
- persiste `MediaIssueEntity(status = OPEN)` + événement `ISSUE_REPORTED` (`details = {category, comment}`, `sourceRef = "portal:issue:{id}:reported"`)
- retourne le DTO créé

`ResolveIssue(username, isAdmin, issueId)` :
- 403 si `username != issue.reportedBy && !isAdmin`
- idempotent : déjà `RESOLVED` → 409
- `repository.resolve(...)` + événement `ISSUE_RESOLVED` (`sourceRef = "portal:issue:{id}:resolved"`)

**Étape 2 : endpoints**

```kotlin
@POST @Path("/{kind}/{tmdbId}/issues")
suspend fun reportIssue(@PathParam("kind") kind: String, @PathParam("tmdbId") tmdbId: Int, @Valid body: ReportIssueRequest): Response // 201 + IssueDto ; 400 catégorie invalide

@POST @Path("/issues/{id}/resolve")
suspend fun resolveIssue(@PathParam("id") id: Long): Response // 200 + IssueDto ; 403 ; 404 ; 409 déjà résolu
```

```kotlin
data class ReportIssueRequest(val category: String?, val comment: String? = null)
data class MediaIssueDto(
    val id: Long, val mediaType: String, val tmdbId: Int, val category: String, val comment: String?,
    val reportedBy: String, val reportedAt: LocalDateTime, val status: String,
    val resolvedBy: String?, val resolvedAt: LocalDateTime?,
)
```

**Étape 3 : test IT** — report → 201 ; catégorie inconnue → 400 ; resolve par le rapporteur → 200 ; resolve par un tiers non-admin → 403 ; double resolve → 409 ; événements `ISSUE_REPORTED`/`ISSUE_RESOLVED` présents en base.

Run : `./mvnw -pl app test -Dtest=DownloadsIssuesTest` — attendu : PASS.

**Étape 4 : commit** `feat(downloads): user-visible media issue reports`

---

# Phase 6 — Fiche média

## Tâche 19 : Résolution Jellyfin (`JellyfinItems`) — lien « Regarder »

**Objectif :** mapper `tmdbId` → item Jellyfin + URL web, cache TTL 5 min.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/ports/JellyfinItems.kt`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/infra/JellyfinItemsAdapter.kt`
- Modifier : `app/src/main/kotlin/org/hoohoot/homelab/manager/shared/jellyfin/Models.kt` (ajouter `ProviderIds` au modèle d'item si absent — vérifier)

**Étape 1 : port + adapter**

```kotlin
interface JellyfinItems {
    /** itemId Jellyfin par (mediaKind, tmdbId) */
    suspend fun resolveItemId(kind: MediaKind, tmdbId: Int): String?
}
```

Adapter : `jellyfinRestClient.getItems("Movie,Series", recursive = true, fields = "ProviderIds")` → map `tmdbId → itemId` (ProviderIds.Tmdb, string) avec cache TTL 5 min ; URL construite au niveau DTO : `"{jellyfin-public-url}/web/index.html#!/details?id={itemId}"` (bouton masqué si `downloads.jellyfin-public-url` vide ou item absent).

**Étape 2 : commit** `feat(downloads): resolve jellyfin items for watch links`

---

## Tâche 20 : Use case `GetMediaDetails` (agrégation fiche)

**Objectif :** un seul endpoint qui agrège *arr + TMDB + demandes + signalements + lien Jellyfin.

**Fichiers :**
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/ports/TmdbMetadata.kt`
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/infra/TmdbMetadataAdapter.kt` (cache TTL 10 min par id ; no-op si `tmdb.api_key` vide → retourne `null`)
- Créer : `app/src/main/kotlin/org/hoohoot/homelab/manager/downloads/domain/usecases/GetMediaDetails.kt`
- Modifier : `api/DownloadsResource.kt`, `api/DownloadsDtos.kt`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/it/DownloadsDetailsTest.kt`

**Étape 1 : DTO**

```kotlin
data class MediaDetailsDto(
    val mediaType: String,
    val tmdbId: Int,
    val title: String,
    val year: Int?,
    val overview: String?,
    val posterUrl: String?,
    val status: String,                 // statut portail (REQUESTED…PROBLEM) ou "NOT_IN_LIBRARY"
    val progressPercent: Int?,
    val voteAverage: Double?,           // note TMDB (null si clé absente)
    val cast: List<CastMemberDto>,      // 12 premiers par order
    val directors: List<String>,        // crew job == "Director" (film) / creators (série)
    val trailerUrl: String?,            // premier videos.results YouTube type == "Trailer"
    val similar: List<SimilarMediaDto>, // 10 premiers, avec inLibrary + status si présents
    val file: MediaFileDto?,            // null si pas de fichier
    val seasons: List<SeasonStatusDto>?,// séries uniquement
    val requests: List<MediaRequestDto>,// demandes ACTIVES (tous demandeurs visibles)
    val issues: List<MediaIssueDto>,    // OPEN puis RESOLVED récents
    val jellyfinUrl: String?,           // null si indisponible
)

data class CastMemberDto(val name: String, val character: String?, val profileUrl: String?)
data class SimilarMediaDto(val tmdbId: Int, val title: String, val year: Int?, val posterUrl: String?, val inLibrary: Boolean, val status: String?)
data class MediaFileDto(val quality: String?, val sizeBytes: Long?, val languages: List<String>, val subtitlesNote: String?)
data class SeasonStatusDto(val seasonNumber: Int, val monitored: Boolean, val episodeFileCount: Int, val episodeCount: Int, val status: String) // AVAILABLE | PARTIAL | MISSING
```

**Étape 2 : logique**

Film : `ArrCatalogCache.movies()` par tmdbId → statut (via `MediaStatusResolver`), `movieFile` → `MediaFileDto(quality.quality.name, size, languages.map { it.name }, null)`. TMDB via port (nullable). Demandes actives via `MediaRequestRepository.findActiveFor`. Issues via `MediaIssueRepository.listFor`. Jellyfin via port.

Série : stats saisons depuis `Series.seasons[].statistics` ; `SeasonStatusDto.status` : `AVAILABLE` si `episodeFileCount >= episodeCount > 0`, `PARTIAL` si `episodeFileCount > 0`, sinon `MISSING`. Fichier « détails » agrégé : langues/taille additionnées non fiables par saison en v1 → `MediaFileDto` au niveau série = qualité dominante non calculée : `null` en v1 (les détails fichier fiables sont par épisode — hors scope v1, noté dans les limites). Pour les séries, la section fichier affiche les stats par saison à la place.

`directors` série : TMDB `created_by` — ajouter `created_by` à `TmdbSeriesDetails` (`List<TmdbCrewMember>`) plutôt que crew.

**Étape 3 : endpoint**

```kotlin
@GET @Path("/movies/{tmdbId}")
suspend fun movieDetails(@PathParam("tmdbId") tmdbId: Int): MediaDetailsDto = getMediaDetails.movie(tmdbId)

@GET @Path("/series/{tmdbId}")
suspend fun seriesDetails(@PathParam("tmdbId") tmdbId: Int): MediaDetailsDto = getMediaDetails.series(tmdbId)
```

**Étape 4 : test IT** — stubs : `/api/v3/movie` (1 film avec movieFile), `/3/movie/872585` (TMDB, cast+videos+similar), `/Items` Jellyfin (ProviderIds.Tmdb correspondant), demande + issue seedées → vérifier l'agrégat complet, `trailerUrl` YouTube, `jellyfinUrl`, statut AVAILABLE. Cas TMDB en échec (500 WireMock) → réponse 200 avec `cast` vide (dégradation gracieuse).

Run : `./mvnw -pl app test -Dtest=DownloadsDetailsTest` — attendu : PASS.

**Étape 5 : commit** `feat(downloads): aggregated media details endpoint`

---

## Tâche 21 : Endpoint timeline par média (groupée pour les séries)

**Objectif :** historique complet de la fiche, lisible pour les séries.

**Fichiers :**
- Modifier : `api/DownloadsResource.kt`, `api/DownloadsDtos.kt`
- Test : `app/src/test/kotlin/org/hoohoot/homelab/manager/it/DownloadsTimelineTest.kt`

**Étape 1 : DTOs**

```kotlin
data class MediaEventDto(
    val id: Long,
    val eventType: String,            // REQUESTED | … | WATCHED
    val actor: String?,
    val title: String,
    val seasonNumber: Int?,
    val episodeNumber: Int?,          // null sur les lignes groupées
    val episodeCount: Int,            // 1 sauf ligne groupée
    val firstEpisode: Int?, val lastEpisode: Int?,  // lignes groupées
    val day: String?,                 // jour de regroupement (séries)
    val details: Map<String, String>,
    val occurredAt: LocalDateTime,
)

data class MediaEventPageDto(val items: List<MediaEventDto>, val page: Int, val pageSize: Int, val totalPages: Int, val totalCount: Long)
```

**Étape 2 : endpoints**

```kotlin
@GET @Path("/movies/{tmdbId}/events")   // page brute (pageFor)
@GET @Path("/series/{tmdbId}/events")   // groupedPageForSeries : épisodes regroupés par (event_type, season, jour)
```

Films : pas de regroupement nécessaire (un média = peu d'événements). Séries : `MediaEventRepository.groupedPageForSeries` (tâche 2) ; les événements sans `season_number` (REQUESTED, ISSUE_*, WATCHED série) restent des lignes unitaires — requête `UNION ALL` : lignes non-épisode + lignes groupées, tri global `occurred_at DESC`, `COUNT` total = somme des deux. (Implémentation : deux requêtes natives + fusion paginée en Kotlin, acceptable à cette échelle — le volume par série reste faible ; noter la limite dans un commentaire.)

**Étape 3 : test IT** — seed : 8 événements `DOWNLOADED` épisodes S01 même jour + 2 S02 autre jour + 1 REQUESTED → page 1 : 4 lignes (2 groupées + REQUESTED + …), `episodeCount=8`, `firstEpisode=1`, `lastEpisode=8`.

Run : `./mvnw -pl app test -Dtest=DownloadsTimelineTest` — attendu : PASS.

**Étape 4 : commit** `feat(downloads): per-media timeline endpoint with episode grouping`

---

# Phase 7 — Frontend

> Prérequis avant chaque tâche frontend qui consomme l'API : régénérer le client Orval.
> Depuis `app/src/main/webui` : `./mvnw -pl app compile -DskipTests` exporte le schéma OpenAPI (config `quarkus.smallrye-openapi.store-schema-directory`), puis `npm run dev` le régénère (ou `npx orval` directement).
> Vérifier que `src/api/service/homelab.ts` contient `useGetApiDownloadsMovies`, `usePostApiDownloadsRequests`, etc.

## Tâche 22 : Wrapper `lib/downloadsApi.ts`

**Objectif :** hooks TanStack + invalidations, pattern `cleanupApi.ts`.

**Fichiers :**
- Créer : `app/src/main/webui/src/lib/downloadsApi.ts`
- Test : `app/src/main/webui/src/lib/downloads.test.ts` (helpers purs)

**Étape 1 : wrapper**

```ts
import { useQueryClient, type QueryClient } from '@tanstack/vue-query'
import type { MaybeRef } from 'vue'
import { isAxiosError } from 'axios'
import type { GetApiDownloadsMoviesParams, GetApiDownloadsSeriesParams } from '../api/model'
import {
  getGetApiDownloadsMoviesQueryKey,
  getGetApiDownloadsSeriesQueryKey,
  useGetApiDownloadsMovies,
  useGetApiDownloadsSeries,
  useGetApiDownloadsSearch,
  useGetApiDownloadsMoviesTmdbId,
  useGetApiDownloadsSeriesTmdbId,
  useGetApiDownloadsMoviesTmdbIdEvents,
  useGetApiDownloadsSeriesTmdbIdEvents,
  usePostApiDownloadsRequests,
  usePostApiDownloadsKindTmdbIdCancel,
  usePostApiDownloadsKindTmdbIdIssues,
  usePostApiDownloadsIssuesIdResolve,
} from '../api/service/homelab'

export function invalidateDownloads(queryClient: QueryClient) {
  queryClient.invalidateQueries({ queryKey: getGetApiDownloadsMoviesQueryKey() })
  queryClient.invalidateQueries({ queryKey: getGetApiDownloadsSeriesQueryKey() })
}

export function downloadsErrorMessage(error: unknown): string {
  const backendMessage =
    isAxiosError(error) && (error.response?.data as { error?: string } | undefined)?.error
  return backendMessage || 'Une erreur est survenue, réessaie dans un instant.'
}

export function useMovies(params: MaybeRef<GetApiDownloadsMoviesParams>) {
  return useGetApiDownloadsMovies(params, {
    query: { placeholderData: (previous) => previous, refetchInterval: 30_000 },
  })
}
// useSeries symétrique ; useCatalogSearch(query, enabled) : enabled = query.trim().length >= 2
// useMovieDetails(tmdbId, enabled), useSeriesDetails(tmdbId, enabled)
// useMediaEvents(kind, tmdbId, params)
// Mutations : useRequestMedia / useCancelMedia / useReportIssue / useResolveIssue
//   → mutation: { onSettled: () => invalidateDownloads(queryClient) + invalidate details/events du média }
```

Les helpers purs à tester : libellés de statut (`statusLabel('DOWNLOADING') → 'En cours'`), couleurs de badge, `formatBytes` existe déjà (`lib/format.ts`), `progressLabel`, `seasonsLabel(['1','3']) → 'Saisons 1, 3'`.

**Étape 2 :** `npm run test -- downloads` (depuis `app/src/main/webui`) — PASS.

**Étape 3 : commit** `feat(downloads): frontend api wrapper for downloads module`

---

## Tâche 23 : Composants carte (`MediaCard`, `MediaStatusBadge`, `MediaProgressBar`)

**Objectif :** carte média réutilisable (grille listes + recherche + similaires).

**Fichiers :**
- Créer : `app/src/main/webui/src/components/downloads/MediaStatusBadge.vue`
- Créer : `app/src/main/webui/src/components/downloads/MediaProgressBar.vue`
- Créer : `app/src/main/webui/src/components/downloads/MediaCard.vue`
- Test : `app/src/main/webui/src/components/downloads/MediaCard.test.ts`

**Étape 1 : `MediaStatusBadge.vue`** — `BaseBadge` existant + mapping :

```vue
<script setup lang="ts">
import BaseBadge from '../ui/BaseBadge.vue'
const props = defineProps<{ status: string }>()
const labels: Record<string, string> = {
  REQUESTED: 'Demandé', DOWNLOADING: 'En cours', PARTIAL: 'Partiel',
  AVAILABLE: 'Disponible', PROBLEM: 'Problème',
}
const tones: Record<string, string> = {
  REQUESTED: 'info', DOWNLOADING: 'info', PARTIAL: 'warning',
  AVAILABLE: 'success', PROBLEM: 'danger',
}
// Adapter aux props réelles de BaseBadge (lire components/ui/BaseBadge.vue avant)
</script>
```

(Vérifier l'API exacte de `BaseBadge`/`BaseButton` avant d'écrire — ne pas inventer les props.)

**Étape 2 : `MediaProgressBar.vue`** — barre Tailwind : `div.h-1.5.rounded-full.bg-line` + `div.h-full.rounded-full.bg-berry` `:style="{ width: percent + '%' }"`.

**Étape 3 : `MediaCard.vue`** — affiche (`img.rounded-tile.aspect-[2/3].object-cover`, placeholder `bg-line` + `UiIcon` si `posterUrl` null), titre (2 lignes max), année, `MediaStatusBadge`, `MediaProgressBar` si `DOWNLOADING`, mention « Demandé par X » si `requestedBy` non vide. Clic → `router.push({ name: 'media-detail', params: { kind, tmdbId } })`.

**Étape 4 : test** — rendu via `renderWithQuery` : badge disponible, barre 42 %, placeholder sans poster.

**Étape 5 :** `npm run test -- MediaCard` — PASS. **Commit** `feat(downloads): media card components`

---

## Tâche 24 : Page liste `DownloadsPage.vue` (onglets + filtre + pagination + recherche)

**Objectif :** arrivée directe sur les listes ; recherche commune dès 2 caractères.

**Fichiers :**
- Créer : `app/src/main/webui/src/pages/DownloadsPage.vue`
- Test : `app/src/main/webui/src/pages/DownloadsPage.test.ts`

**Étape 1 : structure**

- Header (pattern `CleanupPage`) : titre « Téléchargements », sous-titre « Cherche un film ou une série, demande-le en un clic et suis son arrivée dans la bibliothèque. »
- `BaseInput` recherche avec debounce 300 ms (`ref` + `watch` + `setTimeout`, ou `computed` sur un `searchInput`/`searchQuery`).
- Si recherche active : deux sections « Films » / « Séries » de `MediaCard` ; pour les cartes `inLibrary=false` : bouton « Demander » (ouvre `RequestModal`, tâche 25) ; pour `inLibrary=true` : badge de statut, clic → fiche.
- Sinon : pills d'onglets « Films » / « Séries » (pattern onglets de `CleanupPage.vue`) + chips de filtre « Tous / En cours / Disponible / Problème » (`status` param), grille `grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4` de `MediaCard`, pagination « Préc./Suiv. » (pattern `ProtectionsPanel.vue` — vérifier son markup de pagination), `pageSize = 24`.
- États : `BaseSpinner` (pending), bloc erreur (pattern CleanupPage), état vide « Aucun média ici pour l'instant — lance une recherche pour demander ton premier téléchargement. »

**Étape 2 : test** — mocks du wrapper API (pattern des tests existants, ex. `components/app/ApplicationsByCategory.test.ts`) : onglet Séries bascule la query, recherche affiche les sections, bouton Demander ouvre la modale.

**Étape 3 :** `npm run test -- DownloadsPage` — PASS. **Commit** `feat(downloads): downloads page with tabs, filter, search`

---

## Tâche 25 : `RequestModal.vue` (confirmation + saisons + suivi)

**Objectif :** demande en un clic pour un film ; choix des saisons + « suivre la série » pour une série.

**Fichiers :**
- Créer : `app/src/main/webui/src/components/downloads/RequestModal.vue`

**Étape 1 :** `BaseModal` + titre « Demander {title} ». Film : texte « Le téléchargement démarre tout de suite avec le profil qualité par défaut. » + bouton confirmer. Série : liste des saisons avec `BaseToggle`/checkbox (pré-cochées toutes hors saison 0), `BaseToggle` « Suivre la série — les nouvelles saisons seront téléchargées automatiquement », bouton confirmer. Succès → ferme + invalide + toast/état optimiste simple ; erreur → `downloadsErrorMessage`.

**Étape 2 : commit** `feat(downloads): request modal with season selection`

---

## Tâche 26 : Page fiche `MediaDetailPage.vue`

**Objectif :** fiche complète style Jellyseerr + actions + timeline.

**Fichiers :**
- Créer : `app/src/main/webui/src/pages/MediaDetailPage.vue`
- Créer : `app/src/main/webui/src/components/downloads/FileDetails.vue`, `SeasonList.vue`, `MediaTimeline.vue`, `IssuesPanel.vue`, `ReportIssueModal.vue`

**Étape 1 : sections** (toutes alimentées par `useMovieDetails`/`useSeriesDetails` + `useMediaEvents`)
- En-tête : poster, titre, année, note TMDB (★ + valeur/10), `MediaStatusBadge` + `MediaProgressBar`, boutons d'action : « Regarder sur Jellyfin » (`jellyfinUrl`, `target="_blank"`), « Demander » (si `NOT_IN_LIBRARY` → `RequestModal`), « Annuler » (si `REQUESTED`/`DOWNLOADING`, confirmation `BaseModal`), « Signaler un problème » (`ReportIssueModal` : `BaseSelect` catégorie + `BaseTextarea` commentaire).
- Résumé (`overview`), réalisateur(s), casting (rangée horizontale scrollable : photo ronde + nom + rôle), bande-annonce (lien YouTube externe, icône `UiIcon`).
- `FileDetails` : qualité, taille (`formatBytes`), langues audio. Séries : `SeasonList` — une ligne par saison : « Saison 2 — 8/10 épisodes » + badge statut (`AVAILABLE` vert, `PARTIAL` orange, `MISSING` gris).
- `IssuesPanel` : signalements (badge catégorie, auteur, date, commentaire, statut ; bouton « Résoudre » si rapporteur ou admin).
- Rangée « Similaires » : `MediaCard` miniatures ; carte absente → bouton Demander.
- `MediaTimeline` : `BaseTimeline` + `BaseTimelineItem` existants (lire `components/app/HomelabTimeline.vue` pour le pattern de rendu). Libellés FR par `eventType` : `REQUESTED` → « Demandé par {actor} », `DOWNLOAD_STARTED` → « Téléchargement démarré », `DOWNLOADED` → « Importé ({quality}) », `UPGRADED` → « Montée en qualité ({quality}) », `SUBTITLES_DOWNLOADED` → « Sous-titres {language} téléchargés », `ISSUE_REPORTED` → « Problème signalé par {actor} : {category} », `ISSUE_RESOLVED` → « Signalement résolu par {actor} », `MEDIA_DELETED` → « Média supprimé », `WATCHED` → « Vu par {actor} » ; ligne groupée → « {episodeCount} épisodes de la saison {seasonNumber} importés ». Helper pur `eventLabel(event)` dans `lib/downloads.ts` + tests.

**Étape 2 : test page** — rendu sections, actions conditionnelles par statut, libellés timeline.

**Étape 3 :** `npm run test -- MediaDetailPage` — PASS. **Commit** `feat(downloads): media detail page with timeline and issues`

---

## Tâche 27 : Router + sidenav

**Objectif :** navigation vers le module.

**Fichiers :**
- Modifier : `app/src/main/webui/src/router/index.ts`
- Modifier : `app/src/main/webui/src/components/app/AppShell.vue`
- Vérifier : `app/src/main/webui/src/components/ui/UiIcon.vue` expose une icône `download` (lire le fichier ; sinon ajouter le path Lucide correspondant).

**Étape 1 : routes**

```ts
{ path: '/telechargements', name: 'downloads', component: () => import('../pages/DownloadsPage.vue') },
{ path: '/telechargements/:kind(movie|series)/:tmdbId(\\d+)', name: 'media-detail', component: () => import('../pages/MediaDetailPage.vue') },
```

**Étape 2 : sidenav** — dans `AppShell.vue`, après « Tes applis » :

```vue
<SideNavItem to="/telechargements" label="Téléchargements" icon="download" />
```

**Étape 3 :** `npm run test:ci` (lint + tests) — PASS. **Commit** `feat(downloads): wire downloads module into navigation`

---

# Phase 8 — DevEx, docs et finitions

## Tâche 28 : Stubs WireMock dev

**Objectif :** module utilisable en `quarkus:dev` sans vrais *arr.

**Fichiers :** `app/src/main/resources/wiremock/mappings/` :
- `radarr-lookup.json` (`GET /api/v3/movie/lookup` — matcher sur query `term`, retourne 3 films dont 1 avec `id` présent = déjà en bibliothèque)
- `radarr-movie-add.json` (`POST /api/v3/movie` → 201 écho du film avec `id: 999`)
- `radarr-rootfolder.json` (`GET /api/v3/rootfolder` → `[{"id":1,"path":"/data/movies"}]`)
- `sonarr-lookup.json`, `sonarr-series-add.json`, `sonarr-rootfolder.json`
- `sonarr-queue.json` (queue avec un item à 55 %)
- `radarr-queue-delete.json` + `sonarr-queue-delete.json` (`DELETE /api/v3/queue/{id}` → 200)
- `tmdb-movie.json` / `tmdb-tv.json` (`GET /3/movie/*` et `/3/tv/*` → payload minimal cast+videos+similar)
- Modifier `application.properties` : ajouter `%dev.quarkus.rest-client.tmdb-api.url=http://localhost:${quarkus.wiremock.devservices.port}` et `%test.quarkus.rest-client.tmdb-api.url=...`
- Stubs historique : étendre `radarr-history-since.json` / `sonarr-history-since.json` avec des records `grabbed` et `movieFileDeleted` (avec `movie.tmdbId` / `series.tmdbId`) pour voir la timeline se remplir en dev.
- Vérifier `bazarr-*.json` : présence de `radarrId`/`sonarrSeriesId` ; ajouter si manquant.

Vérification : `./mvnw -pl app quarkus:dev`, ouvrir l'UI → onglet Téléchargements, recherche, demande, fiche. **Commit** `feat(downloads): wiremock dev stubs for downloads module`

---

## Tâche 29 : Config finale + README + Helm

**Fichiers :**
- Modifier : `app/src/main/resources/application.properties` (regrouper les clés ajoutées, commentaires FR)
- Modifier : `README.md` (section fonctionnalité + table des variables : `DOWNLOADS_RADARR_QUALITY_PROFILE_ID`, `DOWNLOADS_RADARR_ROOT_FOLDER`, `DOWNLOADS_SONARR_QUALITY_PROFILE_ID`, `DOWNLOADS_SONARR_ROOT_FOLDER`, `TMDB_API_KEY`, `JELLYFIN_PUBLIC_URL`, `media-watch-events.every`)
- Modifier : `charts/homelab-manager/` (values + template d'env de l'app, en suivant le pattern des variables existantes — lire le chart avant)

**Commit** `feat(downloads): document and expose downloads module configuration`

---

## Tâche 30 : Validation globale

1. `./mvnw verify` (JDK 21 — build complet, backend + Quinoa lint/tests/build frontend) — BUILD SUCCESS.
2. Vérifier la couverture JaCoCo du nouveau module au passage (rapport CI).
3. Revue croisée : endpoints dans le schéma OpenAPI exporté (`app/src/main/webui/api/openapi.yaml` régénéré, diff visible — contrat Orval).
4. `git log --oneline` : commits Conventional Commits en anglais.

---

# Risques, compromis et limites connues

- **R1 — Charge *arr :** listes et recherche frappent Radarr/Sonarr. Mitigation : cache 60 s (`ArrCatalogCache`) ; queue en une seule page de 500. Si la bibliothèque dépasse ~5 000 items, le tri/pagination en mémoire reste OK mais il faudra mesurer.
- **R2 — Lookup Sonarr par tmdbId :** le lookup Sonarr est TVDB-first ; les résultats sans `tmdbId` sont ignorés en v1 (log). Si des séries demandées n'apparaissent pas, fallback : recherche par titre + correspondance fuzzy (v2).
- **R3 — Upgrade = heuristique :** 2ᵉ import d'un même média ⇒ `UPGRADED`. Un re-téléchargement manuel de même qualité sera aussi étiqueté « Montée en qualité » (la `details.quality` lève l'ambiguïté).
- **R4 — Suppression détectée par polling :** un média supprimé entre deux syncs laisse un événement `MEDIA_DELETED` horodaté à la date d'historique *arr — correct ; mais si l'historique *arr est purgé avant la sync, l'événement est perdu (backfill 30 j identique au dashboard, Q3).
- **R5 — Séries : détails fichier par épisode** non affichés en v1 (stats par saison à la place) ; les événements sous-titres d'épisodes non résolubles vers un `tmdbId` sont ignorés.
- **R6 — Annulation = blocklist :** `DELETE /queue/{id}?blocklist=true` empêche le re-grab automatique de la même release — comportement voulu pour « annuler », à garder en tête si des utilisateurs s'en étonnent (un admin peut débloquer dans Radarr).
- **R7 — TMDB optionnel :** sans `TMDB_API_KEY`, la fiche reste utile (statut, timeline, signalements, fichier) mais perd casting/trailer/similaires — documenté dans le README.
- **R8 — Pas de nouvelle notification Matrix** (spec) ; les webhooks *arr existants restent inchangés — la timeline ne dépend pas de leur configuration.
