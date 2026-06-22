// Tiny API client. The backend uses HTTP Basic Auth, so we keep the base64-encoded
// "username:password" token in localStorage and attach it on every request.

const STORAGE_KEY = 'locket.credentials'

export function saveCredentials(username, password) {
  const token = btoa(unescape(encodeURIComponent(`${username}:${password}`)))
  localStorage.setItem(STORAGE_KEY, token)
  return token
}

export function getToken() {
  return localStorage.getItem(STORAGE_KEY)
}

export function clearCredentials() {
  localStorage.removeItem(STORAGE_KEY)
}

function authHeaders() {
  const token = getToken()
  return token ? { Authorization: `Basic ${token}` } : {}
}

async function handle(res) {
  if (!res.ok) {
    let message = `HTTP ${res.status}`
    try {
      const body = await res.json()
      if (body && body.message) message = body.message
    } catch {
      // ignore non-JSON error bodies
    }
    const error = new Error(message)
    error.status = res.status
    throw error
  }
  const contentType = res.headers.get('content-type') || ''
  return contentType.includes('application/json') ? res.json() : res
}

export const api = {
  get: (path) =>
    fetch(`/api${path}`, { headers: { ...authHeaders() } }).then(handle),

  post: (path, body) =>
    fetch(`/api${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeaders() },
      body: JSON.stringify(body),
    }).then(handle),

  postForm: (path, formData) =>
    fetch(`/api${path}`, {
      method: 'POST',
      headers: { ...authHeaders() },
      body: formData,
    }).then(handle),

  // Images need the auth header too, so fetch them as a blob and turn into an object URL.
  blob: (path) =>
    fetch(`/api${path}`, { headers: { ...authHeaders() } }).then((res) => {
      if (!res.ok) throw new Error(`Image request failed (${res.status})`)
      return res.blob()
    }),
}
