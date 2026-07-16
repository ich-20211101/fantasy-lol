import { API_BASE_URL } from './config'

export async function getMyScores() {
  try {
    const response = await fetch(`${API_BASE_URL}/users/me/scores`, {
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
  const params = new URLSearchParams({
    weekNumber,
    seasonName,
    page,
    pageSize,
  })

  try {
    const response = await fetch(`${API_BASE_URL}/users/leaderboard?${params.toString()}`, {
      credentials: 'include',
    })

    if (!response.ok) return null

    return await response.json()
  } catch (error) {
    console.error('Failed to fetch leaderboard:', error)
    return null
  }
}
