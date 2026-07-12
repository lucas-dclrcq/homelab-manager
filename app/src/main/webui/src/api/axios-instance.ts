import axios, { type AxiosRequestConfig } from 'axios'

/**
 * Same-origin instance: relative URLs, so the Quarkus OIDC session cookie is always sent.
 * The X-Requested-With header makes Quarkus answer XHR calls with a 499 status instead
 * of a 302 redirect to Keycloak (quarkus.oidc.authentication.java-script-auto-redirect=false).
 */
export const AXIOS_INSTANCE = axios.create({
  headers: { 'X-Requested-With': 'JavaScript' },
})

AXIOS_INSTANCE.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    if (status === 401 || status === 499) {
      window.location.reload()
      return new Promise(() => {})
    }
    return Promise.reject(error)
  },
)

export const customInstance = <T>(
  config: AxiosRequestConfig,
  options?: AxiosRequestConfig,
): Promise<T> =>
  AXIOS_INSTANCE({ ...config, ...options }).then(({ data }) => data)

export default customInstance
