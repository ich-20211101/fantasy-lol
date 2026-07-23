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

export async function getPlayerRankings({ position = 'ALL', page = 1, pageSize = 20 } = {}) {
  const params = new URLSearchParams({ position, page, pageSize })

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