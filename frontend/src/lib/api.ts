import type { AuthResponse, Car, CarInput, CatalogSearchResponse, CatalogVehicle, FuelEconomyOption, User } from './types'
const API_BASE = import.meta.env.VITE_API_URL || '/api'
const TOKEN_KEY = 'costdrive.token'
export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token)
export const clearToken = () => localStorage.removeItem(TOKEN_KEY)

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = getToken()
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init?.headers || {}),
    },
  })
  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    throw new Error(body.message || 'Não foi possível concluir a operação.')
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const login = (email: string, password: string) => request<AuthResponse>('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
export const register = (name: string, email: string, password: string) => request<AuthResponse>('/auth/register', { method: 'POST', body: JSON.stringify({ name, email, password }) })
export const getMe = () => request<User>('/auth/me')
export const getCars = () => request<Car[]>('/cars')
export const createCar = (payload: CarInput) => request<Car>('/cars', { method: 'POST', body: JSON.stringify(payload) })
export const updateCar = (id: number, payload: CarInput) => request<Car>(`/cars/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
export const deleteCar = (id: number) => request<void>(`/cars/${id}`, { method: 'DELETE' })

export const searchLocalCatalog = (query: string, type: string, year: string, page = 1) => {
  const params = new URLSearchParams({ q: query, type, page: String(page), pageSize: '30' })
  if (year) params.set('year', year)
  return request<CatalogSearchResponse>(`/catalog/search?${params}`)
}
export const searchFuelEconomy = (year: string, make: string, model: string) => {
  const params = new URLSearchParams({ year, make, model })
  return request<FuelEconomyOption[]>(`/external/fueleconomy/options?${params}`)
}
export const getFuelEconomyVehicle = (id: string, label: string) => {
  const params = new URLSearchParams({ label })
  return request<CatalogVehicle>(`/external/fueleconomy/vehicle/${encodeURIComponent(id)}?${params}`)
}
export const decodeNhtsaVin = (vin: string, year: string) => {
  const params = new URLSearchParams()
  if (year) params.set('year', year)
  return request<CatalogVehicle>(`/external/nhtsa/vin/${encodeURIComponent(vin)}${params.size ? `?${params}` : ''}`)
}
