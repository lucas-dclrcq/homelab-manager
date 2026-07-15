# Changelog

## [0.3.0](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.2.0...v0.3.0) (2026-07-15)


### Features

* send notification on problem workflow events ([5ea851e](https://github.com/lucas-dclrcq/homelab-manager/commit/5ea851e64c42796372574f739f3ab654c6ae6cd6))
* split cleanup page into Campagnes/Propositions/Protection tabs ([#123](https://github.com/lucas-dclrcq/homelab-manager/issues/123)) ([55fb34c](https://github.com/lucas-dclrcq/homelab-manager/commit/55fb34c2fdc4103e91872eeb519be1ebc73f10cd))


### Documentation

* add AGENTS.md context file for AI coding agents ([#121](https://github.com/lucas-dclrcq/homelab-manager/issues/121)) ([6acc5f2](https://github.com/lucas-dclrcq/homelab-manager/commit/6acc5f23b07c1bb295144494b28089854c75d3fe))


### Refactoring

* fix compilation warnings ([dd869c8](https://github.com/lucas-dclrcq/homelab-manager/commit/dd869c8393e94e8f905125e4979f36cb4bc75234))
* improve vertx context use by matrix bot ([4190dd1](https://github.com/lucas-dclrcq/homelab-manager/commit/4190dd1be848811db67dbd6b3b780ec904936494))


### CI/CD

* add code coverage ([#118](https://github.com/lucas-dclrcq/homelab-manager/issues/118)) ([b1ade2f](https://github.com/lucas-dclrcq/homelab-manager/commit/b1ade2f66a2e1c54df457f098098fedb08e84986))


### Chores

* **main:** release 0.2.1-SNAPSHOT ([d055910](https://github.com/lucas-dclrcq/homelab-manager/commit/d0559102eca4da41bdd1f8e30430d259ab899c7e))

## [0.2.0](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.1.5...v0.2.0) (2026-07-14)


### Features

* media deletion suggestions by users ([1c0a072](https://github.com/lucas-dclrcq/homelab-manager/commit/1c0a072c3058ad741fa731d68ee9b1a44a0e5306))


### Bug Fixes

* adapt TimeService for kotlinx-datetime 0.8.0 / Kotlin 2.4.0 compatibility ([e3135ff](https://github.com/lucas-dclrcq/homelab-manager/commit/e3135ff43569a6ca905ce40b526d6e0ba716e8a0))
* **deps:** update dependency io.ktor:ktor-client-java-jvm to v3.5.1 ([2554f3b](https://github.com/lucas-dclrcq/homelab-manager/commit/2554f3b2984c3321ffde4b831f10bc42fe33de94))
* **deps:** update dependency io.quarkiverse.quinoa:quarkus-quinoa to v2.8.3 ([5dae712](https://github.com/lucas-dclrcq/homelab-manager/commit/5dae712f12bffb870fd8b1222b3583e472c95084))
* **deps:** update dependency io.quarkiverse.wiremock:quarkus-wiremock to v1.6.3 ([a9be90f](https://github.com/lucas-dclrcq/homelab-manager/commit/a9be90f8a7d930427ed6429567bede39e62ed673))
* **deps:** update dependency org.commonmark:commonmark to v0.29.0 ([447d60e](https://github.com/lucas-dclrcq/homelab-manager/commit/447d60efa9cc560393e738ce34b3e8ae8ce8b618))
* **deps:** update dependency org.jetbrains.kotlinx:kotlinx-datetime-jvm to v0.8.0-0.6.x-compat ([20a9e4c](https://github.com/lucas-dclrcq/homelab-manager/commit/20a9e4c65362fb5685ffd1cbd5b6699857001e1b))
* **deps:** update quarkus.platform.version to v3.37.2 ([41d8a88](https://github.com/lucas-dclrcq/homelab-manager/commit/41d8a8884fa529874fbd5ba4d0b7548448f14a7b))
* **deps:** update trixnity.version to v5.6.0 ([504736f](https://github.com/lucas-dclrcq/homelab-manager/commit/504736f99d584592a2664671bc740c90f5bbc334))
* resolve merge conflict in package.json ([f5517d9](https://github.com/lucas-dclrcq/homelab-manager/commit/f5517d944bcadf9ad484c52cfb36a9e8de3dce77))


### Tests

* add frontend test strategy ([8fc73cc](https://github.com/lucas-dclrcq/homelab-manager/commit/8fc73cc01bd366ca00481fce5505aa3bed1453c4))


### Chores

* **deps:** update actions/checkout action to v7 ([af64d72](https://github.com/lucas-dclrcq/homelab-manager/commit/af64d7241999d78e50190a090487266809b51349))
* **deps:** update actions/setup-java action to v5 ([6a12929](https://github.com/lucas-dclrcq/homelab-manager/commit/6a129292436eccfe0e95dcfc987f81f447527fb7))
* **deps:** update dependency @vitejs/plugin-vue to v5.2.4 ([153b7e4](https://github.com/lucas-dclrcq/homelab-manager/commit/153b7e4007fcb8802b2c7bd4a95d793c88857eeb))
* **deps:** update dependency @vue/tsconfig to ^0.9.0 ([6a1eca1](https://github.com/lucas-dclrcq/homelab-manager/commit/6a1eca11dc98012a2d87da6dbbdcf3dceb34771d))
* **deps:** update dependency io.smallrye:jandex-maven-plugin to v3.6.0 ([44bba1a](https://github.com/lucas-dclrcq/homelab-manager/commit/44bba1a7c13051fef51439847c242632c75776c9))
* **deps:** update dependency maven-wrapper to v3.3.4 ([6959552](https://github.com/lucas-dclrcq/homelab-manager/commit/6959552e74ec112962a87562a104e773454ff6cc))
* **deps:** update dependency org.apache.maven.plugins:maven-compiler-plugin to v3.15.0 ([cddda0f](https://github.com/lucas-dclrcq/homelab-manager/commit/cddda0f67ee479b54323031f16ba11a82d7bc0b2))
* **deps:** update dependency org.apache.maven.plugins:maven-surefire-report-plugin to v3.5.6 ([36ee2ca](https://github.com/lucas-dclrcq/homelab-manager/commit/36ee2ca6fb85728ffeac76dd0d66cfaeccd32a3f))
* **deps:** update dependency org.assertj:assertj-core to v3.27.7 ([f37916e](https://github.com/lucas-dclrcq/homelab-manager/commit/f37916ee45f861b6001e21a63a2a14c810a52812))
* **deps:** update dependency org.wiremock:wiremock to v3.13.2 ([4052cd8](https://github.com/lucas-dclrcq/homelab-manager/commit/4052cd87ed71942f969d56f8095a4b02241b9dc6))
* **deps:** update dependency orval to v7.21.0 ([55283ef](https://github.com/lucas-dclrcq/homelab-manager/commit/55283effd95330ab6208bca03309e06afb2433cb))
* **deps:** update dependency vite to v6.4.3 ([e9a81c4](https://github.com/lucas-dclrcq/homelab-manager/commit/e9a81c492adbead5dcfc782280c7f17381bf5fd2))
* **deps:** update dependency vue to v3.5.39 ([0465236](https://github.com/lucas-dclrcq/homelab-manager/commit/04652360287b2e8825e31e1ff4073efa6df7f70d))
* **deps:** update dependency vue-tsc to v2.2.12 ([c979040](https://github.com/lucas-dclrcq/homelab-manager/commit/c979040d994c80b5a23c8408a49c6c3ddb261a39))
* **deps:** update docker/build-push-action action to v7 ([2ff5874](https://github.com/lucas-dclrcq/homelab-manager/commit/2ff58740bf20c84eb686e9c9aa4069147440d60e))
* **deps:** update docker/login-action action to v4 ([7c32f31](https://github.com/lucas-dclrcq/homelab-manager/commit/7c32f311a3543a0f487387e69c4e594bf0fc2a30))
* **deps:** update docker/setup-buildx-action action to v4 ([8363328](https://github.com/lucas-dclrcq/homelab-manager/commit/8363328c9561e2dd303892f15b7c958f95bd774f))
* **deps:** update docker/setup-qemu-action action to v4 ([bfad519](https://github.com/lucas-dclrcq/homelab-manager/commit/bfad5195a38f966eacc289cfecdbdb5389f1da04))
* **deps:** update googleapis/release-please-action action to v5 ([350ce4d](https://github.com/lucas-dclrcq/homelab-manager/commit/350ce4d7189d0a22fdd4f1df3b7a9dc74b75cdb6))
* **deps:** update kotlin monorepo to v2.4.0 ([08152de](https://github.com/lucas-dclrcq/homelab-manager/commit/08152deab5b7647b09e7010d6198c3567c45eda5))
* **deps:** update surefire-plugin.version to v3.5.6 ([cf082ab](https://github.com/lucas-dclrcq/homelab-manager/commit/cf082abf21ac64ce3a4f2ce585ebfb132107543c))
* **main:** release 0.1.6-SNAPSHOT ([fdf6f0d](https://github.com/lucas-dclrcq/homelab-manager/commit/fdf6f0d62e03d2e6317af91d764634e8ea5336cd))

## [0.1.5](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.1.4...v0.1.5) (2026-07-11)


### Features

* add cleanup module ([19c7946](https://github.com/lucas-dclrcq/homelab-manager/commit/19c7946fe998d951c4b23d8fecc17c98bdd32089))

## [0.1.4](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.1.3...v0.1.4) (2026-07-10)


### Features

* **problems:** other problem reports, tv support, admin module, release recommendations ([42dc89a](https://github.com/lucas-dclrcq/homelab-manager/commit/42dc89a835a96da3d91eddece05c90c0e8fa7d85))
* **problems:** Problèmes UI — tv/series, other-problem reports, admin module, recommended releases ([6a5868b](https://github.com/lucas-dclrcq/homelab-manager/commit/6a5868b822eaa12b36efb3f03c4698886c93cb5a))


### Bug Fixes

* **problems:** base release recommendation on desired quality, not current file ([f720513](https://github.com/lucas-dclrcq/homelab-manager/commit/f72051307732974ee519572c0e99c7954f73432b))
* **problems:** pin isFrench/isRecommended JSON keys so the default filter works ([38e0861](https://github.com/lucas-dclrcq/homelab-manager/commit/38e0861f245da79d5a777e467b732762e64e4601))
* **problems:** recommend 1080p+ VF/MULTI torrents regardless of exact profile target ([e6b6438](https://github.com/lucas-dclrcq/homelab-manager/commit/e6b6438952cfb48596fe2e95dfd2f8461e07c857))
* **problems:** recommend only the exact desired resolution, not higher ([7772470](https://github.com/lucas-dclrcq/homelab-manager/commit/777247089f82915ed495a678064163e99112e1a5))
* try to avoid infinite loop ([f266f36](https://github.com/lucas-dclrcq/homelab-manager/commit/f266f369fce2223fdce0fb2fde262b9fdcf40204))

## [0.1.3](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.1.2...v0.1.3) (2026-07-10)


### Features

* **stats:** add currently playing + general stats on vieweing ([26902ba](https://github.com/lucas-dclrcq/homelab-manager/commit/26902ba48e96dd6845559d53732d2728cf854c0e))

## [0.1.2](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.1.1...v0.1.2) (2026-07-09)


### Features

* add finances tab ([55f5766](https://github.com/lucas-dclrcq/homelab-manager/commit/55f576695808f80177c5086157cd0eeb37224115))


### Documentation

* **README:** rewrite ([a62f03a](https://github.com/lucas-dclrcq/homelab-manager/commit/a62f03a845b16332314640b17056dc11547c669c))

## [0.1.1](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.1.0...v0.1.1) (2026-07-09)


### Features

* add fault tolerance to all external REST clients ([299cea8](https://github.com/lucas-dclrcq/homelab-manager/commit/299cea854e96c44d986112f313a8c7ee9b70cfe8))
* **applications:** type the create/update responses in the OpenAPI contract ([8359d45](https://github.com/lucas-dclrcq/homelab-manager/commit/8359d455ea53c14ceb5d4ae4104fcd47d25ad853))

## 0.1.0 (2026-07-08)


### Features

* add additional info to issue created notification ([4d3eb63](https://github.com/lucas-dclrcq/homelab-manager/commit/4d3eb633eb38d7ba8e4cf0e2d6a96dbaf5ac3274))
* add emojis on notifications ([585fd70](https://github.com/lucas-dclrcq/homelab-manager/commit/585fd70e19e332a4ad736a36d0506af8da42d6a6))
* add endpoint for Radarr webhooks ([16c65a7](https://github.com/lucas-dclrcq/homelab-manager/commit/16c65a77df6a145cb37564be8b327e9a32ab4237))
* add endpoint to send weekly report ([6b2b08c](https://github.com/lucas-dclrcq/homelab-manager/commit/6b2b08cb6238f4584ac67dc48eaa61b3283d9c03))
* add logging on all commands ([52e120a](https://github.com/lucas-dclrcq/homelab-manager/commit/52e120ab4144a3172acc2b5cd6f86cf89561ecd9))
* add more emojis ([2ce134a](https://github.com/lucas-dclrcq/homelab-manager/commit/2ce134a15b69324eb8cb2a268f11e5870dfeff37))
* add optional api key auth on notification endpoints ([d5bad74](https://github.com/lucas-dclrcq/homelab-manager/commit/d5bad74dd0bbe128113bf777603878e59d2f9d30))
* add release year to album notifications ([a43e23f](https://github.com/lucas-dclrcq/homelab-manager/commit/a43e23f2ca817bfb3227077abc109493127ff85a))
* add synapse devservices to enable testing locally ([5a4def0](https://github.com/lucas-dclrcq/homelab-manager/commit/5a4def075f5a99b9a55150df038234da566119b4))
* add who watched bot command ([682cf73](https://github.com/lucas-dclrcq/homelab-manager/commit/682cf73ce048d43b24ea2bb95d64a242b3a1b371))
* **admin:** can completely manage applications from admin ([1459331](https://github.com/lucas-dclrcq/homelab-manager/commit/14593319bbdec637b89dd87116e75b277a74cbac))
* **bot:** add bot infrastructure ([ee63187](https://github.com/lucas-dclrcq/homelab-manager/commit/ee631872d0eddc6c3933185b221630220dfa246c))
* c'est comment ? ([c2e8b6e](https://github.com/lucas-dclrcq/homelab-manager/commit/c2e8b6e9d52a7f704280a9845c96c0ed6b7959fc))
* configure dlqs ([e8f56a4](https://github.com/lucas-dclrcq/homelab-manager/commit/e8f56a45e5265fab942bb4aa7f5563bbedb7c6ef))
* **corrector:** allow forcing release ([68f85b2](https://github.com/lucas-dclrcq/homelab-manager/commit/68f85b279d98f1c7ce3b77e94126ba43647d1371))
* **corrector:** fix vo instead of fr movies ([5409e8a](https://github.com/lucas-dclrcq/homelab-manager/commit/5409e8ab66551d49835c600916dd62f50f9d5460))
* **deado-command:** add deado regex to trigger deados ([0280d22](https://github.com/lucas-dclrcq/homelab-manager/commit/0280d22906f932f48edb9cd05d5b391bfabcb8bc))
* **deado-data:** Expand and diversify deados list ([668dd4b](https://github.com/lucas-dclrcq/homelab-manager/commit/668dd4befa8653f31ada7b907de734ec4db120e5))
* disable thread series notifications for now ([ed4f433](https://github.com/lucas-dclrcq/homelab-manager/commit/ed4f433b9d90226a578314d3d3ddcd987d9ac1e1))
* enable opentelemetry ([5e78456](https://github.com/lucas-dclrcq/homelab-manager/commit/5e78456990a373e3d38223ead34e43df13188154))
* enable otel at build time ([9093c8b](https://github.com/lucas-dclrcq/homelab-manager/commit/9093c8b3d42fcc1fc9b0ee4cf475b8837468b30e))
* **frontend:** init quinoa ([9ac3d72](https://github.com/lucas-dclrcq/homelab-manager/commit/9ac3d728f7d64d34927ceab6c3ea03b46f481661))
* **frontend:** use orval to generate http client from openapi spec ([0017d13](https://github.com/lucas-dclrcq/homelab-manager/commit/0017d13f7a41fccf9f5003ce608544f1b5bb99a1))
* **gif:** add matrix command ([0320828](https://github.com/lucas-dclrcq/homelab-manager/commit/03208289aab128f9949612dee650214cd8223649))
* **homelab-manager:** send notification to dedicated room ([bca1ac7](https://github.com/lucas-dclrcq/homelab-manager/commit/bca1ac7f829fffd236f3c94440a6219c3c072bd8))
* **jellyseerr:** notify all events ([443d1ec](https://github.com/lucas-dclrcq/homelab-manager/commit/443d1ec691cf17702ff1110b39a28541bde1aa86))
* **lidarr:** send matrix notification on lidarr events ([ae45f92](https://github.com/lucas-dclrcq/homelab-manager/commit/ae45f92645158d4b9d75580996464fa8b9cc2ff6))
* log errors for weekly report ([23392d8](https://github.com/lucas-dclrcq/homelab-manager/commit/23392d80437e7213df6c74f57aeb7485b0e43b5b))
* log radarr webhooks ([a379470](https://github.com/lucas-dclrcq/homelab-manager/commit/a379470e1b5f2d470c80bf593459869664be9375))
* manage multiple matrix bot using leader election ([229a447](https://github.com/lucas-dclrcq/homelab-manager/commit/229a447b5e1df2c941dcf9111b2d4342d3098230))
* **monthly-stats-report:** add most popular series of the month ([e28be67](https://github.com/lucas-dclrcq/homelab-manager/commit/e28be67a240791bc4880f6d7e04be7c11e675a2f))
* **notifications:** add generic notification route ([888132d](https://github.com/lucas-dclrcq/homelab-manager/commit/888132de881b2946bcfef595eed27235f9d1d8b2))
* **notifications:** send resolved notification in thread ([be1e03d](https://github.com/lucas-dclrcq/homelab-manager/commit/be1e03dc70a60b8ba350441d0da2f4099dfa974e))
* **notifications:** send weekly whats next report ([93ab5c7](https://github.com/lucas-dclrcq/homelab-manager/commit/93ab5c7936d270578a5c6cd00610d343b0e9f368))
* notify subtitles downloads ([7bedb7a](https://github.com/lucas-dclrcq/homelab-manager/commit/7bedb7a7a0a2cd230e598591296e782086fc80fc))
* prevent null exception in radarr notification consumer ([ea2775c](https://github.com/lucas-dclrcq/homelab-manager/commit/ea2775cc9cfa84cfaccab2f90bfb89fb8c20b73e))
* remove deprecated arr notification endpoint ([bb33a38](https://github.com/lucas-dclrcq/homelab-manager/commit/bb33a38e5aa62f43a56a8bbecc6514bf6594425c))
* remove leader stuff ([71d642e](https://github.com/lucas-dclrcq/homelab-manager/commit/71d642e3fd258466abc049c47943551587d5257e))
* replace http calls with trixnity ([8976c0c](https://github.com/lucas-dclrcq/homelab-manager/commit/8976c0ce9d46299bb4a8fae6e0f27314b40320a1))
* rework weekly report ([bafedb4](https://github.com/lucas-dclrcq/homelab-manager/commit/bafedb469501e95a03c2b33f1bc957d562b59a3a))
* send jellyseerr issue notifications to matrix ([c7d4215](https://github.com/lucas-dclrcq/homelab-manager/commit/c7d4215f1b0ce326269130dcaccc23bfe8474e29))
* **series:** fix parsing of series episode quality ([c2719a3](https://github.com/lucas-dclrcq/homelab-manager/commit/c2719a3b76ee38fa8917fece4c36576702fea093))
* show album downloads in timeline ([9da28d3](https://github.com/lucas-dclrcq/homelab-manager/commit/9da28d39a1a23f904f4167b28593708235746182))
* skong ([edb8028](https://github.com/lucas-dclrcq/homelab-manager/commit/edb802899337f30788229a169f58fc34edeb6110))
* **sonarr:** add imdb link ([ae95db1](https://github.com/lucas-dclrcq/homelab-manager/commit/ae95db16445f232533a17c5013c74408b47aa075))
* **sonarr:** add ParseSeries class for handling series parsing and refactor notifications ([1527ebf](https://github.com/lucas-dclrcq/homelab-manager/commit/1527ebf3bc65860ab4bbba2cf80cd18c44469266))
* **sonarr:** add season and episode number on notification ([033e8d7](https://github.com/lucas-dclrcq/homelab-manager/commit/033e8d73b5f75be4ec424682fab76b1427225390))
* **sonarr:** send matrix notification on sonarr events ([e8b412a](https://github.com/lucas-dclrcq/homelab-manager/commit/e8b412a24fcc4b59e2c2d98b2b475698f856a951))
* start implementing interface ([842b0bb](https://github.com/lucas-dclrcq/homelab-manager/commit/842b0bb1666865536aed1d68b1ace733fc9302f5))
* subsequent series notifications should be in thread ([4e6e8b1](https://github.com/lucas-dclrcq/homelab-manager/commit/4e6e8b105c9a244e605f934d5c73e6b3296fcf60))
* **support:** notify issue comments ([ea5417a](https://github.com/lucas-dclrcq/homelab-manager/commit/ea5417a6d7441828a3e7b9dc6a92cda5a68b8317))
* **top-watched:** add bot command ([3a24555](https://github.com/lucas-dclrcq/homelab-manager/commit/3a245559377150521512f7ce830e294f644845c2))
* **top-watched:** add most viewed series & movies ([9b8a6c9](https://github.com/lucas-dclrcq/homelab-manager/commit/9b8a6c95d76132e601c92c5accd23f9afa9cf201))
* **top-watchers:** add top watchers matrix command ([da8b311](https://github.com/lucas-dclrcq/homelab-manager/commit/da8b311df674cf207b513db4be069cbe511009d0))
* upgrade synapse to 1.150 + fix rate limit issue ([3135a28](https://github.com/lucas-dclrcq/homelab-manager/commit/3135a28374fddac55de46966e0b98802c50aad91))
* upgrade to issue notifications to seerrr ([ebad8a8](https://github.com/lucas-dclrcq/homelab-manager/commit/ebad8a834cd6b08cccaaa7305a737adce67fe49f))
* upgrade trixnity to 5.3.1 ([2f93518](https://github.com/lucas-dclrcq/homelab-manager/commit/2f935188835f8fbbbd94332887a63101b6775817))
* use kafka to orchestrate notifications ([282c55b](https://github.com/lucas-dclrcq/homelab-manager/commit/282c55b6ca0b77e9881a4f3a116fb5163eff0248))
* use pg db for trixnity ([dbc5bc0](https://github.com/lucas-dclrcq/homelab-manager/commit/dbc5bc0b83c51373158d8a4ab761f7b4f432cd6d))
* use pg instead of kafka for simplicity ([d9ab164](https://github.com/lucas-dclrcq/homelab-manager/commit/d9ab164ec7f3c49f81fe90b4adc1383625d1c26e))
* use scheduled jobs to sync stats ([aee166a](https://github.com/lucas-dclrcq/homelab-manager/commit/aee166a16faafe48cb40045b5ba04d43ed1f9123))
* **weeklyreport:** add album releases ([409bbdf](https://github.com/lucas-dclrcq/homelab-manager/commit/409bbdf58dac7317bfc81c0def007f91b6dd0857))
* **weeklyreport:** add emojis to differentiate between tv and movies ([28a16ee](https://github.com/lucas-dclrcq/homelab-manager/commit/28a16eea4384ef01dcef1a7ac7951898775b374b))
* **weeklyreport:** report on sunday ([e7230c7](https://github.com/lucas-dclrcq/homelab-manager/commit/e7230c732184ff381dfaf2d5e6a7e842d096bf8a))
* **who-watched:** send error if no series was found ([dd901c4](https://github.com/lucas-dclrcq/homelab-manager/commit/dd901c419f8a95616e315b900a1c57d14bd915f8))


### Bug Fixes

* add missing h2 dependency for trixnity ([f054572](https://github.com/lucas-dclrcq/homelab-manager/commit/f0545725d7170f049171f6e2ead33e8b0b9a6538))
* **arr:** movie quality should be parsed from webhook payload ([e33dd3a](https://github.com/lucas-dclrcq/homelab-manager/commit/e33dd3a55fb4500b8c0cd0e1e164353e9e74cac1))
* **ci:** resolve npm packages from npmjs instead of private nexus ([df5e8b0](https://github.com/lucas-dclrcq/homelab-manager/commit/df5e8b022ca73e8c56f58b08c1bc6cb77e6c88d8))
* concatenate watchers with new line ([5bf778f](https://github.com/lucas-dclrcq/homelab-manager/commit/5bf778fcf903662839d2c3af266a308b3d4e120a))
* **deado-data:** Implement weighted random selection for deado responses ([5c0bd9a](https://github.com/lucas-dclrcq/homelab-manager/commit/5c0bd9ab89e3a1cd0f355ce45ee05b57cb4b6d5a))
* **deps:** update dependency io.quarkus.platform:quarkus-bom to v3.19.1 ([07c0e1f](https://github.com/lucas-dclrcq/homelab-manager/commit/07c0e1f3590589cb2223830bb2fa754f9200cc56))
* **deps:** update dependency io.smallrye.reactive:smallrye-mutiny-vertx-web-client to v3.18.1 ([62538d3](https://github.com/lucas-dclrcq/homelab-manager/commit/62538d3c84080929ce5fbc589ecdd37e90075713))
* **deps:** update dependency io.smallrye.reactive:smallrye-mutiny-vertx-web-client to v3.19.0 ([399ef09](https://github.com/lucas-dclrcq/homelab-manager/commit/399ef09b3d23389ffb725c84cd56bd6d449c9703))
* **deps:** update dependency org.fuchss:matrix-bot-base to v0.14.0 ([150b4dd](https://github.com/lucas-dclrcq/homelab-manager/commit/150b4dd72a038803201d1998bf8881c709bfad23))
* **deps:** update quarkus.platform.version to v3.18.4 ([3e5181c](https://github.com/lucas-dclrcq/homelab-manager/commit/3e5181cc9592ad9147b978e5567b364052f4f9df))
* **deps:** update quarkus.platform.version to v3.19.2 ([f400def](https://github.com/lucas-dclrcq/homelab-manager/commit/f400def1cbf8eb61192e45d77d45093829601007))
* **deps:** update quarkus.platform.version to v3.19.3 ([1b91007](https://github.com/lucas-dclrcq/homelab-manager/commit/1b91007ed9e3482f57c772b56b7ed1b8ae4f0ca9))
* **deps:** update quarkus.platform.version to v3.19.4 ([7883d2f](https://github.com/lucas-dclrcq/homelab-manager/commit/7883d2f07cb4dad08f5ce0952d090aa0d36e9e05))
* **deps:** update quarkus.platform.version to v3.21.4 ([1953bfb](https://github.com/lucas-dclrcq/homelab-manager/commit/1953bfb83f5758714b790868e22b65d7dbbbd090))
* **deps:** update quarkus.platform.version to v3.23.0 ([247234a](https://github.com/lucas-dclrcq/homelab-manager/commit/247234a1afd8b9b872c69bbbe3a49493fe103b92))
* **docker:** should not ignore operatur stuff ([090b049](https://github.com/lucas-dclrcq/homelab-manager/commit/090b049138bddc7118d8a71bdb68ebd4d523e392))
* don't keep leader status when sync init as crashed ([ade51f4](https://github.com/lucas-dclrcq/homelab-manager/commit/ade51f46b17dacf513ebafe22b83dfbeb2e2b80b))
* enable context on scheduled jobs ([c48f9cd](https://github.com/lucas-dclrcq/homelab-manager/commit/c48f9cde3fe8cf383f792a9e2ab135ef3b9fbe82))
* fix album download title ([e00c905](https://github.com/lucas-dclrcq/homelab-manager/commit/e00c90588f4ec6920251e9a39bbdc42c6bba8a12))
* fix bot tests ([5080c98](https://github.com/lucas-dclrcq/homelab-manager/commit/5080c98360d79e8809f8de8c2f253025d513bd1b))
* fix devservices element url ([ece00c9](https://github.com/lucas-dclrcq/homelab-manager/commit/ece00c996cc75a4b2916079af554feedff609148))
* fix johnny help command ([d117b2f](https://github.com/lucas-dclrcq/homelab-manager/commit/d117b2f7243e121cd802ac55973e5a18e55be2e6))
* handle errors in bot command ([1ce5b1f](https://github.com/lucas-dclrcq/homelab-manager/commit/1ce5b1f739e0066974fe9f561df4904098257ae6))
* **homelab-manager:** rollback to default user ([43cec54](https://github.com/lucas-dclrcq/homelab-manager/commit/43cec54ff955beb2030d2b20ad5a851e6fff2cf9))
* injection issue with body logger ([677f242](https://github.com/lucas-dclrcq/homelab-manager/commit/677f242c70651c885f0659b20838f5e08fa87da2))
* **matrix:** update HTTP method from POST to PUT for sending messages ([b91bc3a](https://github.com/lucas-dclrcq/homelab-manager/commit/b91bc3a082711345b92941431d759f7cc0059be3))
* parsing of bazarr webhook ([ca596fd](https://github.com/lucas-dclrcq/homelab-manager/commit/ca596fdabee073a56bda6a673a5dce9046bc7b3b))
* prevent crashing sync on start ([2707117](https://github.com/lucas-dclrcq/homelab-manager/commit/2707117264f26681130cc73284d313fb0f98717d))
* remove kstream timeout configs ([61de471](https://github.com/lucas-dclrcq/homelab-manager/commit/61de4716be87d90d57306fcc00e75f0856d7dd2b))
* **skong:** doubter & believer are reversed ([c06fdb8](https://github.com/lucas-dclrcq/homelab-manager/commit/c06fdb89797d7fafc295063b221e65b215a49ef1))
* try another way to release leadership ([cbc74eb](https://github.com/lucas-dclrcq/homelab-manager/commit/cbc74eb126db85c21a690528a893c37572c3ac57))
* **who-watched:** fix html format + sort by last viewed ([7643164](https://github.com/lucas-dclrcq/homelab-manager/commit/7643164e5e3d56beb54026f0e365289b3e707942))
* **who-watched:** use jellyfin media title instead of searchParameter ([d905a21](https://github.com/lucas-dclrcq/homelab-manager/commit/d905a2140af6fa1e53228aa42d3f0bf23f133e94))


### Documentation

* improve swagger documentation ([b6fe394](https://github.com/lucas-dclrcq/homelab-manager/commit/b6fe394542621883de708acfdbf771bbc412a89a))
* **readme:** add readme and license ([95a742a](https://github.com/lucas-dclrcq/homelab-manager/commit/95a742a6606a828f3bce96e9bfa9efd38b3a20b6))
