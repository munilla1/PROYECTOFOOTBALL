import { registerUser } from '../core/registro-service.js';
import { createRegistrationState, validateRegistration } from '../domain/registro-model.js';

const form = document.querySelector('#register-form');
const fields = {
  name: document.querySelector('#name'),
  email: document.querySelector('#email'),
  password: document.querySelector('#password')
};
const errors = {
  name: document.querySelector('#name-error'),
  email: document.querySelector('#email-error'),
  password: document.querySelector('#password-error')
};
const status = document.querySelector('#form-status');
const submitButton = document.querySelector('#submit-button');
const submitLabel = document.querySelector('#submit-label');
let state = createRegistrationState();

function values() {
  return Object.fromEntries(Object.entries(fields).map(([key, field]) => [key, field.value]));
}

function renderErrors(nextErrors) {
  Object.entries(fields).forEach(([key, field]) => {
    const message = nextErrors[key] || '';
    field.setAttribute('aria-invalid', String(Boolean(message)));
    errors[key].textContent = message;
  });
}

function setStatus(message, kind = '') {
  status.textContent = message;
  status.className = `form-status ${kind}`.trim();
}

function setLoading(isLoading) {
  submitButton.disabled = isLoading;
  Object.values(fields).forEach((field) => { field.disabled = isLoading; });
  submitLabel.textContent = isLoading ? 'Creando cuenta…' : 'Crear mi cuenta';
}

function validate() {
  state = { ...state, errors: validateRegistration(values()) };
  renderErrors(state.errors);
  return Object.keys(state.errors).length === 0;
}

Object.entries(fields).forEach(([key, field]) => {
  field.addEventListener('blur', validate);
  field.addEventListener('input', () => {
    const nextErrors = validateRegistration(values());
    if (state.errors[key] && !nextErrors[key]) {
      state = { ...state, errors: nextErrors };
      renderErrors(nextErrors);
    }
    if (state.status === 'error') {
      setStatus('');
    }
  });
});

form.addEventListener('submit', async (event) => {
  event.preventDefault();
  if (state.status === 'loading' || !validate()) {
    const firstError = Object.keys(state.errors)[0];
    fields[firstError]?.focus();
    return;
  }

  state = { ...state, status: 'loading' };
  setLoading(true);
  setStatus('Guardando tus datos…');

  try {
    await registerUser(values());
    fields.password.value = '';
    state = { ...state, status: 'success' };
    setStatus('Cuenta creada. Te llevamos al inicio de sesión.', 'success');
    window.setTimeout(() => { window.location.assign('/login'); }, 900);
  } catch (error) {
    if (error.name === 'AbortError') return;
    state = { ...state, status: 'error' };
    setLoading(false);
    setStatus(error.message || 'No hemos podido crear la cuenta. Inténtalo de nuevo.');
    if (error.type === 'email') {
      fields.email.focus();
    }
  }
});
