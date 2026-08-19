import { fetchProtected } from './session-store.js';
import { adaptUser } from '../domain/panel-model.js';

const CURRENT_USER_URL = '/api/usuarios/me';

export async function getCurrentUser() {
  let response;

  try {
    response = await fetchProtected(CURRENT_USER_URL);
  } catch (error) {
    if (error.message === 'Unauthenticated') throw error;
    throw { type: 'network', message: 'No hemos podido cargar tu perfil. Revisa tu conexion e intentalo de nuevo.' };
  }

  if (response.status === 401 || response.status === 403) {
    throw { type: 'unauthorized', message: 'Tu sesion ha expirado. Vuelve a iniciar sesion.' };
  }

  if (!response.ok) {
    throw { type: 'unknown', message: 'No hemos podido cargar tu perfil. Intentalo de nuevo.' };
  }

  let payload;
  try {
    payload = await response.json();
  } catch {
    throw { type: 'invalid-response', message: 'La respuesta del perfil no es valida.' };
  }

  if (!payload || typeof payload !== 'object') {
    throw { type: 'invalid-response', message: 'La respuesta del perfil no es valida.' };
  }

  return adaptUser(payload);
}
