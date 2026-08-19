import { loginUser } from '../core/login-service.js';
import { saveSession } from '../core/session-store.js';
import { createLoginState, validateLogin } from '../domain/login-model.js';

const form = document.querySelector('#login-form');
const fields = {
  email: document.querySelector('#email'),
  password: document.querySelector('#password')
};
const errors = {
  email: document.querySelector('#email-error'),
  password: document.querySelector('#password-error')
};
const status = document.querySelector('#form-status');
const submitButton = document.querySelector('#submit-button');
const submitLabel = document.querySelector('#submit-label');
let state = createLoginState();

function values() {
  return { email: fields.email.value, password: fields.password.value };
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
  submitLabel.textContent = isLoading ? 'Comprobando…' : 'Entrar al vestuario';
}

function validate() {
  state = { ...state, errors: validateLogin(values()) };
  renderErrors(state.errors);
  return Object.keys(state.errors).length === 0;
}

Object.entries(fields).forEach(([key, field]) => {
  field.addEventListener('blur', validate);
  field.addEventListener('input', () => {
    const nextErrors = validateLogin(values());
    if (state.errors[key] && !nextErrors[key]) {
      state = { ...state, errors: nextErrors };
      renderErrors(nextErrors);
    }
    if (state.status === 'error') {
      state = { ...state, status: 'idle' };
      setStatus('');
    }
  });
});

// ⭐ AQUÍ ESTÁ LA INTEGRACIÓN CORRECTA
form.addEventListener('submit', async (event) => {
  event.preventDefault(); // evita el submit automático del navegador

  if (state.status === 'loading' || !validate()) {
    fields[Object.keys(state.errors)[0]]?.focus();
    return;
  }

  state = { ...state, status: 'loading' };
  setLoading(true);
  setStatus('Iniciando sesion…');

  try {
    const session = await loginUser(values());
    saveSession(session.token);
    fields.password.value = '';
    state = { ...state, status: 'success' };
    setStatus('Sesion iniciada. Te llevamos a tu panel.', 'success');
    window.location.assign('/panel');
  } catch (error) {
    if (error.name === 'AbortError') return;
    state = { ...state, status: 'error' };
    setLoading(false);
    setStatus(error.message || 'No hemos podido iniciar sesion. Intentalo de nuevo.');
    if (error.type === 'credentials' || error.type === 'network') {
      fields.email.focus();
    }
  }
});
