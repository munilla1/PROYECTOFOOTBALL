import { test, expect } from '@playwright/test';

const sessionToken = 'token-panel-de-prueba';
const activeUser = {
  id: 'user-103',
  nombre: 'Ada Lovelace',
  email: 'ada@example.com',
  membresia: 'PREMIUM',
  estadoJugador: 'NORMAL',
  nivel: 12,
  xp: 840,
  energia: 76,
  campoNuevo: 'ignorado'
};

const inactiveUser = {
  ...activeUser,
  membresia: 'NONE',
  estadoJugador: 'LESIONADO',
  nivel: null,
  xp: null,
  energia: null
};

async function openPanel(page, token = sessionToken) {
  if (token) {
    await page.addInitScript((value) => {
      if (window.location.pathname === '/panel') {
        sessionStorage.setItem('football.session.token', value);
      }
    }, token);
  }
  await page.goto('/panel');
  await expect(page.locator('#dashboard-title')).toHaveText('Tu temporada, de un vistazo.');
}

async function mockCurrentUser(page, response, { delay = 0 } = {}) {
  let requestCount = 0;
  const requests = [];
  await page.route('**/api/usuarios/me', async (route) => {
    requestCount += 1;
    requests.push(route.request());
    if (delay) await new Promise((resolve) => setTimeout(resolve, delay));
    await route.fulfill(response);
  });
  return { requestCount: () => requestCount, requests };
}

function jsonResponse(body, status = 200) {
  return { status, contentType: 'application/json', body: JSON.stringify(body) };
}

test.describe('CHG-0103 - Panel de usuario', () => {
  test('redirige al login cuando no existe una sesion local', async ({ page }) => {
    await page.goto('/panel');

    await expect(page).toHaveURL(/\/login$/);
    expect(await page.evaluate(() => sessionStorage.length)).toBe(0);
  });

  test('carga el perfil con el contrato protegido y muestra sus datos', async ({ page }) => {
    const mock = await mockCurrentUser(page, jsonResponse(activeUser));
    await openPanel(page);

    await expect(page.locator('#profile-content')).toBeVisible();
    await expect(page.locator('#user-name')).toHaveText('Ada Lovelace');
    await expect(page.locator('#user-email')).toHaveText('ada@example.com');
    await expect(page.locator('#membership-name')).toHaveText('Premium');
    await expect(page.locator('#membership-status')).toHaveText('Membresia activa');
    await expect(page.locator('#player-status')).toHaveText('NORMAL');
    await expect(page.locator('#user-level')).toHaveText('12');
    await expect(page.locator('#user-xp')).toHaveText('840');
    await expect(page.locator('#user-energy')).toHaveText('76');
    await expect(page.locator('#checkout-button')).toBeHidden();

    expect(mock.requestCount()).toBe(1);
    expect(mock.requests[0].method()).toBe('GET');
    expect(mock.requests[0].headers().authorization).toBe(`Bearer ${sessionToken}`);
    expect(await page.locator('body').innerText()).not.toContain(sessionToken);
    expect(await page.locator('body').innerText()).not.toContain('ignorado');
  });

  test('muestra valores neutros para datos opcionales ausentes', async ({ page }) => {
    await mockCurrentUser(page, jsonResponse({ email: null, membresia: null, estadoJugador: null }));
    await openPanel(page);

    await expect(page.locator('#user-name')).toHaveText('No disponible');
    await expect(page.locator('#user-email')).toHaveText('No disponible');
    await expect(page.locator('#membership-name')).toHaveText('No disponible');
    await expect(page.locator('#player-status')).toHaveText('No disponible');
    await expect(page.locator('#user-level')).toHaveText('-');
    await expect(page.locator('#user-xp')).toHaveText('-');
    await expect(page.locator('#user-energy')).toHaveText('-');
    expect(await page.locator('body').innerText()).not.toMatch(/undefined|null/);
    await expect(page.locator('#checkout-button')).toBeVisible();
  });

  test('comunica la carga, conserva el error recuperable y permite reintentar', async ({ page }) => {
    let attempts = 0;
    let releaseRequest;
    const requestPaused = new Promise((resolve) => {
      releaseRequest = resolve;
    });
    await page.route('**/api/usuarios/me', async (route) => {
      attempts += 1;
      if (attempts === 1) await requestPaused;
      if (attempts === 1) {
        await route.abort('failed');
        return;
      }
      await route.fulfill(jsonResponse(activeUser));
    });

    await page.addInitScript((value) => {
      sessionStorage.setItem('football.session.token', value);
    }, sessionToken);
    const responsePromise = page.waitForRequest('**/api/usuarios/me');
    await page.goto('/panel');
    await expect(page.locator('#panel-status')).toHaveText('Cargando tu perfil…');
    releaseRequest();
    await responsePromise;
    await expect(page.locator('#panel-status')).toHaveText('No hemos podido cargar tu perfil. Revisa tu conexion e intentalo de nuevo.');
    await expect(page.getByRole('button', { name: 'Reintentar carga' })).toBeVisible();

    await page.getByRole('button', { name: 'Reintentar carga' }).click();
    await expect(page.locator('#panel-status')).toHaveText('Perfil actualizado.');
    await expect(page.locator('#user-name')).toHaveText('Ada Lovelace');
    expect(attempts).toBe(2);
  });

  test('limpia la sesion y vuelve al login cuando el backend rechaza el token', async ({ page }) => {
    await mockCurrentUser(page, jsonResponse({ code: 'sesion.no-autorizado' }, 401));
    await page.addInitScript((value) => {
      if (window.location.pathname === '/panel') {
        sessionStorage.setItem('football.session.token', value);
      }
    }, sessionToken);
    await page.goto('/panel');

    await expect(page).toHaveURL(/\/login$/);
    expect(await page.evaluate(() => sessionStorage.length)).toBe(0);
  });

  test('ofrece Checkout para membresia inactiva y redirige con una peticion protegida', async ({ page }) => {
    await mockCurrentUser(page, jsonResponse(inactiveUser));
    let checkoutRequest;
    await page.route('**/api/membresias/checkout', async (route) => {
      checkoutRequest = route.request();
      await route.fulfill(jsonResponse({ url: '/checkout/stripe-session' }));
    });
    await page.route('**/checkout/stripe-session', (route) => route.fulfill({
      status: 200,
      contentType: 'text/html',
      body: '<title>Stripe Checkout</title><main>Checkout</main>'
    }));

    await openPanel(page);
    await expect(page.locator('#membership-status')).toHaveText('Sin membresia activa');
    await expect(page.getByRole('button', { name: 'Activar membresia' })).toBeVisible();
    await page.getByRole('button', { name: 'Activar membresia' }).click();
    await expect(page).toHaveURL(/\/checkout\/stripe-session$/);

    expect(checkoutRequest.method()).toBe('POST');
    expect(checkoutRequest.headers().authorization).toBe(`Bearer ${sessionToken}`);
    expect(checkoutRequest.postDataJSON()).toEqual({});
    expect(checkoutRequest.postData()).not.toMatch(/card|cvv|cvc|number/i);
  });

  test('muestra un error recuperable de Checkout sin perder el perfil', async ({ page }) => {
    await mockCurrentUser(page, jsonResponse(inactiveUser));
    await page.route('**/api/membresias/checkout', (route) => route.fulfill(jsonResponse({ code: 'checkout.no-disponible' }, 500)));

    await openPanel(page);
    await page.getByRole('button', { name: 'Activar membresia' }).click();
    await expect(page.locator('#checkout-status')).toHaveText('No hemos podido preparar el pago. Intentalo de nuevo.');
    await expect(page.locator('#user-name')).toHaveText('Ada Lovelace');
    await expect(page.getByRole('button', { name: 'Activar membresia' })).toBeEnabled();
  });

  test('expone los cuatro accesos del juego con sus rutas definidas', async ({ page }) => {
    await mockCurrentUser(page, jsonResponse(activeUser));
    await openPanel(page);

    const destinations = [
      ['Estadisticas reales', '/estadisticas'],
      ['Jornadas', '/jornadas'],
      ['Fichajes', '/fichajes'],
      ['Progresion', '/progresion']
    ];

    for (const [name, href] of destinations) {
      await expect(page.getByRole('link', { name: new RegExp(name) })).toHaveAttribute('href', href);
    }

    await page.route('**/estadisticas', (route) => route.fulfill({ status: 200, body: '<main>Estadisticas</main>' }));
    await page.getByRole('link', { name: /Estadisticas reales/ }).click();
    await expect(page).toHaveURL(/\/estadisticas$/);
  });

  test('cierra la sesion, limpia el estado local y bloquea el acceso posterior', async ({ page }) => {
    await mockCurrentUser(page, jsonResponse(activeUser));
    await openPanel(page);
    await expect(page.locator('#profile-content')).toBeVisible();
    await page.route('**/login', (route) => route.fulfill({ status: 200, contentType: 'text/html', body: '<main>Login</main>' }));

    await page.getByRole('button', { name: 'Cerrar sesion' }).click();
    await expect(page).toHaveURL(/\/login$/);
    expect(await page.evaluate(() => sessionStorage.length)).toBe(0);

    await page.reload();
    await expect(page).toHaveURL(/\/login$/);
  });

  test('mantiene accesibilidad y usabilidad en movil', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await mockCurrentUser(page, jsonResponse(inactiveUser));
    await openPanel(page);
    await expect(page.locator('#profile-content')).toBeVisible();

    expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true);
    await expect(page.locator('#panel-status')).toHaveAttribute('aria-live', 'polite');
    await expect(page.locator('#checkout-status')).toHaveAttribute('aria-live', 'polite');
    await expect(page.getByRole('button', { name: 'Cerrar sesion' })).toBeVisible();

    await page.getByRole('button', { name: 'Cerrar sesion' }).focus();
    await page.keyboard.press('Tab');
    await expect(page.getByRole('button', { name: 'Activar membresia' })).toBeFocused();
    await page.keyboard.press('Tab');
    await expect(page.getByRole('link', { name: /Estadisticas reales/ })).toBeFocused();
    await page.keyboard.press('Tab');
    await expect(page.getByRole('link', { name: /Jornadas/ })).toBeFocused();
  });
});
