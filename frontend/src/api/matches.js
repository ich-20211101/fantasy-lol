import { API_BASE_URL } from './config'

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
