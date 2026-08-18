import { test, expect } from '@playwright/test';

const validRegistration = {
  name: 'Ada Lovelace',
  email: 'ada@example.com',
  password: 'Secreta-123'
};

async function openRegistration(page) {
  await page.goto('/registro');
  await expect(page.locator('h2')).toHaveText('Entra al vestuario');
}

async function fillRegistration(page, values = validRegistration) {
  await page.getByLabel('Nombre').fill(values.name);
  await page.getByLabel('Email').fill(values.email);
  await page.getByLabel('Contraseña').fill(values.password);
}

async function mockRegistration(page, response, { delay = 0 } = {}) {
  let requestCount = 0;
  await page.route('**/api/usuarios', async (route) => {
    requestCount += 1;
    if (delay) {
      await new Promise((resolve) => setTimeout(resolve, delay));
    }
    await route.fulfill(response);
  });
  return () => requestCount;
}

test.describe('CHG-0101 - Pantalla de registro', () => {
  test('muestra los campos obligatorios, labels, contraseña protegida y navegación al login', async ({ page }) => {
    await openRegistration(page);

    await expect(page.getByLabel('Nombre')).toBeVisible();
    await expect(page.getByLabel('Email')).toBeVisible();
    await expect(page.getByLabel('Contraseña')).toHaveAttribute('type', 'password');
    await expect(page.getByRole('button', { name: 'Crear mi cuenta' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Inicia sesión' })).toHaveAttribute('href', '/login');
    await expect(page.getByLabel('Contraseña')).toHaveAttribute('aria-describedby', /password-help/);
  });

  test('valida formulario vacío y actualiza errores al corregir los campos', async ({ page }) => {
    await openRegistration(page);

    await page.getByRole('button', { name: 'Crear mi cuenta' }).click();
    await expect(page.getByText('Escribe tu nombre para continuar.')).toBeVisible();
    await expect(page.getByText('Escribe tu email para continuar.')).toBeVisible();
    await expect(page.getByText('Crea una contraseña para proteger tu cuenta.')).toBeVisible();
    await expect(page.getByLabel('Nombre')).toHaveAttribute('aria-invalid', 'true');
    await expect(page.getByLabel('Nombre')).toBeFocused();

    await fillRegistration(page);
    await expect(page.getByText('Escribe tu nombre para continuar.')).toBeHidden();
    await expect(page.getByLabel('Nombre')).toHaveAttribute('aria-invalid', 'false');
  });

  test('valida email inválido y contraseña insuficiente al perder el foco', async ({ page }) => {
    await openRegistration(page);

    await page.getByLabel('Nombre').fill('A');
    await page.getByLabel('Nombre').blur();
    await expect(page.getByText('El nombre debe tener al menos 2 caracteres.')).toBeVisible();

    await page.getByLabel('Email').fill('no-es-un-email');
    await page.getByLabel('Email').blur();
    await expect(page.getByText('Introduce un email válido.')).toBeVisible();

    await page.getByLabel('Contraseña').fill('corta');
    await page.getByLabel('Contraseña').blur();
    await expect(page.locator('#password-error')).toHaveText('Usa al menos 8 caracteres.');
  });

  test('envía una única petición, muestra carga y no expone la contraseña', async ({ page }) => {
    const requestCount = await mockRegistration(page, {
      status: 201,
      contentType: 'application/json',
      body: JSON.stringify({ id: 'user-1', nombre: validRegistration.name, email: validRegistration.email })
    }, { delay: 500 });
    const requests = [];
    page.on('request', (request) => {
      if (request.url().endsWith('/api/usuarios')) requests.push(request);
    });

    await openRegistration(page);
    await fillRegistration(page);
    await page.getByRole('button', { name: 'Crear mi cuenta' }).click();
    await expect(page.getByRole('button', { name: 'Creando cuenta…' })).toBeDisabled();
    await page.getByRole('button', { name: 'Creando cuenta…' }).click({ force: true });
    await expect(page.getByText('Cuenta creada. Te llevamos al inicio de sesión.')).toBeVisible();
    expect(requestCount()).toBe(1);
    expect(requests).toHaveLength(1);
    expect(requests[0].postDataJSON()).toEqual({
      nombre: validRegistration.name,
      email: validRegistration.email,
      password: validRegistration.password
    });
    expect(page.url()).not.toContain(encodeURIComponent(validRegistration.password));
  });

  test('confirma el registro y redirige a login sin crear sesión ni reenviar el formulario', async ({ page }) => {
    await mockRegistration(page, {
      status: 201,
      contentType: 'application/json',
      body: JSON.stringify({ id: 'user-2' })
    });
    await page.route('**/login', (route) => route.fulfill({
      status: 200,
      contentType: 'text/html',
      body: '<title>Login</title><main>Inicio de sesión</main>'
    }));

    await openRegistration(page);
    await fillRegistration(page);
    await page.getByRole('button', { name: 'Crear mi cuenta' }).click();
    await expect(page).toHaveURL(/\/login$/);
    expect(await page.evaluate(() => ({
      local: localStorage.length,
      session: sessionStorage.length,
      cookies: document.cookie
    }))).toEqual({ local: 0, session: 0, cookies: '' });
  });

  test('traduce email duplicado y permite corregir y reenviar', async ({ page }) => {
    let attempts = 0;
    await page.route('**/api/usuarios', async (route) => {
      attempts += 1;
      if (attempts === 1) {
        await route.fulfill({
          status: 409,
          contentType: 'application/json',
          body: JSON.stringify({ code: 'usuario.email-duplicado' })
        });
        return;
      }
      await route.fulfill({ status: 201, contentType: 'application/json', body: JSON.stringify({ id: 'user-3' }) });
    });

    await openRegistration(page);
    await fillRegistration(page);
    await page.getByRole('button', { name: 'Crear mi cuenta' }).click();
    await expect(page.getByText('Este email ya está registrado. Prueba a iniciar sesión.')).toBeVisible();
    await expect(page.getByLabel('Email')).toBeFocused();
    await page.getByLabel('Email').fill('nuevo@example.com');
    await page.getByRole('button', { name: 'Crear mi cuenta' }).click();
    await expect(page.getByText('Cuenta creada. Te llevamos al inicio de sesión.')).toBeVisible();
    expect(attempts).toBe(2);
  });

  test('gestiona validación backend y fallo de red conservando el formulario corregible', async ({ page }) => {
    await page.route('**/api/usuarios', (route) => route.fulfill({
      status: 400,
      contentType: 'application/json',
      body: JSON.stringify({ code: 'usuario.datos-invalidos' })
    }));
    await openRegistration(page);
    await fillRegistration(page);
    await page.getByRole('button', { name: 'Crear mi cuenta' }).click();
    await expect(page.getByText('Revisa los datos introducidos e inténtalo de nuevo.')).toBeVisible();
    await expect(page.getByLabel('Nombre')).toHaveValue(validRegistration.name);
    await expect(page.getByLabel('Contraseña')).toHaveValue(validRegistration.password);

    await page.unroute('**/api/usuarios');
    await page.route('**/api/usuarios', (route) => route.abort('failed'));
    await page.getByRole('button', { name: 'Crear mi cuenta' }).click();
    await expect(page.getByText('No hemos podido conectar. Revisa tu conexión e inténtalo de nuevo.')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Crear mi cuenta' })).toBeEnabled();
  });

  test('mantiene el formulario usable en móvil y permite recorrerlo con teclado', async ({ page }) => {
    await openRegistration(page);
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= document.documentElement.clientWidth)).toBe(true);
    const submitButton = page.getByRole('button', { name: 'Crear mi cuenta' });
    await submitButton.scrollIntoViewIfNeeded();
    await expect(submitButton).toBeInViewport();

    await page.getByLabel('Nombre').focus();
    await page.keyboard.press('Tab');
    await expect(page.getByLabel('Email')).toBeFocused();
    await page.keyboard.press('Tab');
    await expect(page.getByLabel('Contraseña')).toBeFocused();
    await page.keyboard.press('Tab');
    await expect(page.getByRole('button', { name: 'Crear mi cuenta' })).toBeFocused();
  });
});
