import { API_BASE_URL } from './config'

export async function syncMatches(date) {
  const response = await fetch(`${API_BASE_URL}/matches/sync?date=${date}`, {
    method: 'POST',
    credentials: 'include',
  })

  const text = await response.text().catch(() => '')

  if (!response.ok) {
    throw new Error(text || 'Sync failed')
  }

  return text
}

export async function syncPlayers(overviewPage) {
  const response = await fetch(`${API_BASE_URL}/matches/players/sync?overviewPage=${encodeURIComponent(overviewPage)}`, {
    method: 'POST',
    credentials: 'include',
  })

  const text = await response.text().catch(() => '')

  if (!response.ok) {
    throw new Error(text || 'Player sync failed')
  }

  return text
}

export async function getUpcomingMatches() {
  try {
    const response = await fetch(`${API_BASE_URL}/matches/upcoming`, {
      credentials: 'include',
    })

    if (!response.ok) return null

    return await response.json()
  } catch (error) {
    console.error('Failed to fetch upcoming matches:', error)
    return null
  }
}
