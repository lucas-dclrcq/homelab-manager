import axios, { type AxiosRequestConfig } from 'axios'

/**
 * Same-origin instance: relative URLs, so the Quarkus OIDC session cookie is always sent.
 * The X-Requested-With header makes Quarkus answer XHR calls with a 499 status instead
 * of a 302 redirect to Keycloak (quarkus.oidc.authentication.java-script-auto-redirect=false).
 */
export const AXIOS_INSTANCE = axios.create({
  headers: { 'X-Requested-With': 'JavaScript' },
})

// Guard against a reload storm: if reloading does not restore the session (e.g.
// the server keeps answering 401 without redirecting to the IdP), reloading again
// immediately would pin the CPU. Allow at most one auto-reload per interval; any
// successful response means we are authenticated again and clears the guard.
const RELOAD_GUARD_KEY = 'auth-reload-at'
const RELOAD_MIN_INTERVAL_MS = 10_000

AXIOS_INSTANCE.interceptors.response.use(
  (response) => {
    sessionStorage.removeItem(RELOAD_GUARD_KEY)
    return response
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401 || status === 499) {
      const lastReloadAt = Number(sessionStorage.getItem(RELOAD_GUARD_KEY) ?? 0)
      if (Date.now() - lastReloadAt > RELOAD_MIN_INTERVAL_MS) {
        sessionStorage.setItem(RELOAD_GUARD_KEY, String(Date.now()))
        window.location.reload()
      }
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
