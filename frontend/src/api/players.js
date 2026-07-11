import { API_BASE_URL } from './config'

export async function getPlayers() {
  const response = await fetch(`${API_BASE_URL}/players`, {
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Failed to fetch players')
  }

  return response.json()
}