import { useEffect, useState } from 'react'
import { getProTeams } from '../api/proTeams'

export function useProTeamAbbreviations() {
  const [abbreviations, setAbbreviations] = useState({})

  useEffect(() => {
    getProTeams()
      .then((teams) => {
        const map = {}
        teams.forEach((t) => { map[t.fullName] = t.shortName })
        setAbbreviations(map)
      })
      .catch((error) => console.error('Failed to load pro teams:', error))
  }, [])

  return abbreviations
}

export function abbreviateTeam(abbreviations, fullName) {
  return abbreviations[fullName] || fullName
}
