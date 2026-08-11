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

export async function syncSeason(seasonName) {
  const response = await fetch(`${API_BASE_URL}/matches/sync-season?seasonName=${encodeURIComponent(seasonName)}`, {
    method: 'POST',
    credentials: 'include',
  })

  const text = await response.text().catch(() => '')

  if (!response.ok) {
    throw new Error(text || 'Season sync failed')
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

export async function getWeekMatches() {
  try {
    const response = await fetch(`${API_BASE_URL}/matches/week`, {
      credentials: 'include',
    })

    if (!response.ok) return null

    return await response.json()
  } catch (error) {
    console.error('Failed to fetch week matches:', error)
    return null
  }
}

export async function getRecentResults() {
  try {
    const response = await fetch(`${API_BASE_URL}/matches/recent-results`, {
      credentials: 'include',
    })

    if (!response.ok) return null

    return await response.json()
  } catch (error) {
    console.error('Failed to fetch recent results:', error)
    return null
  }
}
