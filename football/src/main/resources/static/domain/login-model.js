const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateLogin(values) {
  const errors = {};
  const email = values.email.trim();

  if (!email) {
    errors.email = 'Escribe tu email para continuar.';
  } else if (!EMAIL_PATTERN.test(email)) {
    errors.email = 'Introduce un email valido.';
  }

  if (!values.password) {
    errors.password = 'Escribe tu contrasena para continuar.';
  }

  return errors;
}

export function createLoginState() {
  return { status: 'idle', errors: {} };
}
