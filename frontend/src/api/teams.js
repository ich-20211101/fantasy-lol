const API_BASE_URL = 'http://localhost:8080'

export async function getMyTeam() {
  const response = await fetch(`${API_BASE_URL}/teams/me`, {
    credentials: 'include',
  })

  if (!response.ok) return null

  return response.json()
}

export async function saveRoster({ teamName, playerIds }) {
  const response = await fetch(`${API_BASE_URL}/teams/roster`, {
    method: 'PUT',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ teamName, playerIds }),
  })

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    throw new Error(data?.error || 'Failed to save roster')
  }

  return data
}