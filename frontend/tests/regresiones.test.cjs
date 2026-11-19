const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const ts = require('typescript');
const crypto = require('node:crypto').webcrypto;

function load(file, service = {}, globals = {}) {
  const exports = {};
  const core = {
    Component: () => () => {}, Injectable: () => () => {}, Input: () => () => {}, Output: () => () => {},
    EventEmitter: class { emit() {} }, inject: () => service
  };
  const code = ts.transpileModule(fs.readFileSync(path.join(__dirname, '../src/app', file), 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022, experimentalDecorators: true }
  }).outputText;
  vm.runInNewContext(code, { exports, require: name => name === '@angular/core' ? core : name === 'rxjs' ? require('rxjs') : name.includes('environments/environment') ? { environment: { apiBaseUrl: '/api/v1' } } : {}, Date, Number, Set, Map, crypto, atob, TextDecoder, history: {}, alert() {}, ...globals });
  return exports;
}

test('la búsqueda de órdenes filtra por modelo y número', () => {
  const { OrdenesComponent } = load('features/ordenes/ordenes.component.ts');
  const c = new OrdenesComponent();
  c.ordenes = [{ numero: 'OP-1', modelo: 'Comfort', estado: 'borrador' }, { numero: 'OP-2', modelo: 'Eco', estado: 'calidad' }];
  c.busqueda = 'COMFORT';
  assert.equal(c.ordenesFiltradas.length, 1);
  c.busqueda = 'OP-2';
  assert.equal(c.ordenesFiltradas[0].modelo, 'Eco');
});

test('el filtro de despacho usa la fecha real y preserva cancelados', () => {
  const { DespachoComponent } = load('features/despacho/despacho.component.ts');
  const c = new DespachoComponent();
  c.despachos = [{ fecha_salida: '2026-11-13', estado: 'cancelado' }, { fecha_salida: '2026-11-12', estado: 'entregado' }];
  c.filtroFecha = '2026-11-13';
  assert.equal(c.despachosFiltrados.length, 1);
  assert.equal(c.estadoLabel(c.despachosFiltrados[0].estado), 'Cancelado');
  assert.equal(c.claseEstado('cancelado'), 'pill-danger');
});

test('el modal de despacho se reinicia con el estado de cada despacho', () => {
  const { EstadoDespachoModalComponent } = load('features/despacho/estado-despacho-modal.component.ts');
  const c = new EstadoDespachoModalComponent();
  c.visible = true;
  c.despacho = { estado: 'entregado' }; c.ngOnChanges();
  assert.equal(c.nuevoEstado, 'entregado');
  c.nuevoEstado = 'cancelado';
  c.despacho = { estado: 'transito' }; c.ngOnChanges();
  assert.equal(c.nuevoEstado, 'transito');
});

test('el modal de avance borra notas y resultados del lote anterior', () => {
  const { AvanceLoteModalComponent } = load('features/produccion/avance-lote-modal.component.ts');
  const c = new AvanceLoteModalComponent();
  c.visible = true; c.lote = { id: 'nuevo' };
  c.avance = { notas: 'viejas', resultado: 'rechazado', area_destino: 'Tapizado' };
  c.ngOnChanges();
  assert.equal(c.avance.notas, ''); assert.equal(c.avance.resultado, ''); assert.equal(c.avance.lote_id, 'nuevo');
});

test('la fecha de hoy es válida y la cantidad debe ser entera', () => {
  const { NuevaOrdenComponent } = load('features/ordenes/nueva/nueva.component.ts', {
    getCatalogos: () => ({ subscribe() {} }), getUsuario: () => null
  });
  const c = new NuevaOrdenComponent();
  const hoy = new Date();
  c.fechaEntrega = [hoy.getFullYear(), String(hoy.getMonth() + 1).padStart(2, '0'), String(hoy.getDate()).padStart(2, '0')].join('-');
  assert.equal(c.fechaPasada, false);
  c.modeloId = 'modelo'; c.cantidad = 1.5; c.prioridad = 'Alta';
  assert.equal(c.esCompleto, false);
  c.cantidad = 2;
  assert.equal(c.esCompleto, true);
});

const { of, throwError } = require('rxjs');
function editorService(extra = {}) {
  return { getCatalogos: () => of({ modelos: [] }), getUsuario: () => null, navigate() {}, ...extra };
}
function completarEditor(c) {
  const d = new Date(); d.setDate(d.getDate() + 3);
  c.modeloId = 'modelo'; c.cantidad = 5; c.prioridad = 'Alta';
  c.fechaEntrega = [d.getFullYear(), String(d.getMonth()+1).padStart(2,'0'), String(d.getDate()).padStart(2,'0')].join('-');
}

test('un borrador parcial conserva los campos sin definir', () => {
  let guardado;
  const servicio = editorService({ guardarBorrador: d => { guardado = d; return of({ numero: 'BORR-1' }); } });
  const { NuevaOrdenComponent } = load('features/ordenes/nueva/nueva.component.ts', servicio);
  const c = new NuevaOrdenComponent(); c.observaciones = 'Definir medidas mañana';
  assert.equal(c.esBorradorValido, true); c.guardarBorrador();
  assert.equal(guardado.modelo_id, null); assert.equal(guardado.cantidad, null);
  assert.equal(guardado.fecha_entrega, null); assert.equal(guardado.observaciones, 'Definir medidas mañana');
});

test('reintentar la creación mantiene el identificador y cambiar datos lo renueva', () => {
  const solicitudes = [];
  const servicio = editorService({ crearOrden: d => { solicitudes.push({ ...d }); return throwError(() => ({ error: { mensaje: 'Corte de conexión' } })); } });
  const { NuevaOrdenComponent } = load('features/ordenes/nueva/nueva.component.ts', servicio);
  const c = new NuevaOrdenComponent(); completarEditor(c);
  c.crearOrden(); c.crearOrden();
  assert.equal(solicitudes.length, 2); assert.equal(solicitudes[0].solicitudId, solicitudes[1].solicitudId);
  c.cantidad = 6; c.crearOrden();
  assert.notEqual(solicitudes[1].solicitudId, solicitudes[2].solicitudId);
});

test('retomar y confirmar un borrador actualiza el existente sin crear otro', () => {
  const operaciones = [];
  const servicio = editorService({
    snapshot: { paramMap: { get: () => 'BORR-1' } },
    getOrden: () => of({ id: 'id-1', numero: 'BORR-1', estado: 'borrador', cantidad: null }),
    editarBorrador: (id, datos) => { operaciones.push(['editar', id, datos.cantidad]); return of({ id }); },
    confirmarBorrador: id => { operaciones.push(['confirmar', id]); return of({ id }); }
  });
  const { NuevaOrdenComponent } = load('features/ordenes/nueva/nueva.component.ts', servicio);
  const c = new NuevaOrdenComponent(); completarEditor(c); c.crearOrden();
  assert.equal(operaciones.length, 2); assert.equal(operaciones[0][0], 'editar'); assert.equal(operaciones[1][0], 'confirmar');
  assert.equal(operaciones[0][1], 'id-1'); assert.equal(operaciones[1][1], 'id-1');
});

test('Recordarme usa almacenamiento persistente y sin marcar usa la sesión', () => {
  class Storage { constructor() { this.items = new Map(); } getItem(k) { return this.items.get(k) || null; } setItem(k,v) { this.items.set(k,v); } removeItem(k) { this.items.delete(k); } }
  const localStorage = new Storage(), sessionStorage = new Storage();
  const token = 'eyJhbGciOiJIUzI1NiJ9.' + Buffer.from(JSON.stringify({ exp: Math.floor(Date.now()/1000)+3600, turno: 'Turno B' })).toString('base64url') + '.firma';
  const { AuthService } = load('core/auth.service.ts', {}, { localStorage, sessionStorage });
  const auth = new AuthService({ post: () => of({ token, nombre: 'Usuario', correo: 'u@test.local', rol: 'gerente' }) }, { navigate() {} });
  auth.login({ correo: 'u@test.local', password: 'clave' }, false).subscribe();
  assert.equal(sessionStorage.getItem('token'), token); assert.equal(localStorage.getItem('token'), null); assert.equal(auth.tieneToken(), true);
  auth.login({ correo: 'u@test.local', password: 'clave' }, true).subscribe();
  assert.equal(localStorage.getItem('token'), token); assert.equal(sessionStorage.getItem('token'), null);
  auth.logout(); assert.equal(auth.getToken(), null);
});

test('programar permite lote completo o parcial y rechaza sobreasignar', () => {
  const enviados = [];
  const servicio = { disponibles: () => of([{ id: 'lote', codigo: 'LT-1', modelo: 'Modelo', unidades: 10, disponibles: 10 }]), crear: datos => { enviados.push(datos); return of({ codigo: 'DS-1' }); } };
  const { ProgramarDespachoComponent } = load('features/despacho/programar-despacho.component.ts', servicio);
  const c = new ProgramarDespachoComponent(); c.ngOnChanges();
  const l = c.lotes[0]; l.seleccionado = true; l.completo = true; c.cambiarModo(l);
  assert.equal(c.total, 10); assert.equal(c.valido, true); c.guardar();
  assert.equal(enviados[0].lotes[0].cantidad, 10);
  l.completo = false; l.cantidad = 3; assert.equal(c.valido, true); c.guardar();
  assert.equal(enviados[1].lotes[0].cantidad, 3);
  l.cantidad = 11; assert.equal(c.valido, false);
});

test('los estados terminales de despacho no ofrecen reapertura', () => {
  const { EstadoDespachoModalComponent } = load('features/despacho/estado-despacho-modal.component.ts');
  const c = new EstadoDespachoModalComponent(); c.despacho = { estado: 'entregado' };
  assert.equal(c.estadosPermitidos.length, 1); assert.equal(c.estadosPermitidos[0], 'entregado');
  c.despacho = { estado: 'transito' };
  assert.equal(c.estadosPermitidos.includes('programado'), false); assert.equal(c.estadosPermitidos.includes('entregado'), true);
});
