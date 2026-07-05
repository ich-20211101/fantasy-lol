import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import './RosterPage.css'

import { getPlayers } from '../api/players'
import { saveRoster } from '../api/teams'

const POSITIONS = ['Top', 'Jungle', 'Mid', 'Bot', 'Support']

const POS_LABEL = {
  Top: 'TOP',
  Jungle: 'JUG',
  Mid: 'MID',
  Bot: 'BOT',
  Support: 'SPT',
}

const MAX_ROSTER_SIZE = 8
const PLAYER_POINT = 10
const TOTAL_POINT = 80

export default function RosterPage() {
  const navigate = useNavigate()

  const [players, setPlayers] = useState([])
  const [selectedIds, setSelectedIds] = useState(new Set())
  const [positionFilter, setPositionFilter] = useState('Top')
  const [teamFilter, setTeamFilter] = useState('ALL')
  const [teamOpen, setTeamOpen] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    getPlayers()
      .then(setPlayers)
      .catch(error => {
        console.error(error)
        setPlayers([])
      })
  }, [])

  const teams = useMemo(() => {
    const uniqueTeams = [...new Set(players.map(player => player.teamName))]
    return ['ALL', ...uniqueTeams.sort()]
  }, [players])

  const filteredPlayers = useMemo(() => {
    return players
      .filter(player => player.position === positionFilter)
      .filter(player => teamFilter === 'ALL' || player.teamName === teamFilter)
  }, [players, positionFilter, teamFilter])

  const selectedCount = selectedIds.size
  const remainingPoint = TOTAL_POINT - selectedCount * PLAYER_POINT

  const togglePlayer = (playerId) => {
    setSelectedIds(prev => {
      const next = new Set(prev)

      if (next.has(playerId)) {
        next.delete(playerId)
        return next
      }

      if (next.size >= MAX_ROSTER_SIZE) return next

      next.add(playerId)
      return next
    })
  }

  const handleSubmit = async () => {
    if (selectedCount !== MAX_ROSTER_SIZE) return

    try {
      setIsSubmitting(true)
      setMessage('')

      await saveRoster({
        teamName: 'My LFM Team',
        playerIds: [...selectedIds],
      })

      navigate('/')
    } catch (error) {
      console.error(error)
      setMessage(error.message || '저장 중 오류가 발생했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="build-page">
      <section className="build-frame">
        <header className="build-header">
          <button
            type="button"
            className="build-back"
            onClick={() => navigate('/')}
          >
            <svg width="9" height="15" viewBox="0 0 9 15" fill="none">
              <path
                d="M8 1L1.5 7.5L8 14"
                stroke="#0b0b0c"
                strokeWidth="1.8"
                strokeLinecap="round"
              />
            </svg>
          </button>

          <span className="build-header-title">BUILD ROSTER</span>
        </header>

        <section className="build-round">
          <span>2026 LCK Round 3</span>
          <strong>{remainingPoint} P</strong>
        </section>

        <nav className="build-tabs">
          {POSITIONS.map(position => {
            const active = positionFilter === position

            return (
              <button
                type="button"
                key={position}
                className={active ? 'active' : ''}
                onClick={() => setPositionFilter(position)}
              >
                <div>
                  <span>{POS_LABEL[position]}</span>
                  <svg width="13" height="10" viewBox="0 0 13 10" fill="none">
                    <path
                      d="M1 5L4.7 8.6L12 1"
                      stroke={active ? '#0b0b0c' : '#9a9a9e'}
                      strokeWidth="2.2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  </svg>
                </div>
              </button>
            )
          })}
        </nav>

        <section className="build-list">
          <div className="build-team-filter">
            <div className="build-dropdown">
              <button
                type="button"
                className="build-dropdown-trigger"
                onClick={() => setTeamOpen(prev => !prev)}
              >
                <span>{teamFilter}</span>
                <svg width="11" height="7" viewBox="0 0 11 7" fill="none">
                  <path
                    d="M1 1L5.5 5.5L10 1"
                    stroke="#6a6a6f"
                    strokeWidth="1.6"
                    strokeLinecap="round"
                  />
                </svg>
              </button>

              {teamOpen && (
                <div className="build-dropdown-menu">
                  {teams.map(team => {
                    const active = team === teamFilter

                    return (
                      <button
                        type="button"
                        key={team}
                        className={active ? 'active' : ''}
                        onClick={() => {
                          setTeamFilter(team)
                          setTeamOpen(false)
                        }}
                      >
                        <span>{team}</span>
                        {active && (
                          <svg width="11" height="8" viewBox="0 0 11 8" fill="none">
                            <path
                              d="M1 4L4 7L10 1"
                              stroke="#0b0b0c"
                              strokeWidth="1.7"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                            />
                          </svg>
                        )}
                      </button>
                    )
                  })}
                </div>
              )}
            </div>
          </div>

          {filteredPlayers.map(player => {
            const selected = selectedIds.has(player.playerId)

            return (
              <article
                key={player.playerId}
                className={`build-player ${selected ? 'selected' : ''}`}
              >
                <button type="button" className="build-info">
                  <svg width="4" height="11" viewBox="0 0 4 11" fill="#6a6a6f">
                    <circle cx="2" cy="1.4" r="1.4" />
                    <rect x="1.1" y="3.7" width="1.8" height="7" rx="0.9" />
                  </svg>
                </button>

                <div className="build-player-main">
                  <div className="build-player-title">
                    <span>{player.playerName}</span>
                    <em>{POS_LABEL[player.position]}</em>
                  </div>
                  <p>
                    {player.teamName} | {POS_LABEL[player.position]}
                  </p>
                </div>

                <strong className="build-point">{PLAYER_POINT}P</strong>

                <button
                  type="button"
                  className="build-toggle"
                  onClick={() => togglePlayer(player.playerId)}
                >
                  {selected ? (
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                      <path
                        d="M1.6 1.6L10.4 10.4M10.4 1.6L1.6 10.4"
                        stroke="#0b0b0c"
                        strokeWidth="1.9"
                        strokeLinecap="round"
                      />
                    </svg>
                  ) : (
                    <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
                      <path
                        d="M6.5 1.4V11.6M1.4 6.5H11.6"
                        stroke="#0b0b0c"
                        strokeWidth="1.9"
                        strokeLinecap="round"
                      />
                    </svg>
                  )}
                </button>
              </article>
            )
          })}

          <footer className="build-footer-info">
            <div>
              <span>PRIVACY POLICY</span>
              <span>SCORE POLICY</span>
              <span>CONTACT US</span>
            </div>
            <p>
              본 사이트는 LCK 팬페이지입니다. 라이엇 및 LCK와 무관하며,
              경기 데이터를 기반으로 한 시뮬레이션 콘텐츠만을 제공합니다.
            </p>
          </footer>
        </section>

        <section className="build-action">
          {message && <p>{message}</p>}

          <button
            type="button"
            disabled={selectedCount !== MAX_ROSTER_SIZE || isSubmitting}
            onClick={handleSubmit}
          >
            내 로스터 보기{' '}
            <span>
              {selectedCount}/{MAX_ROSTER_SIZE}
            </span>
          </button>
        </section>
      </section>
    </main>
  )
}