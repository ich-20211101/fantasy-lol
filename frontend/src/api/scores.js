import { API_BASE_URL } from './config'

export async function getMyScores(weekNumber, seasonName) {
  const params = new URLSearchParams()

  if (weekNumber != null) params.set('weekNumber', weekNumber)
  if (seasonName != null) params.set('seasonName', seasonName)

  try {
    const response = await fetch(`${API_BASE_URL}/users/me/scores?${params.toString()}`, {
      credentials: 'include',
    })

    if (!response.ok) return null

    return await response.json()
  } catch (error) {
    console.error('Failed to fetch my scores:', error)
    return null
  }
}

export async function getLeaderboard(weekNumber, seasonName, { page = 1, pageSize = 20 } = {}) {
  const params = new URLSearchParams({ page, pageSize })

  if (weekNumber != null) params.set('weekNumber', weekNumber)
  if (seasonName != null) params.set('seasonName', seasonName)

  try {
    const response = await fetch(`${API_BASE_URL}/leaderboard?${params.toString()}`, {
      credentials: 'include',
    })

    if (!response.ok) return null

    return await response.json()
  } catch (error) {
    console.error('Failed to fetch leaderboard:', error)
    return null
  }
}

export async function getLeaderboardRounds() {
  try {
    const response = await fetch(`${API_BASE_URL}/leaderboard/rounds`, {
      credentials: 'include',
    })

    if (!response.ok) return null

    return await response.json()
  } catch (error) {
    console.error('Failed to fetch leaderboard rounds:', error)
    return null
  }
}
