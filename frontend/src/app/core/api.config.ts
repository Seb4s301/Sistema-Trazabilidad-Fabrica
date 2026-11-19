import { environment } from '../../environments/environment';

export const API_BASE = environment.apiBaseUrl;

export const ENDPOINTS = {
  auth: `${API_BASE}/auth`,
  ordenes: `${API_BASE}/ordenes`,
  produccion: `${API_BASE}/produccion`,
  comisiones: `${API_BASE}/comisiones`,
  despachos: `${API_BASE}/despachos`,
  catalogos: `${API_BASE}/catalogos`,
  dashboard: `${API_BASE}/resumen`
};
