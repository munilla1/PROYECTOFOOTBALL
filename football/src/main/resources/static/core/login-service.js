const LOGIN_URL = '/api/sesiones/login';

export async function loginUser(values, { signal } = {}) {
  let response;

  try {
    response = await fetch(LOGIN_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: values.email.trim(), password: values.password }),
      signal
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw { type: 'network', message: 'No hemos podido conectar. Revisa tu conexion e intentalo de nuevo.' };
  }

  let payload = {};
  try {
    payload = await response.json();
  } catch {
    payload = {};
  }

  if (response.ok && typeof payload.token === 'string' && payload.token.trim()) {
    return { token: payload.token.trim() };
  }

  if (response.status === 401 || payload.code === 'sesion.credenciales-invalidas') {
    throw { type: 'credentials', message: 'El email o la contrasena no son correctos.' };
  }

  if (response.status === 404 || payload.code === 'usuario.no-existe') {
    throw { type: 'credentials', message: 'El email o la contrasena no son correctos.' };
  }

  if (response.ok) {
    throw { type: 'invalid-response', message: 'No hemos podido iniciar la sesion. Intentalo de nuevo.' };
  }

  throw { type: 'unknown', message: 'Ha ocurrido un problema. Intentalo de nuevo en unos instantes.' };
}
