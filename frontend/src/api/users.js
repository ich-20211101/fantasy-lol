import { API_BASE_URL } from './config'

export async function getMe() {
  const response = await fetch(`${API_BASE_URL}/users/me`, {
    credentials: 'include',
    redirect: 'manual',
  })

  if (!response.ok) return null

  return response.json()
}

export async function logout() {
  await fetch(`${API_BASE_URL}/users/logout`, {
    method: 'POST',
    credentials: 'include',
  })
}

export function loginWithGoogle() {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/google`
}

export async function updateNickname(username) {
  const response = await fetch(`${API_BASE_URL}/users/me/nickname`, {
    method: 'PATCH',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username }),
  })

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    throw new Error(data?.error || 'Failed to update nickname')
  }

  return data
}

export async function deleteAccount(reason, note) {
  const response = await fetch(`${API_BASE_URL}/users/me`, {
    method: 'DELETE',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ reason, note }),
  })

  if (!response.ok) {
    const data = await response.json().catch(() => null)
    throw new Error(data?.error || 'Failed to delete account')
  }
}