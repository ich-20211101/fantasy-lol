import { API_BASE_URL } from './config'

export async function adminLogin(email, password) {
  const response = await fetch(`${API_BASE_URL}/admin/login`, {
    method: 'POST',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })

  if (!response.ok) {
    const data = await response.json().catch(() => null)
    throw new Error(data?.error || 'Login failed')
  }
}

export async function getAdminMe() {
  const response = await fetch(`${API_BASE_URL}/admin/me`, {
    credentials: 'include',
    redirect: 'manual',
  })

  if (!response.ok) return null

  return response.json()
}

export async function adminLogout() {
  await fetch(`${API_BASE_URL}/users/logout`, {
    method: 'POST',
    credentials: 'include',
  })
}
