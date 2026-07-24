# Changelog

## [0.6.0](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.5.1...v0.6.0) (2026-07-24)


### Features

* add additional info to issue created notification ([4d3eb63](https://github.com/lucas-dclrcq/homelab-manager/commit/4d3eb633eb38d7ba8e4cf0e2d6a96dbaf5ac3274))
* add cleanup module ([19c7946](https://github.com/lucas-dclrcq/homelab-manager/commit/19c7946fe998d951c4b23d8fecc17c98bdd32089))
* add emojis on notifications ([585fd70](https://github.com/lucas-dclrcq/homelab-manager/commit/585fd70e19e332a4ad736a36d0506af8da42d6a6))
* add endpoint for Radarr webhooks ([16c65a7](https://github.com/lucas-dclrcq/homelab-manager/commit/16c65a77df6a145cb37564be8b327e9a32ab4237))
* add endpoint to send weekly report ([6b2b08c](https://github.com/lucas-dclrcq/homelab-manager/commit/6b2b08cb6238f4584ac67dc48eaa61b3283d9c03))
* add fault tolerance to all external REST clients ([299cea8](https://github.com/lucas-dclrcq/homelab-manager/commit/299cea854e96c44d986112f313a8c7ee9b70cfe8))
* add finances tab ([55f5766](https://github.com/lucas-dclrcq/homelab-manager/commit/55f576695808f80177c5086157cd0eeb37224115))
* add logging on all commands ([52e120a](https://github.com/lucas-dclrcq/homelab-manager/commit/52e120ab4144a3172acc2b5cd6f86cf89561ecd9))
* add more emojis ([2ce134a](https://github.com/lucas-dclrcq/homelab-manager/commit/2ce134a15b69324eb8cb2a268f11e5870dfeff37))
* add optional api key auth on notification endpoints ([d5bad74](https://github.com/lucas-dclrcq/homelab-manager/commit/d5bad74dd0bbe128113bf777603878e59d2f9d30))
* add pagination on protection list ([0f62c66](https://github.com/lucas-dclrcq/homelab-manager/commit/0f62c66626eab80bd45aa8785829532f17a52b25))
* add release year to album notifications ([a43e23f](https://github.com/lucas-dclrcq/homelab-manager/commit/a43e23f2ca817bfb3227077abc109493127ff85a))
* add synapse devservices to enable testing locally ([5a4def0](https://github.com/lucas-dclrcq/homelab-manager/commit/5a4def075f5a99b9a55150df038234da566119b4))
* add who watched bot command ([682cf73](https://github.com/lucas-dclrcq/homelab-manager/commit/682cf73ce048d43b24ea2bb95d64a242b3a1b371))
* **admin:** can completely manage applications from admin ([1459331](https://github.com/lucas-dclrcq/homelab-manager/commit/14593319bbdec637b89dd87116e75b277a74cbac))
* **applications:** type the create/update responses in the OpenAPI contract ([8359d45](https://github.com/lucas-dclrcq/homelab-manager/commit/8359d455ea53c14ceb5d4ae4104fcd47d25ad853))
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
* media deletion suggestions by users ([1c0a072](https://github.com/lucas-dclrcq/homelab-manager/commit/1c0a072c3058ad741fa731d68ee9b1a44a0e5306))
* **monthly-stats-report:** add most popular series of the month ([e28be67](https://github.com/lucas-dclrcq/homelab-manager/commit/e28be67a240791bc4880f6d7e04be7c11e675a2f))
* **notifications:** add generic notification route ([888132d](https://github.com/lucas-dclrcq/homelab-manager/commit/888132de881b2946bcfef595eed27235f9d1d8b2))
* **notifications:** send resolved notification in thread ([be1e03d](https://github.com/lucas-dclrcq/homelab-manager/commit/be1e03dc70a60b8ba350441d0da2f4099dfa974e))
* **notifications:** send weekly whats next report ([93ab5c7](https://github.com/lucas-dclrcq/homelab-manager/commit/93ab5c7936d270578a5c6cd00610d343b0e9f368))
* notify subtitles downloads ([7bedb7a](https://github.com/lucas-dclrcq/homelab-manager/commit/7bedb7a7a0a2cd230e598591296e782086fc80fc))
* prevent null exception in radarr notification consumer ([ea2775c](https://github.com/lucas-dclrcq/homelab-manager/commit/ea2775cc9cfa84cfaccab2f90bfb89fb8c20b73e))
* **problems:** better handle blocked imports ([b8d559b](https://github.com/lucas-dclrcq/homelab-manager/commit/b8d559b9cd191e6cd7b6f70e52a6f679b78bcfa9))
* **problems:** other problem reports, tv support, admin module, release recommendations ([42dc89a](https://github.com/lucas-dclrcq/homelab-manager/commit/42dc89a835a96da3d91eddece05c90c0e8fa7d85))
* **problems:** Problèmes UI — tv/series, other-problem reports, admin module, recommended releases ([6a5868b](https://github.com/lucas-dclrcq/homelab-manager/commit/6a5868b822eaa12b36efb3f03c4698886c93cb5a))
* remove deprecated arr notification endpoint ([bb33a38](https://github.com/lucas-dclrcq/homelab-manager/commit/bb33a38e5aa62f43a56a8bbecc6514bf6594425c))
* remove leader stuff ([71d642e](https://github.com/lucas-dclrcq/homelab-manager/commit/71d642e3fd258466abc049c47943551587d5257e))
* replace http calls with trixnity ([8976c0c](https://github.com/lucas-dclrcq/homelab-manager/commit/8976c0ce9d46299bb4a8fae6e0f27314b40320a1))
* rework weekly report ([bafedb4](https://github.com/lucas-dclrcq/homelab-manager/commit/bafedb469501e95a03c2b33f1bc957d562b59a3a))
* send jellyseerr issue notifications to matrix ([c7d4215](https://github.com/lucas-dclrcq/homelab-manager/commit/c7d4215f1b0ce326269130dcaccc23bfe8474e29))
* send notification on problem workflow events ([5ea851e](https://github.com/lucas-dclrcq/homelab-manager/commit/5ea851e64c42796372574f739f3ab654c6ae6cd6))
* **series:** fix parsing of series episode quality ([c2719a3](https://github.com/lucas-dclrcq/homelab-manager/commit/c2719a3b76ee38fa8917fece4c36576702fea093))
* show album downloads in timeline ([9da28d3](https://github.com/lucas-dclrcq/homelab-manager/commit/9da28d39a1a23f904f4167b28593708235746182))
* skong ([edb8028](https://github.com/lucas-dclrcq/homelab-manager/commit/edb802899337f30788229a169f58fc34edeb6110))
* **sonarr:** add imdb link ([ae95db1](https://github.com/lucas-dclrcq/homelab-manager/commit/ae95db16445f232533a17c5013c74408b47aa075))
* **sonarr:** add ParseSeries class for handling series parsing and refactor notifications ([1527ebf](https://github.com/lucas-dclrcq/homelab-manager/commit/1527ebf3bc65860ab4bbba2cf80cd18c44469266))
* **sonarr:** add season and episode number on notification ([033e8d7](https://github.com/lucas-dclrcq/homelab-manager/commit/033e8d73b5f75be4ec424682fab76b1427225390))
* **sonarr:** send matrix notification on sonarr events ([e8b412a](https://github.com/lucas-dclrcq/homelab-manager/commit/e8b412a24fcc4b59e2c2d98b2b475698f856a951))
* split cleanup page into Campagnes/Propositions/Protection tabs ([#123](https://github.com/lucas-dclrcq/homelab-manager/issues/123)) ([55fb34c](https://github.com/lucas-dclrcq/homelab-manager/commit/55fb34c2fdc4103e91872eeb519be1ebc73f10cd))
* start implementing interface ([842b0bb](https://github.com/lucas-dclrcq/homelab-manager/commit/842b0bb1666865536aed1d68b1ace733fc9302f5))
* **statistics:** tabbed stats page with Jellystat-inspired categories ([#148](https://github.com/lucas-dclrcq/homelab-manager/issues/148)) ([1c5606c](https://github.com/lucas-dclrcq/homelab-manager/commit/1c5606c2a26f0bae5cd73d4d85749a3581ec40ac))
* **stats:** add currently playing + general stats on vieweing ([26902ba](https://github.com/lucas-dclrcq/homelab-manager/commit/26902ba48e96dd6845559d53732d2728cf854c0e))
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

* adapt TimeService for kotlinx-datetime 0.8.0 / Kotlin 2.4.0 compatibility ([e3135ff](https://github.com/lucas-dclrcq/homelab-manager/commit/e3135ff43569a6ca905ce40b526d6e0ba716e8a0))
* add missing h2 dependency for trixnity ([f054572](https://github.com/lucas-dclrcq/homelab-manager/commit/f0545725d7170f049171f6e2ead33e8b0b9a6538))
* another try at infinite loop because of cache control ([fc7b6a7](https://github.com/lucas-dclrcq/homelab-manager/commit/fc7b6a760c40d3241b9809fe93a07090556e5ae0))
* **arr:** movie quality should be parsed from webhook payload ([e33dd3a](https://github.com/lucas-dclrcq/homelab-manager/commit/e33dd3a55fb4500b8c0cd0e1e164353e9e74cac1))
* **ci:** resolve npm packages from npmjs instead of private nexus ([df5e8b0](https://github.com/lucas-dclrcq/homelab-manager/commit/df5e8b022ca73e8c56f58b08c1bc6cb77e6c88d8))
* concatenate watchers with new line ([5bf778f](https://github.com/lucas-dclrcq/homelab-manager/commit/5bf778fcf903662839d2c3af266a308b3d4e120a))
* **deado-data:** Implement weighted random selection for deado responses ([5c0bd9a](https://github.com/lucas-dclrcq/homelab-manager/commit/5c0bd9ab89e3a1cd0f355ce45ee05b57cb4b6d5a))
* **deps:** update dependency io.ktor:ktor-client-java-jvm to v3.5.1 ([2554f3b](https://github.com/lucas-dclrcq/homelab-manager/commit/2554f3b2984c3321ffde4b831f10bc42fe33de94))
* **deps:** update dependency io.quarkiverse.quinoa:quarkus-quinoa to v2.8.3 ([5dae712](https://github.com/lucas-dclrcq/homelab-manager/commit/5dae712f12bffb870fd8b1222b3583e472c95084))
* **deps:** update dependency io.quarkiverse.wiremock:quarkus-wiremock to v1.6.3 ([a9be90f](https://github.com/lucas-dclrcq/homelab-manager/commit/a9be90f8a7d930427ed6429567bede39e62ed673))
* **deps:** update dependency io.quarkus.platform:quarkus-bom to v3.19.1 ([07c0e1f](https://github.com/lucas-dclrcq/homelab-manager/commit/07c0e1f3590589cb2223830bb2fa754f9200cc56))
* **deps:** update dependency io.smallrye.reactive:smallrye-mutiny-vertx-web-client to v3.18.1 ([62538d3](https://github.com/lucas-dclrcq/homelab-manager/commit/62538d3c84080929ce5fbc589ecdd37e90075713))
* **deps:** update dependency io.smallrye.reactive:smallrye-mutiny-vertx-web-client to v3.19.0 ([399ef09](https://github.com/lucas-dclrcq/homelab-manager/commit/399ef09b3d23389ffb725c84cd56bd6d449c9703))
* **deps:** update dependency org.commonmark:commonmark to v0.29.0 ([447d60e](https://github.com/lucas-dclrcq/homelab-manager/commit/447d60efa9cc560393e738ce34b3e8ae8ce8b618))
* **deps:** update dependency org.fuchss:matrix-bot-base to v0.14.0 ([150b4dd](https://github.com/lucas-dclrcq/homelab-manager/commit/150b4dd72a038803201d1998bf8881c709bfad23))
* **deps:** update dependency org.jetbrains.kotlinx:kotlinx-datetime-jvm to v0.8.0-0.6.x-compat ([20a9e4c](https://github.com/lucas-dclrcq/homelab-manager/commit/20a9e4c65362fb5685ffd1cbd5b6699857001e1b))
* **deps:** update dependency pinia to v4 ([#130](https://github.com/lucas-dclrcq/homelab-manager/issues/130)) ([9b0a8cd](https://github.com/lucas-dclrcq/homelab-manager/commit/9b0a8cd20ef9e50d244f7b8f072ce7a8d770ff4c))
* **deps:** update quarkus.platform.version to v3.18.4 ([3e5181c](https://github.com/lucas-dclrcq/homelab-manager/commit/3e5181cc9592ad9147b978e5567b364052f4f9df))
* **deps:** update quarkus.platform.version to v3.19.2 ([f400def](https://github.com/lucas-dclrcq/homelab-manager/commit/f400def1cbf8eb61192e45d77d45093829601007))
* **deps:** update quarkus.platform.version to v3.19.3 ([1b91007](https://github.com/lucas-dclrcq/homelab-manager/commit/1b91007ed9e3482f57c772b56b7ed1b8ae4f0ca9))
* **deps:** update quarkus.platform.version to v3.19.4 ([7883d2f](https://github.com/lucas-dclrcq/homelab-manager/commit/7883d2f07cb4dad08f5ce0952d090aa0d36e9e05))
* **deps:** update quarkus.platform.version to v3.21.4 ([1953bfb](https://github.com/lucas-dclrcq/homelab-manager/commit/1953bfb83f5758714b790868e22b65d7dbbbd090))
* **deps:** update quarkus.platform.version to v3.23.0 ([247234a](https://github.com/lucas-dclrcq/homelab-manager/commit/247234a1afd8b9b872c69bbbe3a49493fe103b92))
* **deps:** update quarkus.platform.version to v3.37.2 ([41d8a88](https://github.com/lucas-dclrcq/homelab-manager/commit/41d8a8884fa529874fbd5ba4d0b7548448f14a7b))
* **deps:** update quarkus.platform.version to v3.37.3 ([#127](https://github.com/lucas-dclrcq/homelab-manager/issues/127)) ([85f0626](https://github.com/lucas-dclrcq/homelab-manager/commit/85f0626ccc9f2c33353879ab3a6648d1f234fb62))
* **deps:** update trixnity.version to v5.6.0 ([504736f](https://github.com/lucas-dclrcq/homelab-manager/commit/504736f99d584592a2664671bc740c90f5bbc334))
* **docker:** image needs latest glibc for matrix crypto lib to work ([a488a42](https://github.com/lucas-dclrcq/homelab-manager/commit/a488a42667ef76733c77d8fc0d5b46945bb353f6))
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
* **problems:** base release recommendation on desired quality, not current file ([f720513](https://github.com/lucas-dclrcq/homelab-manager/commit/f72051307732974ee519572c0e99c7954f73432b))
* **problems:** pin isFrench/isRecommended JSON keys so the default filter works ([38e0861](https://github.com/lucas-dclrcq/homelab-manager/commit/38e0861f245da79d5a777e467b732762e64e4601))
* **problems:** recommend 1080p+ VF/MULTI torrents regardless of exact profile target ([e6b6438](https://github.com/lucas-dclrcq/homelab-manager/commit/e6b6438952cfb48596fe2e95dfd2f8461e07c857))
* **problems:** recommend only the exact desired resolution, not higher ([7772470](https://github.com/lucas-dclrcq/homelab-manager/commit/777247089f82915ed495a678064163e99112e1a5))
* remove kstream timeout configs ([61de471](https://github.com/lucas-dclrcq/homelab-manager/commit/61de4716be87d90d57306fcc00e75f0856d7dd2b))
* resolve merge conflict in package.json ([f5517d9](https://github.com/lucas-dclrcq/homelab-manager/commit/f5517d944bcadf9ad484c52cfb36a9e8de3dce77))
* **skong:** doubter & believer are reversed ([c06fdb8](https://github.com/lucas-dclrcq/homelab-manager/commit/c06fdb89797d7fafc295063b221e65b215a49ef1))
* try another way to release leadership ([cbc74eb](https://github.com/lucas-dclrcq/homelab-manager/commit/cbc74eb126db85c21a690528a893c37572c3ac57))
* try to avoid infinite loop ([f266f36](https://github.com/lucas-dclrcq/homelab-manager/commit/f266f369fce2223fdce0fb2fde262b9fdcf40204))
* **who-watched:** fix html format + sort by last viewed ([7643164](https://github.com/lucas-dclrcq/homelab-manager/commit/7643164e5e3d56beb54026f0e365289b3e707942))
* **who-watched:** use jellyfin media title instead of searchParameter ([d905a21](https://github.com/lucas-dclrcq/homelab-manager/commit/d905a2140af6fa1e53228aa42d3f0bf23f133e94))


### Documentation

* add AGENTS.md context file for AI coding agents ([#121](https://github.com/lucas-dclrcq/homelab-manager/issues/121)) ([6acc5f2](https://github.com/lucas-dclrcq/homelab-manager/commit/6acc5f23b07c1bb295144494b28089854c75d3fe))
* improve swagger documentation ([b6fe394](https://github.com/lucas-dclrcq/homelab-manager/commit/b6fe394542621883de708acfdbf771bbc412a89a))
* **readme:** add readme and license ([95a742a](https://github.com/lucas-dclrcq/homelab-manager/commit/95a742a6606a828f3bce96e9bfa9efd38b3a20b6))
* **README:** rewrite ([a62f03a](https://github.com/lucas-dclrcq/homelab-manager/commit/a62f03a845b16332314640b17056dc11547c669c))


### Refactoring

* **applications:** extract applications vertical with bean validation ([aaab78d](https://github.com/lucas-dclrcq/homelab-manager/commit/aaab78d3e615fb7d810286a2be223cbd455d4b79))
* **corrector:** extract corrector vertical with hexagonal ports and use cases ([81da626](https://github.com/lucas-dclrcq/homelab-manager/commit/81da62689aecf07fcf5a7c0d009e38d8b6aa8d0c))
* extract shared infrastructure (arr clients, matrix machinery, security, time) ([cb9f554](https://github.com/lucas-dclrcq/homelab-manager/commit/cb9f55419ff735e59b0f1f922c24000a82b4dcba))
* fix compilation warnings ([dd869c8](https://github.com/lucas-dclrcq/homelab-manager/commit/dd869c8393e94e8f905125e4979f36cb4bc75234))
* **giphy:** replace raw Vert.x WebClient with a @Url REST client ([3ad1aea](https://github.com/lucas-dclrcq/homelab-manager/commit/3ad1aea7051714df6e6bb3d913326e9c7d519b00))
* group admin modules under unique admin page ([adbf08d](https://github.com/lucas-dclrcq/homelab-manager/commit/adbf08d3e50e240261378946e63af0089a83ff60))
* improve vertx context use by matrix bot ([4190dd1](https://github.com/lucas-dclrcq/homelab-manager/commit/4190dd1be848811db67dbd6b3b780ec904936494))
* inject matrix commands ([8bf1bcc](https://github.com/lucas-dclrcq/homelab-manager/commit/8bf1bcce397b66daf79cf5fa02250a2853af678a))
* **library,jobsadmin:** extract library and jobsadmin verticals ([8cf82b7](https://github.com/lucas-dclrcq/homelab-manager/commit/8cf82b7d2012e3b1069369890bb3d1acbb41b0f0))
* make the architecture more hexagonal ([ad2ac91](https://github.com/lucas-dclrcq/homelab-manager/commit/ad2ac91f5d82ae0f1f4033470f6c8543c958401b))
* migrate to kotlin ([a1f3b82](https://github.com/lucas-dclrcq/homelab-manager/commit/a1f3b820cb4fbd149c74a8d708dc9b76465cef23))
* move wiremock stuff into config folder ([8e2f100](https://github.com/lucas-dclrcq/homelab-manager/commit/8e2f100b5a6d9660b6b393badd17fd098b5aab41))
* **notifications:** extract notifications vertical with hexagonal ports and use cases ([12d44db](https://github.com/lucas-dclrcq/homelab-manager/commit/12d44dbef24e66c8861e08838c5666262235cd73))
* **notifications:** Rename packages for structure ([9876e94](https://github.com/lucas-dclrcq/homelab-manager/commit/9876e94f99e34cf6c886b12c37eb58f57953460e))
* **operator:** use native http security policy instead of custom filter to protect operator routes ([403aa64](https://github.com/lucas-dclrcq/homelab-manager/commit/403aa64717ea8fee344d30060499af5c135ba626))
* **problems:** rename El Corrector module to Problems ([94e8bb0](https://github.com/lucas-dclrcq/homelab-manager/commit/94e8bb00e7e3e573946e7429a8a236fc3e4c9d18))
* remove bot library to make customisation easier ([0132a02](https://github.com/lucas-dclrcq/homelab-manager/commit/0132a022fc4df6788b83bc3d09a607ed535207d3))
* remove matrix notification sender ([5d71b06](https://github.com/lucas-dclrcq/homelab-manager/commit/5d71b0644808a35119f51a9bfb33f0cb3e34a268))
* remove notification builder ([d979ad0](https://github.com/lucas-dclrcq/homelab-manager/commit/d979ad005e2a40e0622f35d3adcb6fd86e3f8346))
* remove unneeded models ([2ff95a3](https://github.com/lucas-dclrcq/homelab-manager/commit/2ff95a37fd881758737d79e2f9b96349924ae74d))
* reorganize packages ([08143d7](https://github.com/lucas-dclrcq/homelab-manager/commit/08143d795d197d2c544a63a9c7df3774161cb153))
* rework what's next report ([9e8950c](https://github.com/lucas-dclrcq/homelab-manager/commit/9e8950ccedf0009474106d0eab2d42bf61aa79bf))
* simplify code architecture ([16d8d55](https://github.com/lucas-dclrcq/homelab-manager/commit/16d8d550cca444a6ce1579d747fd8f9876fd4c7b))
* split notification resource ([6582c20](https://github.com/lucas-dclrcq/homelab-manager/commit/6582c20c7295c8629221561c63c1296e47d7a999))
* **time:** replace java.time with kotlinx.datetime for consistency ([2cb8daa](https://github.com/lucas-dclrcq/homelab-manager/commit/2cb8daa0d74cc3dfc95791213144c47161e18b45))
* update deado weights ([4bdc454](https://github.com/lucas-dclrcq/homelab-manager/commit/4bdc454172c3235adb9e739c9c6a2438465e8cb1))
* use a more native quarkus way of setting up trixnity bot ([033189f](https://github.com/lucas-dclrcq/homelab-manager/commit/033189fc1c49b10c8945f88e3395827398f7cece))
* use api key for operator to communicate with app ([83d422c](https://github.com/lucas-dclrcq/homelab-manager/commit/83d422c546b2400a0741a37dddecc0e1acc0689f))
* use dto for sonarr notifications ([11c3ea1](https://github.com/lucas-dclrcq/homelab-manager/commit/11c3ea19adbe3fcba50f2658e5fd6b3a595e5888))


### Styles

* completely rework design system ([fbed9f0](https://github.com/lucas-dclrcq/homelab-manager/commit/fbed9f08597c065fced6e890ddc1c42ebbe826a3))
* **logging:** update log level for Kafka category ([e50cb8d](https://github.com/lucas-dclrcq/homelab-manager/commit/e50cb8d2f642cd261dd3cdaa9c8bfa6aa9697bcc))
* make theme more friendly ([6e33b96](https://github.com/lucas-dclrcq/homelab-manager/commit/6e33b960287c02649ca807867d4c6f807094da0f))
* remove categories on side nav ([187d227](https://github.com/lucas-dclrcq/homelab-manager/commit/187d2276c4ad50f73d599da0b83444b7e4641f3a))
* vertically align logo with title ([b97ef9c](https://github.com/lucas-dclrcq/homelab-manager/commit/b97ef9c7fea2453509ca5e6a17707a60e90bf37f))


### Tests

* add frontend test strategy ([8fc73cc](https://github.com/lucas-dclrcq/homelab-manager/commit/8fc73cc01bd366ca00481fce5505aa3bed1453c4))
* convert test method names to backtick notation ([860a2f2](https://github.com/lucas-dclrcq/homelab-manager/commit/860a2f27b1427ad8f5d8437b3cf44dc7e371848f))
* fix test for album notifications ([8db2f84](https://github.com/lucas-dclrcq/homelab-manager/commit/8db2f84f7bd821c4985c816632b2c05ce843e14a))
* focus on it tests instead of unit ([6eba0b7](https://github.com/lucas-dclrcq/homelab-manager/commit/6eba0b7a9ae2b35cb3425cf9de5d02a08543e6c1))
* it test should run against unique matrix room ([bcf0c53](https://github.com/lucas-dclrcq/homelab-manager/commit/bcf0c53ea7092c2fe5a7729ed353765abe26d682))
* replace WireMock Matrix stubs with real Synapse testcontainer ([4941328](https://github.com/lucas-dclrcq/homelab-manager/commit/49413287b7efd9051a1a0e0141e3e8195770ad96))
* speed up bot invite in room using awaitility ([38acd06](https://github.com/lucas-dclrcq/homelab-manager/commit/38acd06dd62c223d5c7ef24eec13802dc6b35c36))
* use synapse devservice in tests ([6ef94ce](https://github.com/lucas-dclrcq/homelab-manager/commit/6ef94ce8c981c2435b932d43ed1499add210eae4))
* **who-watched:** add it tests ([241ae59](https://github.com/lucas-dclrcq/homelab-manager/commit/241ae596dfc8de3525a8ad490ec146c4d58c74ea))


### CI/CD

* add code coverage ([#118](https://github.com/lucas-dclrcq/homelab-manager/issues/118)) ([b1ade2f](https://github.com/lucas-dclrcq/homelab-manager/commit/b1ade2f66a2e1c54df457f098098fedb08e84986))
* build & push docker image ([5cdac7f](https://github.com/lucas-dclrcq/homelab-manager/commit/5cdac7f12ce1db792e40a2a2da6bc0d5f9a3b829))
* clone gitops repo ([1d39d81](https://github.com/lucas-dclrcq/homelab-manager/commit/1d39d816e85bfe204691aaeb96bc6f076ba7930e))
* commit and push new version to gitops repo ([f362899](https://github.com/lucas-dclrcq/homelab-manager/commit/f362899e76483c7621fb596ba80b06d7cea1d0b2))
* commit new sha to gitops repo ([6a1f539](https://github.com/lucas-dclrcq/homelab-manager/commit/6a1f5396b0c4461176cf2be390101fb4b7ca5011))
* condition deploy job to not on pr ([a534d87](https://github.com/lucas-dclrcq/homelab-manager/commit/a534d8750a38d481efa88a528f53b23858c1fbb0))
* fix path of docker image tag ([874e5d2](https://github.com/lucas-dclrcq/homelab-manager/commit/874e5d2b548e12234537c229a348b30b63df43fc))
* generate test reports ([0a09c9f](https://github.com/lucas-dclrcq/homelab-manager/commit/0a09c9f690cbdf64fb734c0939879ea7d3ed83c5))
* push custom helm chart ([6d2a21c](https://github.com/lucas-dclrcq/homelab-manager/commit/6d2a21c2a066f2d62897f8553b63dade3cdbf2c0))
* remove failing push of test report ([68a89b2](https://github.com/lucas-dclrcq/homelab-manager/commit/68a89b29167f64c3245a960a4faae8115d7ed512))
* remove push to homelab repo ([4945218](https://github.com/lucas-dclrcq/homelab-manager/commit/49452185b25c1c988b556114b24b94aeb42f656d))
* use redpanda quay.io image to prevent docker hub rate limiting ([723ee21](https://github.com/lucas-dclrcq/homelab-manager/commit/723ee2162c96677ff67d8821cfee118655c3ae5f))


### Chores

* add health endpoints ([a6e2c25](https://github.com/lucas-dclrcq/homelab-manager/commit/a6e2c25b7f881bca29610a2a2bd41ac1bda8f667))
* add more logging ([9fb7226](https://github.com/lucas-dclrcq/homelab-manager/commit/9fb7226ab81b9183a2265cee2265684bab1f213d))
* **deps:** update actions/checkout action to v7 ([af64d72](https://github.com/lucas-dclrcq/homelab-manager/commit/af64d7241999d78e50190a090487266809b51349))
* **deps:** update actions/checkout digest to 11bd719 ([c2115d9](https://github.com/lucas-dclrcq/homelab-manager/commit/c2115d9fbb3cfbd6102b90dfd2d780ccc040da39))
* **deps:** update actions/checkout digest to 34e1148 ([9c19abd](https://github.com/lucas-dclrcq/homelab-manager/commit/9c19abda54309fdaf434e79b908e26eb72105904))
* **deps:** update actions/setup-java action to v4 ([8d8bf62](https://github.com/lucas-dclrcq/homelab-manager/commit/8d8bf629bbd3b52ef095663e83196875ad6fb8f7))
* **deps:** update actions/setup-java action to v5 ([6a12929](https://github.com/lucas-dclrcq/homelab-manager/commit/6a129292436eccfe0e95dcfc987f81f447527fb7))
* **deps:** update actions/setup-java action to v5 ([#129](https://github.com/lucas-dclrcq/homelab-manager/issues/129)) ([2f8686c](https://github.com/lucas-dclrcq/homelab-manager/commit/2f8686c6d256820874f3d7d876d4f619a40d653e))
* **deps:** update dependency @vitejs/plugin-vue to v5.2.4 ([153b7e4](https://github.com/lucas-dclrcq/homelab-manager/commit/153b7e4007fcb8802b2c7bd4a95d793c88857eeb))
* **deps:** update dependency @vitejs/plugin-vue to v6 ([#102](https://github.com/lucas-dclrcq/homelab-manager/issues/102)) ([8fdc8ad](https://github.com/lucas-dclrcq/homelab-manager/commit/8fdc8adf35c37e91caca916e96a0071b0dd00519))
* **deps:** update dependency @vue/tsconfig to ^0.9.0 ([6a1eca1](https://github.com/lucas-dclrcq/homelab-manager/commit/6a1eca11dc98012a2d87da6dbbdcf3dceb34771d))
* **deps:** update dependency io.smallrye:jandex-maven-plugin to v3.6.0 ([44bba1a](https://github.com/lucas-dclrcq/homelab-manager/commit/44bba1a7c13051fef51439847c242632c75776c9))
* **deps:** update dependency maven to v3.9.16 ([4727686](https://github.com/lucas-dclrcq/homelab-manager/commit/472768691cc66e9ac2299174732d0fa310bef715))
* **deps:** update dependency maven to v3.9.9 ([45ea937](https://github.com/lucas-dclrcq/homelab-manager/commit/45ea937a33c1f785a8d05f56954abb1883328d9f))
* **deps:** update dependency maven-wrapper to v3.3.4 ([6959552](https://github.com/lucas-dclrcq/homelab-manager/commit/6959552e74ec112962a87562a104e773454ff6cc))
* **deps:** update dependency org.apache.maven.plugins:maven-compiler-plugin to v3.14.0 ([8a57b4f](https://github.com/lucas-dclrcq/homelab-manager/commit/8a57b4f267f716fcf94879703ee21a1607564373))
* **deps:** update dependency org.apache.maven.plugins:maven-compiler-plugin to v3.15.0 ([cddda0f](https://github.com/lucas-dclrcq/homelab-manager/commit/cddda0f67ee479b54323031f16ba11a82d7bc0b2))
* **deps:** update dependency org.apache.maven.plugins:maven-surefire-report-plugin to v3.5.3 ([e6f7bfe](https://github.com/lucas-dclrcq/homelab-manager/commit/e6f7bfec5acc8c2f9215f3cd7228b93fb2c830a8))
* **deps:** update dependency org.apache.maven.plugins:maven-surefire-report-plugin to v3.5.6 ([36ee2ca](https://github.com/lucas-dclrcq/homelab-manager/commit/36ee2ca6fb85728ffeac76dd0d66cfaeccd32a3f))
* **deps:** update dependency org.assertj:assertj-core to v3.27.3 ([01522c5](https://github.com/lucas-dclrcq/homelab-manager/commit/01522c57587180d7db6e59ed5db8fb43270d2cf6))
* **deps:** update dependency org.assertj:assertj-core to v3.27.7 ([f37916e](https://github.com/lucas-dclrcq/homelab-manager/commit/f37916ee45f861b6001e21a63a2a14c810a52812))
* **deps:** update dependency org.jacoco:jacoco-maven-plugin to v0.8.15 ([#125](https://github.com/lucas-dclrcq/homelab-manager/issues/125)) ([39411c2](https://github.com/lucas-dclrcq/homelab-manager/commit/39411c253cecc0f86345ac86e40c37d0c0084831))
* **deps:** update dependency org.wiremock:wiremock to v3.12.0 ([1f0f173](https://github.com/lucas-dclrcq/homelab-manager/commit/1f0f17390d88c341cff570a737fd727245aa1147))
* **deps:** update dependency org.wiremock:wiremock to v3.12.1 ([0910800](https://github.com/lucas-dclrcq/homelab-manager/commit/0910800ebdbab11f0213e1ea4247250514ad90ee))
* **deps:** update dependency org.wiremock:wiremock to v3.13.0 ([0b3947d](https://github.com/lucas-dclrcq/homelab-manager/commit/0b3947dc5c67590f0969bf785c4b4311ac1d2957))
* **deps:** update dependency org.wiremock:wiremock to v3.13.2 ([4052cd8](https://github.com/lucas-dclrcq/homelab-manager/commit/4052cd87ed71942f969d56f8095a4b02241b9dc6))
* **deps:** update dependency orval to v7.21.0 ([55283ef](https://github.com/lucas-dclrcq/homelab-manager/commit/55283effd95330ab6208bca03309e06afb2433cb))
* **deps:** update dependency prettier to v3.9.5 ([#91](https://github.com/lucas-dclrcq/homelab-manager/issues/91)) ([96bdbf8](https://github.com/lucas-dclrcq/homelab-manager/commit/96bdbf896dca030e56ce61c37d2f244d2fab1f67))
* **deps:** update dependency typescript to ~5.8.0 ([dd50aad](https://github.com/lucas-dclrcq/homelab-manager/commit/dd50aad39f7e244bf09be9f1b640c3c90db9e97e))
* **deps:** update dependency typescript to ~5.9.0 ([#92](https://github.com/lucas-dclrcq/homelab-manager/issues/92)) ([a417a5f](https://github.com/lucas-dclrcq/homelab-manager/commit/a417a5f47716982fbf691c4009f9fa3ac4980af5))
* **deps:** update dependency vite to v6.4.3 ([e9a81c4](https://github.com/lucas-dclrcq/homelab-manager/commit/e9a81c492adbead5dcfc782280c7f17381bf5fd2))
* **deps:** update dependency vue to v3.5.39 ([0465236](https://github.com/lucas-dclrcq/homelab-manager/commit/04652360287b2e8825e31e1ff4073efa6df7f70d))
* **deps:** update dependency vue-tsc to v2.2.12 ([c979040](https://github.com/lucas-dclrcq/homelab-manager/commit/c979040d994c80b5a23c8408a49c6c3ddb261a39))
* **deps:** update docker/build-push-action action to v6 ([0f4b3e3](https://github.com/lucas-dclrcq/homelab-manager/commit/0f4b3e3791a8497ef94e3b91f9f045ec4e760aae))
* **deps:** update docker/build-push-action action to v7 ([2ff5874](https://github.com/lucas-dclrcq/homelab-manager/commit/2ff58740bf20c84eb686e9c9aa4069147440d60e))
* **deps:** update docker/build-push-action digest to 10e90e3 ([ec612ca](https://github.com/lucas-dclrcq/homelab-manager/commit/ec612caad856d3b04b23ebea0d81836b18f0eeb8))
* **deps:** update docker/build-push-action digest to 14487ce ([92b0e50](https://github.com/lucas-dclrcq/homelab-manager/commit/92b0e502f76b89f60058a443a8aeadc16d833c72))
* **deps:** update docker/build-push-action digest to 471d1dc ([82bf36d](https://github.com/lucas-dclrcq/homelab-manager/commit/82bf36d21b4f9ad08e69abe08a7115bb2d9cc5eb))
* **deps:** update docker/login-action action to v4 ([7c32f31](https://github.com/lucas-dclrcq/homelab-manager/commit/7c32f311a3543a0f487387e69c4e594bf0fc2a30))
* **deps:** update docker/login-action digest to 74a5d14 ([5cc2e68](https://github.com/lucas-dclrcq/homelab-manager/commit/5cc2e683f88316c26ff4ab2f50963833c50e4fc9))
* **deps:** update docker/login-action digest to 9780b0c ([a96d54f](https://github.com/lucas-dclrcq/homelab-manager/commit/a96d54f6e1b5893f344462712a1bd19f2489e3c5))
* **deps:** update docker/login-action digest to c94ce9f ([9bbdaac](https://github.com/lucas-dclrcq/homelab-manager/commit/9bbdaacbbb35c8fa48b3861447105492b6e92d8d))
* **deps:** update docker/setup-buildx-action action to v4 ([8363328](https://github.com/lucas-dclrcq/homelab-manager/commit/8363328c9561e2dd303892f15b7c958f95bd774f))
* **deps:** update docker/setup-buildx-action digest to 8d2750c ([3a4046c](https://github.com/lucas-dclrcq/homelab-manager/commit/3a4046cc6b371b74dd9af6837c1a521505c915d0))
* **deps:** update docker/setup-buildx-action digest to b5ca514 ([8a79488](https://github.com/lucas-dclrcq/homelab-manager/commit/8a79488513961d4db3d76fd71e806c19be02d33c))
* **deps:** update docker/setup-buildx-action digest to f7ce87c ([c7a2693](https://github.com/lucas-dclrcq/homelab-manager/commit/c7a2693c2552134463110585f2b3e742ca3d3dc6))
* **deps:** update docker/setup-qemu-action action to v4 ([bfad519](https://github.com/lucas-dclrcq/homelab-manager/commit/bfad5195a38f966eacc289cfecdbdb5389f1da04))
* **deps:** update docker/setup-qemu-action digest to 2910929 ([23a3b5f](https://github.com/lucas-dclrcq/homelab-manager/commit/23a3b5fd0bb1fbc2e7152ac0a918db1c4cf7f7e1))
* **deps:** update docker/setup-qemu-action digest to 4574d27 ([43ef139](https://github.com/lucas-dclrcq/homelab-manager/commit/43ef139120e9f74429a4fb83a46a708e91551293))
* **deps:** update docker/setup-qemu-action digest to c7c5346 ([e908c48](https://github.com/lucas-dclrcq/homelab-manager/commit/e908c48998c5f5951ef28e1cc504e712efe013c8))
* **deps:** update googleapis/release-please-action action to v5 ([350ce4d](https://github.com/lucas-dclrcq/homelab-manager/commit/350ce4d7189d0a22fdd4f1df3b7a9dc74b75cdb6))
* **deps:** update kotlin monorepo to v2.1.10 ([801416e](https://github.com/lucas-dclrcq/homelab-manager/commit/801416ef048554d232c30dd19eeed294f7a5836b))
* **deps:** update kotlin monorepo to v2.1.20 ([426ca14](https://github.com/lucas-dclrcq/homelab-manager/commit/426ca14399482ccf95d6323ec382c37ec04c6859))
* **deps:** update kotlin monorepo to v2.4.0 ([08152de](https://github.com/lucas-dclrcq/homelab-manager/commit/08152deab5b7647b09e7010d6198c3567c45eda5))
* **deps:** update node.js to v24 ([#119](https://github.com/lucas-dclrcq/homelab-manager/issues/119)) ([a762049](https://github.com/lucas-dclrcq/homelab-manager/commit/a762049f3489965d34077142e7762aacb96ad772))
* **deps:** update quay.io/quarkus/quarkus-micro-image docker tag to v3 ([902accf](https://github.com/lucas-dclrcq/homelab-manager/commit/902accf783a2894836a54690d88cd566b1fbeb60))
* **deps:** update registry.access.redhat.com/ubi8/openjdk-21 docker tag to v1.21-1.1739757870 ([6025611](https://github.com/lucas-dclrcq/homelab-manager/commit/60256113004eb33c9c13b3a3d3c4d198734f0842))
* **deps:** update registry.access.redhat.com/ubi8/openjdk-21 docker tag to v1.21-1.1741864871 ([d4474c7](https://github.com/lucas-dclrcq/homelab-manager/commit/d4474c7c28f5820bb8d81435a627675968045dcb))
* **deps:** update registry.access.redhat.com/ubi8/openjdk-21 docker tag to v1.21-1.1744796720 ([011fe4e](https://github.com/lucas-dclrcq/homelab-manager/commit/011fe4e267b708ca3a7ee186b652f953848d9224))
* **deps:** update registry.access.redhat.com/ubi8/openjdk-21 docker tag to v1.23-4.1782750871 ([056d5b2](https://github.com/lucas-dclrcq/homelab-manager/commit/056d5b226bb6c258c7040e2c28b3f2704363ab69))
* **deps:** update registry.access.redhat.com/ubi8/ubi-minimal docker tag to v8.10-1179.1739286367 ([63c4d85](https://github.com/lucas-dclrcq/homelab-manager/commit/63c4d85a8f04402c7282a08c42615ae39cf52197))
* **deps:** update registry.access.redhat.com/ubi8/ubi-minimal docker tag to v8.10-1216 ([56ba779](https://github.com/lucas-dclrcq/homelab-manager/commit/56ba779371aa4d9991254e5f365cc747bfee1e3d))
* **deps:** update registry.access.redhat.com/ubi8/ubi-minimal docker tag to v8.10-1255 ([dcfac43](https://github.com/lucas-dclrcq/homelab-manager/commit/dcfac4389f0d9d17e498dfca4382f0cd43779485))
* **deps:** update registry.access.redhat.com/ubi8/ubi-minimal docker tag to v8.10-1783573312 ([10eb66f](https://github.com/lucas-dclrcq/homelab-manager/commit/10eb66f22a46c5fe34fdf2757a1b2c3206846b7b))
* **deps:** update registry.access.redhat.com/ubi8/ubi-minimal docker tag to v8.10-1784076713 ([#115](https://github.com/lucas-dclrcq/homelab-manager/issues/115)) ([3c5e2a8](https://github.com/lucas-dclrcq/homelab-manager/commit/3c5e2a805251339b4b66cf112f54d6385f483712))
* **deps:** update surefire-plugin.version to v3.5.2 ([930374a](https://github.com/lucas-dclrcq/homelab-manager/commit/930374a645d5f941ad37b27843a05ac1152b7b75))
* **deps:** update surefire-plugin.version to v3.5.3 ([a479bd8](https://github.com/lucas-dclrcq/homelab-manager/commit/a479bd82fb2cf9111a224740406a3ccd5ef6e577))
* **deps:** update surefire-plugin.version to v3.5.6 ([cf082ab](https://github.com/lucas-dclrcq/homelab-manager/commit/cf082abf21ac64ce3a4f2ce585ebfb132107543c))
* **deps:** update tailwindcss monorepo to v4.3.3 ([#146](https://github.com/lucas-dclrcq/homelab-manager/issues/146)) ([a915ed1](https://github.com/lucas-dclrcq/homelab-manager/commit/a915ed1377d80b94a0b444324bf79a6f42c6924e))
* **docker:** use app user ([5144f89](https://github.com/lucas-dclrcq/homelab-manager/commit/5144f896775e69e0ba4262910f633123f6dc3687))
* enable json loggin ([3d0b4ef](https://github.com/lucas-dclrcq/homelab-manager/commit/3d0b4efd71255cef679ce9cdb1ef8024b25f2506))
* enable micrometer metrics ([ff3aed9](https://github.com/lucas-dclrcq/homelab-manager/commit/ff3aed967103f57f024ea1608cbc8636c59de4b0))
* initial commit ([3beac98](https://github.com/lucas-dclrcq/homelab-manager/commit/3beac980af72d084f4b56c585ec90a45bd3cc55c))
* log incoming requests body ([e075a65](https://github.com/lucas-dclrcq/homelab-manager/commit/e075a65097e56a958a619e2a52332d21490630db))
* **main:** release 0.1.0 ([1cf1aff](https://github.com/lucas-dclrcq/homelab-manager/commit/1cf1affc290ee9e6805b5b5f47fa708ff60ba99e))
* **main:** release 0.1.1 ([21d2d25](https://github.com/lucas-dclrcq/homelab-manager/commit/21d2d25b1ccd5b92cfae4dcb3b14bdade531c196))
* **main:** release 0.1.1-SNAPSHOT ([81a5138](https://github.com/lucas-dclrcq/homelab-manager/commit/81a5138c388fbe237892340b8fae73ddb33de9dd))
* **main:** release 0.1.2 ([2a67533](https://github.com/lucas-dclrcq/homelab-manager/commit/2a675333cb5c605c06f24475c5dcf9e567c7a568))
* **main:** release 0.1.2-SNAPSHOT ([b934711](https://github.com/lucas-dclrcq/homelab-manager/commit/b934711de818f7d1eb515e6bfb846c4383115199))
* **main:** release 0.1.3 ([af9d2d2](https://github.com/lucas-dclrcq/homelab-manager/commit/af9d2d20d6291ea1e1419efade672e0dfda850c7))
* **main:** release 0.1.3-SNAPSHOT ([d39790f](https://github.com/lucas-dclrcq/homelab-manager/commit/d39790f8763c35cf2b430e39c116076acda3c635))
* **main:** release 0.1.4 ([321b05a](https://github.com/lucas-dclrcq/homelab-manager/commit/321b05aa61744b4b3d87d93cb974d45f29cba694))
* **main:** release 0.1.4-SNAPSHOT ([4acc4fd](https://github.com/lucas-dclrcq/homelab-manager/commit/4acc4fdd9ae78a0827c8cddea2b35dff1697cd4c))
* **main:** release 0.1.5 ([f5145e4](https://github.com/lucas-dclrcq/homelab-manager/commit/f5145e441c0d998f5d8aadbe50d32a6634b5d247))
* **main:** release 0.1.5-SNAPSHOT ([a0efd33](https://github.com/lucas-dclrcq/homelab-manager/commit/a0efd33a68c89238c54419d76c97b14d50c8c9cb))
* **main:** release 0.1.6-SNAPSHOT ([fdf6f0d](https://github.com/lucas-dclrcq/homelab-manager/commit/fdf6f0d62e03d2e6317af91d764634e8ea5336cd))
* **main:** release 0.2.0 ([d047264](https://github.com/lucas-dclrcq/homelab-manager/commit/d0472640fbecbe9676015139b59efe665ff68cf5))
* **main:** release 0.2.1-SNAPSHOT ([d055910](https://github.com/lucas-dclrcq/homelab-manager/commit/d0559102eca4da41bdd1f8e30430d259ab899c7e))
* **main:** release 0.3.0 ([#120](https://github.com/lucas-dclrcq/homelab-manager/issues/120)) ([4cffce9](https://github.com/lucas-dclrcq/homelab-manager/commit/4cffce91e8f38aedcac5bf9abe2300b70c8be057))
* **main:** release 0.3.1 ([#131](https://github.com/lucas-dclrcq/homelab-manager/issues/131)) ([c97116a](https://github.com/lucas-dclrcq/homelab-manager/commit/c97116a6a2c763616fa7163856db36520b5af765))
* **main:** release 0.3.1-SNAPSHOT ([#124](https://github.com/lucas-dclrcq/homelab-manager/issues/124)) ([c943f30](https://github.com/lucas-dclrcq/homelab-manager/commit/c943f301908e2d2044fa90579c0cd58efcf2098d))
* **main:** release 0.3.2-SNAPSHOT ([#132](https://github.com/lucas-dclrcq/homelab-manager/issues/132)) ([5a4ef0d](https://github.com/lucas-dclrcq/homelab-manager/commit/5a4ef0dd542f14fe3cba0c03b37a673b989eb611))
* **main:** release 0.4.0 ([#133](https://github.com/lucas-dclrcq/homelab-manager/issues/133)) ([89ebf24](https://github.com/lucas-dclrcq/homelab-manager/commit/89ebf24bb8ec0e1706c0d3542eef05c6333dfe11))
* **main:** release 0.4.1-SNAPSHOT ([#145](https://github.com/lucas-dclrcq/homelab-manager/issues/145)) ([4acf5d3](https://github.com/lucas-dclrcq/homelab-manager/commit/4acf5d36bb803024ec94e45b3b2f860c2e9325e3))
* **main:** release 0.5.0 ([#147](https://github.com/lucas-dclrcq/homelab-manager/issues/147)) ([fccf34a](https://github.com/lucas-dclrcq/homelab-manager/commit/fccf34afcb4b323e8a4db98873b273348fa3cf92))
* **main:** release 0.5.1 ([#151](https://github.com/lucas-dclrcq/homelab-manager/issues/151)) ([a9b5f8a](https://github.com/lucas-dclrcq/homelab-manager/commit/a9b5f8a076db09c0bb2364aae3e5282f6b27706c))
* **main:** release 0.5.1-SNAPSHOT ([#150](https://github.com/lucas-dclrcq/homelab-manager/issues/150)) ([0974efc](https://github.com/lucas-dclrcq/homelab-manager/commit/0974efc6a5248b2efc3d8ee5cc29e953ca0c8c3a))
* **problems:** regenerate Orval client for desiredResolution field ([cf98466](https://github.com/lucas-dclrcq/homelab-manager/commit/cf9846640b47f5769576a10a2ebca721cc91979a))
* set logging level to WARN for trixnity client ([35f2218](https://github.com/lucas-dclrcq/homelab-manager/commit/35f2218df59904388c65017b9fbcc1665ae15c6a))
* upgrade quarkus to 3.26 ([40d6d7c](https://github.com/lucas-dclrcq/homelab-manager/commit/40d6d7c20a1f2f84b5d9c6ba50932c907d3aed02))

## [0.5.1](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.5.0...v0.5.1) (2026-07-24)


### Bug Fixes

* **docker:** image needs latest glibc for matrix crypto lib to work ([a488a42](https://github.com/lucas-dclrcq/homelab-manager/commit/a488a42667ef76733c77d8fc0d5b46945bb353f6))


### Chores

* **deps:** update dependency org.jacoco:jacoco-maven-plugin to v0.8.15 ([#125](https://github.com/lucas-dclrcq/homelab-manager/issues/125)) ([39411c2](https://github.com/lucas-dclrcq/homelab-manager/commit/39411c253cecc0f86345ac86e40c37d0c0084831))
* **deps:** update tailwindcss monorepo to v4.3.3 ([#146](https://github.com/lucas-dclrcq/homelab-manager/issues/146)) ([a915ed1](https://github.com/lucas-dclrcq/homelab-manager/commit/a915ed1377d80b94a0b444324bf79a6f42c6924e))
* **main:** release 0.5.1-SNAPSHOT ([#150](https://github.com/lucas-dclrcq/homelab-manager/issues/150)) ([0974efc](https://github.com/lucas-dclrcq/homelab-manager/commit/0974efc6a5248b2efc3d8ee5cc29e953ca0c8c3a))

## [0.5.0](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.4.0...v0.5.0) (2026-07-20)


### Features

* **statistics:** tabbed stats page with Jellystat-inspired categories ([#148](https://github.com/lucas-dclrcq/homelab-manager/issues/148)) ([1c5606c](https://github.com/lucas-dclrcq/homelab-manager/commit/1c5606c2a26f0bae5cd73d4d85749a3581ec40ac))


### Bug Fixes

* **deps:** update quarkus.platform.version to v3.37.3 ([#127](https://github.com/lucas-dclrcq/homelab-manager/issues/127)) ([85f0626](https://github.com/lucas-dclrcq/homelab-manager/commit/85f0626ccc9f2c33353879ab3a6648d1f234fb62))


### Chores

* **deps:** update actions/setup-java action to v5 ([#129](https://github.com/lucas-dclrcq/homelab-manager/issues/129)) ([2f8686c](https://github.com/lucas-dclrcq/homelab-manager/commit/2f8686c6d256820874f3d7d876d4f619a40d653e))
* **deps:** update dependency @vitejs/plugin-vue to v6 ([#102](https://github.com/lucas-dclrcq/homelab-manager/issues/102)) ([8fdc8ad](https://github.com/lucas-dclrcq/homelab-manager/commit/8fdc8adf35c37e91caca916e96a0071b0dd00519))
* **deps:** update node.js to v24 ([#119](https://github.com/lucas-dclrcq/homelab-manager/issues/119)) ([a762049](https://github.com/lucas-dclrcq/homelab-manager/commit/a762049f3489965d34077142e7762aacb96ad772))
* **deps:** update registry.access.redhat.com/ubi8/ubi-minimal docker tag to v8.10-1784076713 ([#115](https://github.com/lucas-dclrcq/homelab-manager/issues/115)) ([3c5e2a8](https://github.com/lucas-dclrcq/homelab-manager/commit/3c5e2a805251339b4b66cf112f54d6385f483712))
* **main:** release 0.4.1-SNAPSHOT ([#145](https://github.com/lucas-dclrcq/homelab-manager/issues/145)) ([4acf5d3](https://github.com/lucas-dclrcq/homelab-manager/commit/4acf5d36bb803024ec94e45b3b2f860c2e9325e3))

## [0.4.0](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.3.1...v0.4.0) (2026-07-19)


### Features

* add pagination on protection list ([0f62c66](https://github.com/lucas-dclrcq/homelab-manager/commit/0f62c66626eab80bd45aa8785829532f17a52b25))
* **problems:** better handle blocked imports ([b8d559b](https://github.com/lucas-dclrcq/homelab-manager/commit/b8d559b9cd191e6cd7b6f70e52a6f679b78bcfa9))


### Bug Fixes

* **deps:** update dependency pinia to v4 ([#130](https://github.com/lucas-dclrcq/homelab-manager/issues/130)) ([9b0a8cd](https://github.com/lucas-dclrcq/homelab-manager/commit/9b0a8cd20ef9e50d244f7b8f072ce7a8d770ff4c))


### Refactoring

* **operator:** use native http security policy instead of custom filter to protect operator routes ([403aa64](https://github.com/lucas-dclrcq/homelab-manager/commit/403aa64717ea8fee344d30060499af5c135ba626))


### Styles

* remove categories on side nav ([187d227](https://github.com/lucas-dclrcq/homelab-manager/commit/187d2276c4ad50f73d599da0b83444b7e4641f3a))
* vertically align logo with title ([b97ef9c](https://github.com/lucas-dclrcq/homelab-manager/commit/b97ef9c7fea2453509ca5e6a17707a60e90bf37f))


### Chores

* **main:** release 0.3.2-SNAPSHOT ([#132](https://github.com/lucas-dclrcq/homelab-manager/issues/132)) ([5a4ef0d](https://github.com/lucas-dclrcq/homelab-manager/commit/5a4ef0dd542f14fe3cba0c03b37a673b989eb611))

## [0.3.1](https://github.com/lucas-dclrcq/homelab-manager/compare/v0.3.0...v0.3.1) (2026-07-16)


### Bug Fixes

* another try at infinite loop because of cache control ([fc7b6a7](https://github.com/lucas-dclrcq/homelab-manager/commit/fc7b6a760c40d3241b9809fe93a07090556e5ae0))


### Refactoring

* group admin modules under unique admin page ([adbf08d](https://github.com/lucas-dclrcq/homelab-manager/commit/adbf08d3e50e240261378946e63af0089a83ff60))


### Chores

* **deps:** update dependency prettier to v3.9.5 ([#91](https://github.com/lucas-dclrcq/homelab-manager/issues/91)) ([96bdbf8](https://github.com/lucas-dclrcq/homelab-manager/commit/96bdbf896dca030e56ce61c37d2f244d2fab1f67))
* **deps:** update dependency typescript to ~5.9.0 ([#92](https://github.com/lucas-dclrcq/homelab-manager/issues/92)) ([a417a5f](https://github.com/lucas-dclrcq/homelab-manager/commit/a417a5f47716982fbf691c4009f9fa3ac4980af5))
* **main:** release 0.3.1-SNAPSHOT ([#124](https://github.com/lucas-dclrcq/homelab-manager/issues/124)) ([c943f30](https://github.com/lucas-dclrcq/homelab-manager/commit/c943f301908e2d2044fa90579c0cd58efcf2098d))

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
