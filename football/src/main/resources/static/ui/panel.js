import { createCheckoutSession } from '../core/checkout-service.js';
import { clearSession, getSessionToken } from '../core/session-store.js';
import { getCurrentUser } from '../core/user-service.js';
import { createPanelState, membershipLabel } from '../domain/panel-model.js';

const profileContent = document.querySelector('#profile-content');
const status = document.querySelector('#panel-status');
const retryButton = document.querySelector('#retry-button');
const logoutButton = document.querySelector('#logout-button');
const checkoutButton = document.querySelector('#checkout-button');
const checkoutLabel = document.querySelector('#checkout-label');
const checkoutStatus = document.querySelector('#checkout-status');
let state = createPanelState();

function setPanelStatus(message, kind = '') {
  status.textContent = message;
  status.className = `panel-status ${kind}`.trim();
}

function setProfile(user) {
  document.querySelector('#user-name').textContent = user.name;
  document.querySelector('#user-email').textContent = user.email;
  document.querySelector('#membership-name').textContent = membershipLabel(user.membership);
  document.querySelector('#membership-status').textContent = user.membershipActive ? 'Membresia activa' : 'Sin membresia activa';
  document.querySelector('#player-status').textContent = user.playerStatus;
  document.querySelector('#user-level').textContent = user.level ?? '-';
  document.querySelector('#user-xp').textContent = user.xp ?? '-';
  document.querySelector('#user-energy').textContent = user.energy ?? '-';
  checkoutButton.hidden = user.membershipActive;
}

function setLoading(isLoading) {
  retryButton.hidden = true;
  logoutButton.disabled = isLoading;
  if (isLoading) {
    profileContent.hidden = true;
    setPanelStatus('Cargando tu perfil…');
  }
}

async function loadPanel() {
  if (!getSessionToken()) {
    window.location.assign('/login');
    return;
  }

  state = { ...state, status: 'loading', error: '' };
  setLoading(true);

  try {
    const user = await getCurrentUser();
    state = { ...state, status: 'success', user };
    setLoading(false);
    setProfile(user);
    profileContent.hidden = false;
    setPanelStatus('Perfil actualizado.', 'success');
  } catch (error) {
    if (error.message === 'Unauthenticated' || error.type === 'unauthorized') return;
    state = { ...state, status: 'error', error: error.message };
    setLoading(false);
    setPanelStatus(error.message || 'No hemos podido cargar tu perfil. Intentalo de nuevo.');
    retryButton.hidden = false;
  }
}

checkoutButton.addEventListener('click', async () => {
  if (state.checkoutStatus === 'loading' || !state.user || state.user.membershipActive) return;

  state = { ...state, checkoutStatus: 'loading', checkoutError: '' };
  checkoutButton.disabled = true;
  checkoutLabel.textContent = 'Preparando pago…';
  checkoutStatus.textContent = 'Conectando con Checkout…';

  try {
    const checkout = await createCheckoutSession();
    window.location.assign(checkout.url);
  } catch (error) {
    if (error.message === 'Unauthenticated' || error.type === 'unauthorized') return;
    state = { ...state, checkoutStatus: 'error', checkoutError: error.message };
    checkoutButton.disabled = false;
    checkoutLabel.textContent = 'Activar membresia';
    checkoutStatus.textContent = error.message || 'No hemos podido preparar el pago.';
  }
});

retryButton.addEventListener('click', loadPanel);
logoutButton.addEventListener('click', () => {
  clearSession();
  window.location.assign('/login');
});

loadPanel();
