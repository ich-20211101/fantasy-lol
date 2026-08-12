import { API_BASE_URL } from './config'

export async function getPlayers({ activeOnly = false } = {}) {
  const params = activeOnly ? '?activeOnly=true' : ''

  const response = await fetch(`${API_BASE_URL}/players${params}`, {
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Failed to fetch players')
  }

  return response.json()
}

export async function getPurchaseList() {
  const response = await fetch(`${API_BASE_URL}/players/purchase-list`, {
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Failed to fetch purchase list')
  }

  return response.json()
}

export async function updatePlayerStatus(playerId, status) {
  const response = await fetch(`${API_BASE_URL}/players/${playerId}/status?status=${encodeURIComponent(status)}`, {
    method: 'PATCH',
    credentials: 'include',
  })

  const text = await response.text().catch(() => '')

  if (!response.ok) {
    throw new Error(text || 'Failed to update player status')
  }

  return text
}

export async function getPlayerRankings({ position = 'ALL', page = 1, pageSize = 20, playerIds } = {}) {
  const params = new URLSearchParams({ position, page, pageSize })

  if (playerIds && playerIds.length > 0) {
    params.set('playerIds', playerIds.join(','))
  }

  try {
    const response = await fetch(`${API_BASE_URL}/players/rankings?${params.toString()}`, {
      credentials: 'include',
    })

    if (!response.ok) return null

    return await response.json()
  } catch (error) {
    console.error('Failed to fetch player rankings:', error)
    return null
  }
}