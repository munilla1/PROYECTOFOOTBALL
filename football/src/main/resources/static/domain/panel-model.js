const EMPTY_VALUE = 'No disponible';

export function adaptUser(user) {
  const membership = user.membresia || EMPTY_VALUE;
  const membershipActive = membership !== EMPTY_VALUE && !['INACTIVA', 'NONE', 'NINGUNA'].includes(String(membership).toUpperCase());

  return {
    name: user.nombre || EMPTY_VALUE,
    email: user.email || EMPTY_VALUE,
    membership,
    membershipActive,
    playerStatus: user.estadoJugador || EMPTY_VALUE,
    level: Number.isFinite(user.nivel) ? user.nivel : null,
    xp: Number.isFinite(user.xp) ? user.xp : null,
    energy: Number.isFinite(user.energia) ? user.energia : null
  };
}

export function membershipLabel(membership) {
  return String(membership)
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/^./, (letter) => letter.toUpperCase());
}

export function createPanelState() {
  return { status: 'idle', user: null, error: '', checkoutStatus: 'idle', checkoutError: '' };
}
