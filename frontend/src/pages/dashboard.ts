/**
 * Controlador de `dashboard.html`.
 *
 * - Verifica la sesión JWT contra `/auth/me` y permite cerrar sesión.
 * - Resuelve el paciente del usuario (o lo busca si es ADMIN/RECEPCION).
 * - Reserva turnos consultando disponibilidad y confirma en un modal.
 * - Lista y cancela los turnos del paciente con refresco inmediato.
 */

import { me } from '../api/auth.js';
import { listarOdontologosPublicos } from '../api/odontologos.js';
import { buscarPacientes, obtenerPacienteActual } from '../api/pacientes.js';
import {
  cancelarTurno,
  crearTurno,
  listarTurnos,
  obtenerDisponibilidad,
} from '../api/turnos.js';
import { isApiError } from '../models/index.js';
import type {
  Disponibilidad,
  EstadoTurno,
  OdontologoPublico,
  Paciente,
  RolUsuario,
  SlotHorario,
  Turno,
  UserMeResponse,
} from '../models/index.js';
import { getToken, removeToken } from '../utils/jwt.js';
import {
  describeError,
  hideAlert,
  requireElement,
  setButtonBusy,
  showAlert,
} from '../utils/dom.js';
import type { AlertVariant } from '../utils/dom.js';

interface PacienteContexto {
  id: number;
  nombreCompleto: string;
}

interface DashboardState {
  usuario: UserMeResponse | null;
  paciente: PacienteContexto | null;
  odontologos: OdontologoPublico[];
  slotSeleccionado: SlotHorario | null;
  odontologoSeleccionado: OdontologoPublico | null;
  turnos: Turno[];
  turnoACancelar: Turno | null;
}

const state: DashboardState = {
  usuario: null,
  paciente: null,
  odontologos: [],
  slotSeleccionado: null,
  odontologoSeleccionado: null,
  turnos: [],
  turnoACancelar: null,
};

const ROLE_LABELS: Record<RolUsuario, string> = {
  ADMIN: 'Administrador',
  RECEPCION: 'Recepción',
  ODONTOLOGO: 'Odontólogo',
  PACIENTE: 'Paciente',
};

const ESTADO_LABELS: Record<EstadoTurno, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADO: 'Confirmado',
  ATENDIDO: 'Atendido',
  CANCELADO: 'Cancelado',
};

const ESTADO_CLASES: Record<EstadoTurno, string> = {
  PENDIENTE: 'badge--warning',
  CONFIRMADO: 'badge--success',
  ATENDIDO: 'badge--info',
  CANCELADO: 'badge--danger',
};

/* --------------------------------------------------------------------------
   Referencias del DOM
   -------------------------------------------------------------------------- */

const globalAlert = requireElement<HTMLDivElement>('global-alert');
const sessionNotice = requireElement<HTMLDivElement>('session-notice');
const userName = requireElement<HTMLElement>('user-name');
const userEmail = requireElement<HTMLElement>('user-email');
const userRole = requireElement<HTMLElement>('user-role');
const headerRole = requireElement<HTMLElement>('header-role');
const userGreeting = requireElement<HTMLElement>('user-greeting');
const pageSubtitle = requireElement<HTMLElement>('page-subtitle');
const userAvatar = requireElement<HTMLElement>('user-avatar');
const logoutButton = requireElement<HTMLButtonElement>('logout-button');

const pacientePanel = requireElement<HTMLElement>('paciente-panel');
const pacienteForm = requireElement<HTMLFormElement>('paciente-form');
const pacienteBusqueda = requireElement<HTMLInputElement>('paciente-busqueda');
const buscarPacienteButton = requireElement<HTMLButtonElement>('buscar-paciente');
const pacienteStatus = requireElement<HTMLElement>('paciente-status');
const pacienteResultados = requireElement<HTMLDivElement>('paciente-resultados');
const pacienteSeleccionado = requireElement<HTMLElement>('paciente-seleccionado');

const reservaForm = requireElement<HTMLFormElement>('reserva-form');
const odontologoSelect = requireElement<HTMLSelectElement>('odontologo');
const fechaInput = requireElement<HTMLInputElement>('fecha');
const buscarHorariosButton = requireElement<HTMLButtonElement>('buscar-horarios');
const reservaPaciente = requireElement<HTMLElement>('reserva-paciente');
const slotsStatus = requireElement<HTMLElement>('slots-status');
const slotsContainer = requireElement<HTMLDivElement>('slots');

const turnosTitle = requireElement<HTMLElement>('turnos-title');
const turnosStatus = requireElement<HTMLElement>('turnos-status');
const turnosList = requireElement<HTMLDivElement>('turnos-list');
const refrescarTurnosButton = requireElement<HTMLButtonElement>('refrescar-turnos');

const reservaModal = requireElement<HTMLDialogElement>('reserva-modal');
const reservaModalAlert = requireElement<HTMLDivElement>('reserva-modal-alert');
const modalOdontologo = requireElement<HTMLElement>('modal-odontologo');
const modalFecha = requireElement<HTMLElement>('modal-fecha');
const modalHorario = requireElement<HTMLElement>('modal-horario');
const modalPaciente = requireElement<HTMLElement>('modal-paciente');
const reservaConfirmForm = requireElement<HTMLFormElement>('reserva-confirm-form');
const motivoInput = requireElement<HTMLTextAreaElement>('motivo');
const confirmarReservaButton = requireElement<HTMLButtonElement>('confirmar-reserva');

const cancelarModal = requireElement<HTMLDialogElement>('cancelar-modal');
const cancelarModalAlert = requireElement<HTMLDivElement>('cancelar-modal-alert');
const cancelarResumen = requireElement<HTMLElement>('cancelar-resumen');
const confirmarCancelacionButton = requireElement<HTMLButtonElement>('confirmar-cancelacion');

/* --------------------------------------------------------------------------
   Helpers de fecha / formato
   -------------------------------------------------------------------------- */

function toIsoDate(date: Date): string {
  const year = String(date.getFullYear());
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function parseLocalDateTime(value: string): Date {
  const [datePart, timePart = '00:00:00'] = value.split('T');
  const [year, month, day] = datePart.split('-').map(Number);
  const [hour, minute] = timePart.split(':').map(Number);
  return new Date(year, month - 1, day, hour, minute);
}

function formatFechaLarga(isoDate: string): string {
  const [year, month, day] = isoDate.split('-').map(Number);
  const date = new Date(year, month - 1, day);
  return new Intl.DateTimeFormat('es-AR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
  }).format(date);
}

function formatFechaCorta(isoDateTime: string): string {
  const [datePart] = isoDateTime.split('T');
  const [year, month, day] = datePart.split('-').map(Number);
  const date = new Date(year, month - 1, day);
  return new Intl.DateTimeFormat('es-AR', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
  }).format(date);
}

function formatHora(isoDateTime: string): string {
  const timePart = isoDateTime.split('T')[1];
  return timePart ? timePart.slice(0, 5) : '';
}

/* --------------------------------------------------------------------------
   Helpers de UI
   -------------------------------------------------------------------------- */

let globalAlertTimer: number | undefined;

function showGlobal(message: string, variant: AlertVariant): void {
  showAlert(globalAlert, message, variant);
  if (globalAlertTimer !== undefined) {
    window.clearTimeout(globalAlertTimer);
  }
  globalAlertTimer = window.setTimeout(() => {
    hideAlert(globalAlert);
    globalAlertTimer = undefined;
  }, 6000);
}

function setSessionNotice(message: string | null, variant: AlertVariant = 'warning'): void {
  if (message === null) {
    hideAlert(sessionNotice);
    return;
  }
  showAlert(sessionNotice, message, variant);
}

function setSlotsStatus(message: string): void {
  slotsStatus.textContent = message;
}

function setTurnosStatus(message: string): void {
  turnosStatus.textContent = message;
}

function redirectToLogin(params = ''): void {
  window.location.replace(`login.html${params}`);
}

function isSessionError(error: unknown): boolean {
  return isApiError(error) && (error.status === 401 || error.status === 403);
}

function subtituloPorRol(rol: RolUsuario): string {
  switch (rol) {
    case 'ADMIN':
    case 'RECEPCION':
      return 'Buscá un paciente para reservar y gestionar sus turnos.';
    case 'ODONTOLOGO':
      return 'Consultá tu agenda profesional.';
    case 'PACIENTE':
      return 'Reservá nuevos turnos y gestioná tus citas.';
    default:
      return 'Gestioná tus turnos.';
  }
}

function renderUsuario(usuario: UserMeResponse): void {
  const roleLabel = ROLE_LABELS[usuario.rol] ?? usuario.rol;
  userName.textContent = usuario.username;
  userEmail.textContent = usuario.email;
  userRole.textContent = roleLabel;
  headerRole.textContent = roleLabel;
  userGreeting.textContent = usuario.username;
  userAvatar.textContent = usuario.username.slice(0, 1).toUpperCase();
  pageSubtitle.textContent = subtituloPorRol(usuario.rol);
}

function configurarRol(usuario: UserMeResponse): void {
  const esStaff = usuario.rol === 'ADMIN' || usuario.rol === 'RECEPCION';
  pacientePanel.hidden = !esStaff;
}

function habilitarReserva(enabled: boolean): void {
  const hayOdontologos = state.odontologos.length > 0;
  odontologoSelect.disabled = !(enabled && hayOdontologos);
  fechaInput.disabled = !enabled;
  buscarHorariosButton.disabled = !(enabled && hayOdontologos);
  if (!enabled) {
    slotsContainer.replaceChildren();
  }
}

function deshabilitarReserva(message: string): void {
  habilitarReserva(false);
  setSlotsStatus(message);
}

function mostrarTurnosPendientes(message: string): void {
  state.turnos = [];
  turnosList.replaceChildren();
  setTurnosStatus(message);
}

function setPaciente(paciente: PacienteContexto): void {
  state.paciente = paciente;
  setSessionNotice(null);
  pacienteSeleccionado.hidden = false;
  pacienteSeleccionado.textContent = `Paciente seleccionado: ${paciente.nombreCompleto}`;
  reservaPaciente.hidden = false;
  reservaPaciente.textContent = `Reservando para: ${paciente.nombreCompleto}`;
  turnosTitle.textContent =
    state.usuario?.rol === 'PACIENTE' ? 'Mis turnos' : 'Turnos del paciente';
  habilitarReserva(true);
  void cargarTurnos();
  if (odontologoSelect.value.length > 0 && fechaInput.value.length > 0) {
    void cargarDisponibilidad();
  }
}

/* --------------------------------------------------------------------------
   Sesión y paciente
   -------------------------------------------------------------------------- */

async function resolverPaciente(usuario: UserMeResponse): Promise<PacienteContexto | null> {
  if (typeof usuario.pacienteId === 'number') {
    return { id: usuario.pacienteId, nombreCompleto: usuario.username };
  }

  if (usuario.rol !== 'PACIENTE') {
    return null;
  }

  try {
    const paciente = await obtenerPacienteActual();
    return {
      id: paciente.id,
      nombreCompleto: `${paciente.nombre} ${paciente.apellido}`.trim(),
    };
  } catch (error) {
    // Solo 401 invalida la sesión.
    if (isApiError(error) && error.status === 401) {
      removeToken();
      redirectToLogin('?expired=1');
      return null;
    }
    // El backend actual en upstream/main no expone /api/v1/pacientes/me (puede devolver
    // 400, 403, 404, 405, 500 por type mismatch del parámetro {id}, o 501).
    // Tratamos la ausencia del endpoint como vínculo no disponible para que el dashboard
    // informe el estado pendiente de forma limpia sin alertas de error genéricas.
    if (isApiError(error) && [400, 403, 404, 405, 500, 501].includes(error.status)) {
      return null;
    }
    showGlobal(describeError(error), 'error');
    return null;
  }
}

/* --------------------------------------------------------------------------
   Odontólogos y disponibilidad
   -------------------------------------------------------------------------- */

async function cargarOdontologos(): Promise<void> {
  try {
    const odontologos = await listarOdontologosPublicos();
    state.odontologos = odontologos;
    odontologoSelect.replaceChildren();

    if (odontologos.length === 0) {
      const option = document.createElement('option');
      option.value = '';
      option.textContent = 'No hay profesionales disponibles';
      odontologoSelect.append(option);
      odontologoSelect.disabled = true;
      buscarHorariosButton.disabled = true;
      setSlotsStatus('No hay profesionales disponibles para reservar.');
      return;
    }

    for (const odontologo of odontologos) {
      const option = document.createElement('option');
      option.value = String(odontologo.id);
      option.textContent = `${odontologo.nombre} ${odontologo.apellido} — ${odontologo.especialidad}`;
      odontologoSelect.append(option);
    }
    odontologoSelect.value = String(odontologos[0].id);
  } catch (error) {
    setSlotsStatus('No pudimos cargar los profesionales. Intentá recargar la página.');
    showGlobal(describeError(error), 'error');
  }
}

let disponibilidadRequestId = 0;

async function cargarDisponibilidad(): Promise<void> {
  if (!state.paciente) {
    setSlotsStatus('Seleccioná un paciente para consultar horarios.');
    return;
  }

  const odontologoId = Number(odontologoSelect.value);
  const fecha = fechaInput.value;
  if (!odontologoId || !fecha) {
    setSlotsStatus('Seleccioná un profesional y una fecha.');
    return;
  }

  const requestId = ++disponibilidadRequestId;
  slotsContainer.replaceChildren();
  setSlotsStatus('Cargando horarios disponibles…');
  setButtonBusy(buscarHorariosButton, true, 'Buscando…');

  try {
    const disponibilidad = await obtenerDisponibilidad(odontologoId, fecha);
    if (requestId !== disponibilidadRequestId) {
      return;
    }
    renderSlots(disponibilidad);
  } catch (error) {
    if (requestId !== disponibilidadRequestId) {
      return;
    }
    if (isApiError(error) && error.status === 400) {
      setSlotsStatus(error.message);
    } else {
      setSlotsStatus('No pudimos cargar la disponibilidad. Intentá nuevamente.');
      showGlobal(describeError(error), 'error');
    }
  } finally {
    if (requestId === disponibilidadRequestId) {
      setButtonBusy(buscarHorariosButton, false, '');
    }
  }
}

function renderSlots(disponibilidad: Disponibilidad): void {
  slotsContainer.replaceChildren();

  if (disponibilidad.slotsDisponibles.length === 0) {
    setSlotsStatus(
      `No hay horarios libres para ${formatFechaLarga(disponibilidad.fecha)}. Probá otra fecha.`,
    );
    return;
  }

  setSlotsStatus(
    `${disponibilidad.slotsDisponibles.length} horarios disponibles · ${disponibilidad.duracionMinutos} min por turno`,
  );

  for (const slot of disponibilidad.slotsDisponibles) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'slot';
    button.textContent = `${slot.horaInicio} – ${slot.horaFin}`;
    button.setAttribute(
      'aria-label',
      `Reservar turno de ${slot.horaInicio} a ${slot.horaFin}`,
    );
    button.addEventListener('click', () => abrirReserva(slot));
    slotsContainer.append(button);
  }
}

/* --------------------------------------------------------------------------
   Reserva
   -------------------------------------------------------------------------- */

function abrirReserva(slot: SlotHorario): void {
  if (!state.paciente) {
    return;
  }
  const odontologo = state.odontologos.find(
    (item) => item.id === Number(odontologoSelect.value),
  );
  if (!odontologo) {
    showGlobal('Seleccioná un profesional antes de reservar.', 'warning');
    return;
  }

  state.slotSeleccionado = slot;
  state.odontologoSeleccionado = odontologo;
  modalOdontologo.textContent = `${odontologo.nombre} ${odontologo.apellido} · ${odontologo.especialidad}`;
  modalFecha.textContent = formatFechaLarga(fechaInput.value);
  modalHorario.textContent = `${slot.horaInicio} – ${slot.horaFin}`;
  modalPaciente.textContent = state.paciente.nombreCompleto;
  motivoInput.value = '';
  hideAlert(reservaModalAlert);

  if (!reservaModal.open) {
    reservaModal.showModal();
  }
}

async function confirmarReserva(): Promise<void> {
  const paciente = state.paciente;
  const slot = state.slotSeleccionado;
  const odontologo = state.odontologoSeleccionado;

  if (!paciente || !slot || !odontologo) {
    showAlert(reservaModalAlert, 'Faltan datos para confirmar el turno.', 'error');
    return;
  }

  hideAlert(reservaModalAlert);
  setButtonBusy(confirmarReservaButton, true, 'Confirmando…');

  try {
    await crearTurno({
      pacienteId: paciente.id,
      odontologoId: odontologo.id,
      fechaHoraInicio: slot.fechaHoraInicio,
      fechaHoraFin: slot.fechaHoraFin,
      motivoConsulta: motivoInput.value.trim() || undefined,
      estado: 'CONFIRMADO',
    });

    reservaModal.close();
    showGlobal('Turno confirmado correctamente.', 'success');
    await cargarDisponibilidad();
    await cargarTurnos();
  } catch (error) {
    if (isApiError(error) && error.status === 409) {
      reservaModal.close();
      showGlobal('Ese horario ya no está disponible. Actualizamos la disponibilidad.', 'warning');
      await cargarDisponibilidad();
      return;
    }
    showAlert(reservaModalAlert, describeError(error), 'error');
  } finally {
    setButtonBusy(confirmarReservaButton, false, '');
  }
}

/* --------------------------------------------------------------------------
   Turnos
   -------------------------------------------------------------------------- */

let turnosRequestId = 0;

function ordenarTurnos(turnos: Turno[]): Turno[] {
  return [...turnos].sort(
    (a, b) =>
      parseLocalDateTime(a.fechaHoraInicio).getTime() -
      parseLocalDateTime(b.fechaHoraInicio).getTime(),
  );
}

async function cargarTurnos(): Promise<void> {
  if (!state.paciente) {
    mostrarTurnosPendientes('Seleccioná un paciente para ver sus turnos.');
    return;
  }

  const requestId = ++turnosRequestId;
  setTurnosStatus('Cargando turnos…');
  setButtonBusy(refrescarTurnosButton, true, 'Actualizando…');

  try {
    const turnos = await listarTurnos({ pacienteId: state.paciente.id });
    if (requestId !== turnosRequestId) {
      return;
    }
    state.turnos = ordenarTurnos(turnos);
    renderTurnos();
  } catch (error) {
    if (requestId !== turnosRequestId) {
      return;
    }
    setTurnosStatus('No pudimos cargar los turnos. Intentá nuevamente.');
    showGlobal(describeError(error), 'error');
  } finally {
    if (requestId === turnosRequestId) {
      setButtonBusy(refrescarTurnosButton, false, '');
    }
  }
}

function puedeCancelar(turno: Turno): boolean {
  return turno.estado === 'PENDIENTE' || turno.estado === 'CONFIRMADO';
}

function crearTurnoElemento(turno: Turno): HTMLElement {
  const article = document.createElement('article');
  article.className = 'turno';

  const when = document.createElement('div');
  when.className = 'turno__when';
  const date = document.createElement('span');
  date.className = 'turno__date';
  date.textContent = formatFechaCorta(turno.fechaHoraInicio);
  const time = document.createElement('span');
  time.className = 'turno__time';
  time.textContent = `${formatHora(turno.fechaHoraInicio)} – ${formatHora(turno.fechaHoraFin)}`;
  when.append(date, time);

  const details = document.createElement('div');
  details.className = 'turno__details';
  const dentist = document.createElement('p');
  dentist.className = 'turno__dentist';
  dentist.textContent = turno.odontologoNombreCompleto;
  const specialty = document.createElement('p');
  specialty.className = 'turno__specialty';
  specialty.textContent = turno.odontologoEspecialidad;
  details.append(dentist, specialty);

  if (turno.motivoConsulta) {
    const reason = document.createElement('p');
    reason.className = 'turno__reason';
    reason.textContent = `Motivo: ${turno.motivoConsulta}`;
    details.append(reason);
  }

  const actions = document.createElement('div');
  actions.className = 'turno__actions';
  const badge = document.createElement('span');
  badge.className = `badge ${ESTADO_CLASES[turno.estado]}`;
  badge.textContent = ESTADO_LABELS[turno.estado];
  actions.append(badge);

  if (puedeCancelar(turno)) {
    const cancelButton = document.createElement('button');
    cancelButton.type = 'button';
    cancelButton.className = 'btn btn--danger btn--sm';
    cancelButton.textContent = 'Cancelar';
    cancelButton.dataset.cancelarId = String(turno.id);
    cancelButton.setAttribute(
      'aria-label',
      `Cancelar turno del ${formatFechaCorta(turno.fechaHoraInicio)} a las ${formatHora(turno.fechaHoraInicio)}`,
    );
    actions.append(cancelButton);
  }

  article.append(when, details, actions);
  return article;
}

function renderTurnos(): void {
  turnosList.replaceChildren();

  if (state.turnos.length === 0) {
    setTurnosStatus('No hay turnos registrados todavía.');
    return;
  }

  const sufijo = state.usuario?.rol === 'PACIENTE' ? 'en tu historial' : 'del paciente';
  setTurnosStatus(
    `${state.turnos.length} turno${state.turnos.length === 1 ? '' : 's'} ${sufijo}.`,
  );

  for (const turno of state.turnos) {
    turnosList.append(crearTurnoElemento(turno));
  }
}

function abrirCancelacion(turno: Turno): void {
  state.turnoACancelar = turno;
  cancelarResumen.replaceChildren();

  const filas: Array<[string, string]> = [
    ['Profesional', turno.odontologoNombreCompleto],
    ['Especialidad', turno.odontologoEspecialidad],
    ['Fecha', formatFechaLarga(turno.fechaHoraInicio.split('T')[0])],
    ['Horario', `${formatHora(turno.fechaHoraInicio)} – ${formatHora(turno.fechaHoraFin)}`],
  ];

  for (const [label, value] of filas) {
    const row = document.createElement('div');
    const dt = document.createElement('dt');
    dt.textContent = label;
    const dd = document.createElement('dd');
    dd.textContent = value;
    row.append(dt, dd);
    cancelarResumen.append(row);
  }

  hideAlert(cancelarModalAlert);
  if (!cancelarModal.open) {
    cancelarModal.showModal();
  }
}

async function confirmarCancelacion(): Promise<void> {
  const turno = state.turnoACancelar;
  if (!turno) {
    return;
  }

  hideAlert(cancelarModalAlert);
  setButtonBusy(confirmarCancelacionButton, true, 'Cancelando…');

  try {
    await cancelarTurno(turno.id);
    cancelarModal.close();
    showGlobal('Turno cancelado correctamente.', 'success');
    await cargarTurnos();
    await refrescarDisponibilidadSiCorresponde(turno);
  } catch (error) {
    showAlert(cancelarModalAlert, describeError(error), 'error');
  } finally {
    setButtonBusy(confirmarCancelacionButton, false, '');
  }
}

async function refrescarDisponibilidadSiCorresponde(turno: Turno): Promise<void> {
  const odontologoId = Number(odontologoSelect.value);
  const fecha = fechaInput.value;
  if (!odontologoId || !fecha) {
    return;
  }
  if (turno.odontologoId === odontologoId && turno.fechaHoraInicio.startsWith(fecha)) {
    await cargarDisponibilidad();
  }
}

/* --------------------------------------------------------------------------
   Búsqueda de pacientes (ADMIN / RECEPCION)
   -------------------------------------------------------------------------- */

function crearResultadoPaciente(paciente: Paciente): HTMLButtonElement {
  const button = document.createElement('button');
  button.type = 'button';
  button.className = 'resultado';

  const info = document.createElement('span');
  info.className = 'resultado__info';
  const name = document.createElement('strong');
  name.textContent = `${paciente.nombre} ${paciente.apellido}`;
  const meta = document.createElement('span');
  meta.className = 'resultado__meta';
  meta.textContent = `DNI ${paciente.dni}${paciente.obraSocial ? ` · ${paciente.obraSocial}` : ''}`;
  info.append(name, meta);

  const action = document.createElement('span');
  action.className = 'resultado__action';
  action.textContent = 'Seleccionar';

  button.append(info, action);
  button.addEventListener('click', () => {
    const contexto: PacienteContexto = {
      id: paciente.id,
      nombreCompleto: `${paciente.nombre} ${paciente.apellido}`.trim(),
    };
    pacienteResultados.replaceChildren();
    pacienteStatus.textContent = '';
    setPaciente(contexto);
    showGlobal(`Paciente seleccionado: ${contexto.nombreCompleto}.`, 'success');
  });

  return button;
}

async function buscarPaciente(): Promise<void> {
  const term = pacienteBusqueda.value.trim();
  pacienteResultados.replaceChildren();

  if (term.length === 0) {
    pacienteStatus.textContent = 'Ingresá un DNI o un nombre para buscar.';
    return;
  }

  pacienteStatus.textContent = 'Buscando pacientes…';
  setButtonBusy(buscarPacienteButton, true, 'Buscando…');

  try {
    const dni = term.replace(/\./g, '');
    const filtros = /^\d{6,}$/.test(dni) ? { dni } : { q: term };
    const resultados = await buscarPacientes(filtros);

    if (resultados.length === 0) {
      pacienteStatus.textContent = 'No encontramos pacientes con ese criterio.';
      return;
    }

    pacienteStatus.textContent = `${resultados.length} resultado${resultados.length === 1 ? '' : 's'}. Elegí un paciente.`;
    for (const paciente of resultados.slice(0, 8)) {
      pacienteResultados.append(crearResultadoPaciente(paciente));
    }
  } catch (error) {
    pacienteStatus.textContent = 'No pudimos buscar pacientes.';
    showGlobal(describeError(error), 'error');
  } finally {
    setButtonBusy(buscarPacienteButton, false, '');
  }
}

/* --------------------------------------------------------------------------
   Eventos e inicialización
   -------------------------------------------------------------------------- */

function bindEvents(): void {
  logoutButton.addEventListener('click', () => {
    removeToken();
    redirectToLogin('?logout=1');
  });

  reservaForm.addEventListener('submit', (event) => {
    event.preventDefault();
    if (!reservaForm.reportValidity()) {
      return;
    }
    void cargarDisponibilidad();
  });

  odontologoSelect.addEventListener('change', () => {
    slotsContainer.replaceChildren();
    setSlotsStatus('Presioná "Buscar horarios" para consultar la disponibilidad.');
  });

  fechaInput.addEventListener('change', () => {
    slotsContainer.replaceChildren();
    setSlotsStatus('Presioná "Buscar horarios" para consultar la disponibilidad.');
  });

  reservaConfirmForm.addEventListener('submit', (event) => {
    event.preventDefault();
    void confirmarReserva();
  });

  confirmarCancelacionButton.addEventListener('click', () => {
    void confirmarCancelacion();
  });

  refrescarTurnosButton.addEventListener('click', () => {
    void cargarTurnos();
  });

  pacienteForm.addEventListener('submit', (event) => {
    event.preventDefault();
    void buscarPaciente();
  });

  turnosList.addEventListener('click', (event) => {
    const target = event.target;
    if (!(target instanceof Element)) {
      return;
    }
    const button = target.closest<HTMLButtonElement>('[data-cancelar-id]');
    if (!button) {
      return;
    }
    const id = Number(button.dataset.cancelarId);
    const turno = state.turnos.find((item) => item.id === id);
    if (turno) {
      abrirCancelacion(turno);
    }
  });

  document.querySelectorAll<HTMLButtonElement>('[data-close-dialog]').forEach((button) => {
    const dialogId = button.dataset.closeDialog;
    if (!dialogId) {
      return;
    }
    const dialog = document.getElementById(dialogId);
    if (dialog instanceof HTMLDialogElement) {
      button.addEventListener('click', () => dialog.close());
    }
  });

  for (const dialog of [reservaModal, cancelarModal]) {
    dialog.addEventListener('click', (event) => {
      if (event.target === dialog) {
        dialog.close();
      }
    });
  }

  reservaModal.addEventListener('close', () => {
    state.slotSeleccionado = null;
    state.odontologoSeleccionado = null;
    hideAlert(reservaModalAlert);
    setButtonBusy(confirmarReservaButton, false, '');
  });

  cancelarModal.addEventListener('close', () => {
    state.turnoACancelar = null;
    hideAlert(cancelarModalAlert);
    setButtonBusy(confirmarCancelacionButton, false, '');
  });
}

async function init(): Promise<void> {
  bindEvents();

  const hoy = toIsoDate(new Date());
  fechaInput.min = hoy;
  fechaInput.value = hoy;

  if (!getToken()) {
    redirectToLogin();
    return;
  }

  let usuario: UserMeResponse;
  try {
    usuario = await me();
  } catch (error) {
    if (isSessionError(error)) {
      removeToken();
      redirectToLogin('?expired=1');
      return;
    }
    showGlobal(describeError(error), 'error');
    pageSubtitle.textContent = 'No pudimos validar tu sesión.';
    deshabilitarReserva('No pudimos validar tu sesión.');
    mostrarTurnosPendientes('No pudimos validar tu sesión.');
    return;
  }

  state.usuario = usuario;
  renderUsuario(usuario);
  configurarRol(usuario);
  await cargarOdontologos();

  if (usuario.rol === 'ODONTOLOGO') {
    setSessionNotice(
      'La agenda profesional estará disponible cuando el backend exponga el vínculo entre tu usuario y tu ficha de odontólogo.',
      'info',
    );
    deshabilitarReserva('La reserva no está disponible para odontólogos.');
    mostrarTurnosPendientes('La agenda profesional todavía no está vinculada.');
    return;
  }

  if (usuario.rol === 'PACIENTE') {
    const paciente = await resolverPaciente(usuario);
    if (paciente) {
      setPaciente(paciente);
      return;
    }
    setSessionNotice(
      'Tu usuario todavía no tiene una ficha de paciente vinculada. Un recepcionista debe completarla para que puedas reservar y ver tus turnos.',
      'warning',
    );
    deshabilitarReserva('Ficha de paciente pendiente.');
    mostrarTurnosPendientes('Tu ficha de paciente todavía no está vinculada.');
    return;
  }

  setSessionNotice('Seleccioná un paciente para reservar y gestionar sus turnos.', 'info');
  deshabilitarReserva('Seleccioná un paciente para comenzar.');
  mostrarTurnosPendientes('Seleccioná un paciente para ver sus turnos.');
}

void init().catch((error: unknown) => {
  showGlobal(describeError(error), 'error');
});
