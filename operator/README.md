# homelab-manager-operator

Opérateur Kubernetes qui synchronise les `HTTPRoute` (Gateway API) annotées avec le registre
d'applications du portail homelab-manager, via son API REST.

- Une `HTTPRoute` annotée `enabled=true` → une application créée/mise à jour dans le portail.
- Route supprimée ou annotation retirée → l'application est supprimée (uniquement celles marquées
  `managedBy=operator` ; les applications créées à la main ne sont jamais touchées).
- `requiresVpn` est déduit des `parentRefs` : si la route est attachée à une gateway dont le nom
  figure dans `VPN_GATEWAYS` (défaut : `internal`), l'application est marquée VPN.

Le reconciler réagit aux événements en temps réel ; un sweep complet périodique (`SYNC_INTERVAL`,
défaut 5m) rattrape les routes supprimées — pas de finalizer posé sur les `HTTPRoute`, qui
appartiennent à Flux/Helm.

## Contrat d'annotations

Préfixe configurable (`ANNOTATION_PREFIX`, défaut `homelab-manager.hoohoot.org`) :

| Annotation | Requis | Fallback |
|---|---|---|
| `homelab-manager.hoohoot.org/enabled: "true"` | oui | absente → route ignorée |
| `homelab-manager.hoohoot.org/name` | non | `metadata.name` de la route |
| `homelab-manager.hoohoot.org/category` | non | `DEFAULT_CATEGORY` (défaut `Uncategorized`) |
| `homelab-manager.hoohoot.org/description` | non | `Managed by homelab-manager-operator` |
| `homelab-manager.hoohoot.org/url` | non | `https://<premier hostname du spec>` ; sans hostname ni annotation, la route est ignorée |
| `homelab-manager.hoohoot.org/logo-url` | non | absente → pas de logo ; le portail télécharge l'image (png/jpeg/svg/webp, 1 Mo max) |

Exemple :

```yaml
apiVersion: gateway.networking.k8s.io/v1
kind: HTTPRoute
metadata:
  name: jellyfin
  namespace: media
  annotations:
    homelab-manager.hoohoot.org/enabled: "true"
    homelab-manager.hoohoot.org/name: "Jellyfin"
    homelab-manager.hoohoot.org/category: "Médias"
    homelab-manager.hoohoot.org/description: "Serveur de streaming du homelab"
    homelab-manager.hoohoot.org/logo-url: "https://cdn.jsdelivr.net/gh/homarr-labs/dashboard-icons/svg/jellyfin.svg"
spec:
  parentRefs:
    - name: internal   # gateway VPN → requiresVpn=true
  hostnames:
    - jellyfin.example.org
```

Logos : l'annotation `logo-url` déclare la source, le portail télécharge l'image et la stocke.
Le logo est re-téléchargé quand l'URL change et supprimé quand l'annotation disparaît. Un logo
uploadé à la main dans l'admin survit aux reconciles tant que la route ne déclare pas de
`logo-url` (dès qu'une annotation est posée, le déclaratif reprend la main).

## Configuration

| Variable | Défaut | Description |
|---|---|---|
| `MANAGER_API_URL` | `http://localhost:8080` | URL de l'API homelab-manager |
| `OPERATOR_API_KEY` | `dev-operator-key` | Clé API partagée avec le portail (header `X-Api-Key`) |
| `VPN_GATEWAYS` | `internal` | Noms de gateways (séparés par des virgules) impliquant `requiresVpn=true` |
| `ANNOTATION_PREFIX` | `homelab-manager.hoohoot.org` | Préfixe des annotations |
| `DEFAULT_CATEGORY` | `Uncategorized` | Catégorie par défaut |
| `SYNC_INTERVAL` | `5m` | Intervalle du sweep complet |
| `WATCH_NAMESPACES` | `JOSDK_ALL_NAMESPACES` | Namespaces watchés par le reconciler |

## Authentification

L'opérateur appelle les endpoints dédiés `/api/operator/applications` du portail, protégés par
une clé partagée passée dans le header `X-Api-Key`. La même valeur doit être fournie des deux
côtés via `OPERATOR_API_KEY` (côté portail, clé absente ⇒ tous les appels opérateur sont
rejetés en 401). Aucun setup Keycloak n'est nécessaire pour l'opérateur.

## RBAC

Le pod de l'opérateur a besoin de lister/watcher les `HTTPRoute` de tout le cluster
(déploiement dans le repo GitOps `lucas-dclrcq/homelabitty`, HelmRelease attendue à
`kubernetes/apps/default/homelab-manager/operator/helmrelease.yaml` pour la mise à jour
automatique de l'image par la CI) :

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: homelab-manager-operator
rules:
  - apiGroups: ["gateway.networking.k8s.io"]
    resources: ["httproutes"]
    verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: homelab-manager-operator
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: homelab-manager-operator
subjects:
  - kind: ServiceAccount
    name: homelab-manager-operator
    namespace: default
```

## Dev

```shell
mvn -pl operator quarkus:dev
```

Le mode dev utilise le kubeconfig local (list/watch cluster-wide requis) et pointe par défaut
sur `http://localhost:8080` (portail en dev), avec la clé API de dev `dev-operator-key` des
deux côtés.
