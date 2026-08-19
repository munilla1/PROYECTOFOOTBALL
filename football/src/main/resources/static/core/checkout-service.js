import { fetchProtected } from './session-store.js';

const CHECKOUT_URL = '/api/membresias/checkout';

export async function createCheckoutSession() {
  let response;

  try {
    response = await fetchProtected(CHECKOUT_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({})
    });
  } catch (error) {
    if (error.message === 'Unauthenticated') throw error;
    throw { type: 'network', message: 'No hemos podido conectar con el pago. Intentalo de nuevo.' };
  }

  if (response.status === 401 || response.status === 403) {
    throw { type: 'unauthorized', message: 'Tu sesion ha expirado. Vuelve a iniciar sesion.' };
  }

  let payload = {};
  try {
    payload = await response.json();
  } catch {
    payload = {};
  }

  if (response.ok && typeof payload.url === 'string' && payload.url.trim()) {
    return { url: payload.url.trim() };
  }

  throw { type: 'checkout', message: 'No hemos podido preparar el pago. Intentalo de nuevo.' };
}
