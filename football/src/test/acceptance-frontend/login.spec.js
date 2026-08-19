import { test, expect } from '@playwright/test';

const validLogin = {
  email: 'ada@example.com',
  password: 'Secreta-123'
};

async function openLogin(page) {
  await page.goto('/login');
  await expect(page.locator('h2')).toHaveText('Inicia sesion');
}

async function fillLogin(page, values = validLogin) {
  await page.getByLabel('Email').fill(values.email);
  await page.getByLabel('Contrasena').fill(values.password);
}

async function mockLogin(page, response, { delay = 0 } = {}) {
  let requestCount = 0;
  await page.route('**/api/sesiones/login', async (route) => {
    requestCount += 1;
    if (delay) {
      await new Promise((resolve) => setTimeout(resolve, delay));
    }
    await route.fulfill(response);
  });
  return () => requestCount;
}

test.describe('CHG-0102 - Pantalla de login', () => {
  test('muestra email, contrasena protegida, accion y enlace de registro', async ({ page }) => {
    await openLogin(page);

    await expect(page.getByLabel('Email')).toBeVisible();
    await expect(page.getByLabel('Contrasena')).toHaveAttribute('type', 'password');
    await expect(page.getByRole('button', { name: 'Entrar al vestuario' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Crea tu perfil' })).toHaveAttribute('href', '/registro');
    await expect(page.getByLabel('Email')).toHaveAttribute('aria-describedby', 'email-error');
    await expect(page.getByLabel('Contrasena')).toHaveAttribute('aria-describedby', 'password-error');
  });

  test('valida campos vacios e email invalido sin llamar al backend', async ({ page }) => {
    let requestCount = 0;
    await page.route('**/api/sesiones/login', (route) => {
      requestCount += 1;
      return route.continue();
    });

    await openLogin(page);
    await page.getByRole('button', { name: 'Entrar al vestuario' }).click();

    await expect(page.getByText('Escribe tu email para continuar.')).toBeVisible();
    await expect(page.getByText('Escribe tu contrasena para continuar.')).toBeVisible();
    await expect(page.getByLabel('Email')).toHaveAttribute('aria-invalid', 'true');
    await expect(page.getByLabel('Email')).toBeFocused();

    await page.getByLabel('Email').fill('no-es-un-email');
    await page.getByLabel('Email').blur();
    await expect(page.getByText('Introduce un email valido.')).toBeVisible();
    expect(requestCount).toBe(0);
  });

  test('envia una unica peticion, muestra carga y respeta el contrato de login', async ({ page }) => {
    const requestCount = await mockLogin(page, {
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ token: 'token-de-prueba' })
    }, { delay: 500 });
    const requests = [];
    page.on('request', (request) => {
      if (request.url().endsWith('/api/sesiones/login')) requests.push(request);
    });

    await page.route('**/panel', (route) => route.fulfill({
      status: 200,
      contentType: 'text/html',
      body: '<title>Panel</title><main>Panel de usuario</main>'
    }));

    await openLogin(page);
    await fillLogin(page);
    await page.getByRole('button', { name: 'Entrar al vestuario' }).click();
    await expect(page.getByRole('button', { name: 'Comprobando…' })).toBeDisabled();
    await page.getByRole('button', { name: 'Comprobando…' }).click({ force: true });

    await expect(page).toHaveURL(/\/panel$/);
    expect(requestCount()).toBe(1);
    expect(requests).toHaveLength(1);
    expect(requests[0].method()).toBe('POST');
    expect(requests[0].postDataJSON()).toEqual(validLogin);
    expect(await page.evaluate(() => sessionStorage.getItem('football.session.token'))).toBe('token-de-prueba');
    expect(page.url()).not.toContain(encodeURIComponent(validLogin.password));
  });

  test('rechaza credenciales invalidas y usuario inexistente sin crear sesion', async ({ page }) => {
    let attempts = 0;
    await page.route('**/api/sesiones/login', async (route) => {
      attempts += 1;
      await route.fulfill({
        status: attempts === 1 ? 401 : 404,
        contentType: 'application/json',
        body: JSON.stringify({ code: attempts === 1 ? 'sesion.credenciales-invalidas' : 'usuario.no-existe' })
      });
    });

    await openLogin(page);
    await fillLogin(page);
    await page.getByRole('button', { name: 'Entrar al vestuario' }).click();
    await expect(page.getByText('El email o la contrasena no son correctos.')).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
    expect(await page.evaluate(() => sessionStorage.length)).toBe(0);

    await page.getByLabel('Email').fill('nuevo@example.com');
    await page.getByRole('button', { name: 'Entrar al vestuario' }).click();
    await expect(page.getByText('El email o la contrasena no son correctos.')).toBeVisible();
    expect(attempts).toBe(2);
    expect(await page.evaluate(() => sessionStorage.length)).toBe(0);
  });

  test('gestiona respuesta invalida, fallo de red y permite reintentar', async ({ page }) => {
    let attempts = 0;
    await page.route('**/api/sesiones/login', async (route) => {
      attempts += 1;
      if (attempts === 1) {
        await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({}) });
        return;
      }
      await route.abort('failed');
    });

    await openLogin(page);
    await fillLogin(page);
    await page.getByRole('button', { name: 'Entrar al vestuario' }).click();
    await expect(page.getByText('No hemos podido iniciar la sesion. Intentalo de nuevo.')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Entrar al vestuario' })).toBeEnabled();

    await page.getByRole('button', { name: 'Entrar al vestuario' }).click();
    await expect(page.getByText('No hemos podido conectar. Revisa tu conexion e intentalo de nuevo.')).toBeVisible();
    expect(attempts).toBe(2);
    expect(await page.evaluate(() => sessionStorage.length)).toBe(0);
  });

  test('limpia un token invalido y vuelve a login al acceder a una solicitud protegida', async ({ page }) => {
    await page.route('**/api/protegido', (route) => route.fulfill({ status: 401, body: '{}' }));
    await openLogin(page);
    await page.evaluate(() => sessionStorage.setItem('football.session.token', 'token-expirado'));

    await page.evaluate(async () => {
      const { fetchProtected } = await import('/core/session-store.js');
      await fetchProtected('/api/protegido');
    });

    await expect(page).toHaveURL(/\/login$/);
    expect(await page.evaluate(() => sessionStorage.length)).toBe(0);
  });

  test('mantiene la pantalla usable con teclado y en movil', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await openLogin(page);

    expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true);
    await page.getByLabel('Email').focus();
    await page.keyboard.press('Tab');
    await expect(page.getByLabel('Contrasena')).toBeFocused();
    await page.keyboard.press('Tab');
    await expect(page.getByRole('button', { name: 'Entrar al vestuario' })).toBeFocused();
    await expect(page.locator('#form-status')).toHaveAttribute('aria-live', 'polite');
  });
});
