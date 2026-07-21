import { useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import Header from '../components/Header'
import Footer from '../components/Footer'
import { useProTeamAbbreviations, abbreviateTeam } from '../hooks/useProTeamAbbreviations'
import { POSITIONS, POS_LABEL } from '../constants/positions'
import './MyRosterPage.css'

const MAX_ROSTER_SIZE = 8
const PLAYER_POINT = 10
const TOTAL_POINT = 80

export default function MyRosterPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()
  const teamAbbreviations = useProTeamAbbreviations()
  const initialPlayers = location.state?.selectedPlayers

  const [selectedPlayers, setSelectedPlayers] = useState(initialPlayers ?? [])

  const slots = useMemo(() => {
    return POSITIONS.map(position => ({
      position,
      players: selectedPlayers.filter(player => player.position === position),
    }))
  }, [selectedPlayers])

  const selectedCount = selectedPlayers.length
  const remainingPoint = TOTAL_POINT - selectedCount * PLAYER_POINT
  const isComplete = selectedCount === MAX_ROSTER_SIZE

  if (!initialPlayers) {
    navigate('/roster')
    return null
  }

  const removePlayer = (playerId) => {
    setSelectedPlayers(prev => prev.filter(player => player.playerId !== playerId))
  }

  const handleSelectPlayers = () => {
    navigate('/roster', { state: { selectedPlayers } })
  }

  const handleBuyRoster = () => {
    if (!isComplete) return
    navigate('/roster/register', { state: { selectedPlayers } })
  }

  return (
    <main className="myroster-page">
      <section className="myroster-frame">
        <Header
          variant="back"
          title={t('myRoster.headerTitle')}
          onBack={() => navigate('/roster', { state: { selectedPlayers } })}
        />

        <section className="myroster-round">
          <span>{t('common.round')}</span>
          <span className="myroster-round-spacer" aria-hidden="true">40 P</span>
        </section>

        <section className="myroster-scroll">
          <div className="myroster-dashboard">
            <div className="myroster-dashboard-cell">
              <div className="myroster-dashboard-label">{t('myRoster.squadStatus')}</div>
              <div className="myroster-dashboard-value">
                {selectedCount} <span>/ {MAX_ROSTER_SIZE}</span>
              </div>
            </div>
            <div className="myroster-dashboard-divider" />
            <div className="myroster-dashboard-cell right">
              <div className="myroster-dashboard-label">{t('myRoster.remaining')}</div>
              <div className="myroster-dashboard-value">
                {remainingPoint} <span>P</span>
              </div>
            </div>
          </div>

          {slots.map(slot => (
            <div className="myroster-slot" key={slot.position}>
              <div className="myroster-slot-label">{POS_LABEL[slot.position]}</div>

              {slot.players.map(player => (
                <div className="myroster-player" key={player.playerId}>
                  <span className="myroster-info">
                    <svg width="4" height="11" viewBox="0 0 4 11" fill="#6a6a6f">
                      <circle cx="2" cy="1.4" r="1.4" />
                      <rect x="1.1" y="3.7" width="1.8" height="7" rx="0.9" />
                    </svg>
                  </span>

                  <div className="myroster-player-main">
                    <div className="myroster-player-title">
                      <span>{player.playerName}</span>
                      <span className="myroster-player-score">
                        {player.lastSeasonScore ?? '0,321'}
                      </span>
                    </div>
                    <p>
                      {abbreviateTeam(teamAbbreviations, player.teamName)} | {POS_LABEL[player.position]}
                    </p>
                  </div>

                  <strong className="myroster-point">{PLAYER_POINT}P</strong>

                  <button
                    type="button"
                    className="myroster-remove"
                    onClick={() => removePlayer(player.playerId)}
                  >
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                      <path
                        d="M1.6 1.6L10.4 10.4M10.4 1.6L1.6 10.4"
                        stroke="currentColor"
                        strokeWidth="1.9"
                        strokeLinecap="round"
                      />
                    </svg>
                  </button>
                </div>
              ))}

              {slot.players.length === 0 && (
                <div className="myroster-slot-empty">{t('myRoster.selectPlayerPlaceholder')}</div>
              )}
            </div>
          ))}

          <Footer marginTop="20px" padding="24px 4px 40px" />
        </section>

        <section className="myroster-action">
          <button type="button" className="myroster-action-select" onClick={handleSelectPlayers}>
            {t('myRoster.selectPlayersBtn')}
          </button>

          <button
            type="button"
            className="myroster-action-buy"
            disabled={!isComplete}
            onClick={handleBuyRoster}
          >
            {t('myRoster.buyRoster', { point: PLAYER_POINT })}
          </button>
        </section>
      </section>
    </main>
  )
}
