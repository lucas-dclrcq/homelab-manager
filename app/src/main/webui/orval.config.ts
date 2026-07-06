import { defineConfig } from 'orval';

export default defineConfig({
  homelab: {
    /** OpenAPI document exported by the Quarkus build (quarkus.smallrye-openapi.store-schema-directory) */
    input: 'api/openapi.yaml',
    output: {
      /** Generates schema and client implementation in separate files */
      mode: 'split',
      /**  Path to the folder where models will be generated */
      schemas: 'src/api/model',
      /** Path to the file containing the client implementation */
      target: 'src/api/service/homelab.ts',
      /** Generates TanStack vue-query composables (useQuery/useMutation) */
      client: 'vue-query',
      override: {
        /** Same-origin axios instance: relative URLs so the OIDC session cookie applies */
        mutator: {
          path: 'src/api/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
    hooks: {
      /** Formats generated files with Prettier after generation */
      afterAllFilesWrite: 'prettier --write',
    },
  },
});
