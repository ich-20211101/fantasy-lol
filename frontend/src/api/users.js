const API_BASE_URL = 'http://localhost:8080'

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