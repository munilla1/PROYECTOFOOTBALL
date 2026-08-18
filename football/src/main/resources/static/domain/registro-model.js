const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateRegistration(values) {
  const errors = {};
  const name = values.name.trim();
  const email = values.email.trim();
  const password = values.password;

  if (!name) {
    errors.name = 'Escribe tu nombre para continuar.';
  } else if (name.length < 2) {
    errors.name = 'El nombre debe tener al menos 2 caracteres.';
  }

  if (!email) {
    errors.email = 'Escribe tu email para continuar.';
  } else if (!EMAIL_PATTERN.test(email)) {
    errors.email = 'Introduce un email válido.';
  }

  if (!password) {
    errors.password = 'Crea una contraseña para proteger tu cuenta.';
  } else if (password.length < 8) {
    errors.password = 'Usa al menos 8 caracteres.';
  }

  return errors;
}

export function createRegistrationState() {
  return {
    status: 'idle',
    errors: {},
    message: ''
  };
}
