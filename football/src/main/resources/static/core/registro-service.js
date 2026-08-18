const REGISTER_URL = '/api/usuarios';

export async function registerUser(values, { signal } = {}) {
  let response;

  try {
    response = await fetch(REGISTER_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        nombre: values.name.trim(),
        email: values.email.trim(),
        password: values.password
      }),
      signal
    });
  } catch (error) {
    if (error.name === 'AbortError') {
      throw error;
    }
    throw { type: 'network', message: 'No hemos podido conectar. Revisa tu conexión e inténtalo de nuevo.' };
  }

  if (response.ok) {
    return { id: (await response.json()).id };
  }

  let payload = {};
  try {
    payload = await response.json();
  } catch {
    payload = {};
  }

  if (payload.code === 'usuario.email-duplicado') {
    throw { type: 'email', message: 'Este email ya está registrado. Prueba a iniciar sesión.' };
  }

  if (response.status === 400) {
    throw { type: 'validation', message: 'Revisa los datos introducidos e inténtalo de nuevo.' };
  }

  throw { type: 'unknown', message: 'Ha ocurrido un problema. Inténtalo de nuevo en unos instantes.' };
}
