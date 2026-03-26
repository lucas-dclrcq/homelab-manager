import { defineConfig } from 'orval';

export default defineConfig({
  catalog: {
    /** The path of the generated OpenAPI scheme */
    input: 'api/catalog.yaml',
    output: {
      /** Generates schema and client implementation in separate files */
      mode: 'split',
      /**  Path to the folder where models will be generated */
      schemas: 'src/api/model',
      /** Path to the file containing the client implementation */
      target: 'src/api/service/catalog.ts',
      /** Type of client implementation */
      client: 'axios',
      /** Base URL of the API, included in the generated client */
      baseUrl: 'http://localhost:8080'
    },
    hooks: {
      /** Formats generated files with Prettier after generation */
      afterAllFilesWrite: 'prettier --write',
    },
  },
});