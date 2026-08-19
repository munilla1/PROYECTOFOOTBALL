const SESSION_KEY = 'football.session.token';

export function getSessionToken() {
  return sessionStorage.getItem(SESSION_KEY);
}

export function saveSession(token) {
  if (typeof token !== 'string' || !token.trim()) {
    throw new Error('Invalid session token');
  }
  sessionStorage.setItem(SESSION_KEY, token.trim());
}

export function clearSession() {
  sessionStorage.removeItem(SESSION_KEY);
}

export function isAuthenticated() {
  return Boolean(getSessionToken());
}

export async function fetchProtected(url, options = {}) {
  const token = getSessionToken();
  if (!token) {
    window.location.assign('/login');
    throw new Error('Unauthenticated');
  }

  const headers = new Headers(options.headers || {});
  headers.set('Authorization', `Bearer ${token}`);
  const response = await fetch(url, { ...options, headers });

  if (response.status === 401 || response.status === 403) {
    clearSession();
    window.location.assign('/login');
  }

  return response;
}
