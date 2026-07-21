import { API_BASE_URL } from './config'

export async function getProTeams() {
  const response = await fetch(`${API_BASE_URL}/pro-teams`, {
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Failed to fetch pro teams')
  }

  return response.json()
}

export async function syncProTeamsFromPlayers() {
  const response = await fetch(`${API_BASE_URL}/pro-teams/sync-from-players`, {
    method: 'POST',
    credentials: 'include',
  })

  const text = await response.text().catch(() => '')

  if (!response.ok) {
    throw new Error(text || 'Failed to sync pro teams')
  }

  return text
}

export async function updateProTeam(proTeamId, { shortName, status }) {
  const response = await fetch(`${API_BASE_URL}/pro-teams/${proTeamId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ shortName, status }),
  })

  if (!response.ok) {
    const data = await response.json().catch(() => null)
    throw new Error(data?.error || 'Failed to update pro team')
  }

  return response.json()
}

export async function deleteProTeam(proTeamId) {
  const response = await fetch(`${API_BASE_URL}/pro-teams/${proTeamId}`, {
    method: 'DELETE',
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Failed to delete pro team')
  }
}
