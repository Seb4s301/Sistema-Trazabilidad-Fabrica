import { environment } from '../../environments/environment';

export const API_BASE = environment.apiBaseUrl;

export const ENDPOINTS = {
  auth: `${API_BASE}/auth`,
  ordenes: `${API_BASE}/ordenes`,
  lotes: `${API_BASE}/produccion/lotes`,
  produccion: `${API_BASE}/produccion`,
  comisiones: `${API_BASE}/comisiones`,
  despachos: `${API_BASE}/despachos`,
  usuarios: `${API_BASE}/usuarios`,
  catalogos: `${API_BASE}/catalogos`,
  dashboard: `${API_BASE}/resumen`
};

export const HEADERS = {
  'Content-Type': 'application/json'
};
