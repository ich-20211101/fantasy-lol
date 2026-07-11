import { API_BASE_URL } from './config'

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

export async function saveStarters({ teamId, playerIds }) {
  const response = await fetch(`${API_BASE_URL}/teams/${teamId}/starters`, {
    method: 'PUT',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ playerIds }),
  })

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    throw new Error(data?.error || 'Failed to save starters')
  }

  return data
}

// [TEST] 테스트 편의용 — 나중에 제거 예정
export async function deleteMyTeam() {
  const response = await fetch(`${API_BASE_URL}/teams/me`, {
    method: 'DELETE',
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Failed to delete team')
  }
}