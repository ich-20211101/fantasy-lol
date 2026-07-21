import { API_BASE_URL } from './config'

export async function detectNewSeasons() {
  const response = await fetch(`${API_BASE_URL}/seasons/detect-new`, {
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Failed to detect new seasons')
  }

  return response.json()
}

export async function registerSeason(seasonName) {
  const response = await fetch(`${API_BASE_URL}/seasons?seasonName=${encodeURIComponent(seasonName)}`, {
    method: 'POST',
    credentials: 'include',
  })

  const text = await response.text().catch(() => '')

  if (!response.ok) {
    throw new Error(text || 'Failed to register season')
  }

  return text
}

export async function activateDueSeasons() {
  const response = await fetch(`${API_BASE_URL}/seasons/activate-due`, {
    method: 'POST',
    credentials: 'include',
  })

  const text = await response.text().catch(() => '')

  if (!response.ok) {
    throw new Error(text || 'Failed to activate due seasons')
  }

  return text
}

export async function endSeason(seasonName) {
  const response = await fetch(`${API_BASE_URL}/seasons/end?seasonName=${encodeURIComponent(seasonName)}`, {
    method: 'POST',
    credentials: 'include',
  })

  const text = await response.text().catch(() => '')

  if (!response.ok) {
    throw new Error(text || 'Failed to end season')
  }

  return text
}

export async function lockWeek(date, seasonName) {
  const response = await fetch(`${API_BASE_URL}/seasons/weeks/lock?date=${date}&seasonName=${encodeURIComponent(seasonName)}`, {
    method: 'POST',
    credentials: 'include',
  })

  const text = await response.text().catch(() => '')

  if (!response.ok) {
    throw new Error(text || 'Failed to lock week')
  }

  return text
}
