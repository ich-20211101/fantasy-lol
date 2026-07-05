const API_BASE_URL = 'http://localhost:8080'

export async function getPlayers() {
  const response = await fetch(`${API_BASE_URL}/players`, {
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Failed to fetch players')
  }

  return response.json()
}