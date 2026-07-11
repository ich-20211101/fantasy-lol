import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import './StarterPage.css'

import { getMyTeam, saveStarters } from '../api/teams'
import BottomNav from '../components/BottomNav'
import LanguageToggle from '../components/LanguageToggle'

const POSITIONS = ['Top', 'Jungle', 'Mid', 'Bot', 'Support']

const POS_LABEL = {
  Top: 'TOP',
  Jungle: 'JUG',
  Mid: 'MID',
  Bot: 'ADC',
  Support: 'SPT',
}

function getIsoWeek(date) {
  const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()))
  const dayNum = d.getUTCDay() || 7
  d.setUTCDate(d.getUTCDate() + 4 - dayNum)
  const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1))
  return Math.ceil((((d - yearStart) / 86400000) + 1) / 7)
}

function getSecondsUntilNextMonday() {
  const now = new Date()
  const next = new Date(now)
  const daysUntilMonday = (8 - now.getDay()) % 7 || 7
  next.setDate(now.getDate() + daysUntilMonday)
  next.setHours(0, 0, 0, 0)
  return Math.max(0, Math.floor((next - now) / 1000))
}

function scoreValue(player) {
  return parseInt(String(player.lastSeasonScore ?? '0').replace(/[^0-9]/g, ''), 10) || 0
}

export default function StarterPage({ user, onLogout, onTeamUpdated }) {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()

  const [team, setTeam] = useState(null)
  const [starterIds, setStarterIds] = useState(new Set())
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [message, setMessage] = useState('')
  const [countdown, setCountdown] = useState(getSecondsUntilNextMonday)

  useEffect(() => {
    getMyTeam().then(data => {
      if (!data) return
      setTeam(data)
      setStarterIds(new Set(data.roster.filter(p => p.isStarter).map(p => p.playerId)))
    })
  }, [])

  useEffect(() => {
    const timer = setInterval(() => {
      setCountdown(prev => (prev > 0 ? prev - 1 : 0))
    }, 1000)
    return () => clearInterval(timer)
  }, [])

  const weekNumber = useMemo(() => getIsoWeek(new Date()), [])

  const countdownStr = useMemo(() => {
    const DAY = 86400
    if (countdown <= DAY) {
      const pad = (n) => String(n).padStart(2, '0')
      return `${pad(Math.floor(countdown / 3600))}:${pad(Math.floor((countdown % 3600) / 60))}:${pad(countdown % 60)}`
    }
    const days = Math.ceil(countdown / DAY)
    return t('myTeam.daysLeft', { count: days })
  }, [countdown, i18n.language, t])

  const startingSlots = useMemo(() => {
    if (!team) return []

    return POSITIONS.map(position => {
      const player = team.roster.find(p => starterIds.has(p.playerId) && p.position === position)
      return { position, player }
    })
  }, [team, starterIds])

  const benchList = useMemo(() => {
    if (!team) return []

    const posOrder = Object.fromEntries(POSITIONS.map((pos, index) => [pos, index]))

    return team.roster
      .filter(p => !starterIds.has(p.playerId))
      .sort((a, b) => (
        (posOrder[a.position] - posOrder[b.position]) ||
        (scoreValue(b) - scoreValue(a)) ||
        a.playerName.localeCompare(b.playerName)
      ))
  }, [team, starterIds])

  const lineupComplete = startingSlots.length === POSITIONS.length && startingSlots.every(slot => slot.player)

  const promote = (playerId) => {
    const player = team.roster.find(p => p.playerId === playerId)
    if (!player) return

    setStarterIds(prev => {
      const next = new Set(prev)
      team.roster.forEach(p => {
        if (next.has(p.playerId) && p.position === player.position) next.delete(p.playerId)
      })
      next.add(playerId)
      return next
    })
  }

  const demote = (playerId) => {
    setStarterIds(prev => {
      const next = new Set(prev)
      next.delete(playerId)
      return next
    })
  }

  const autoFill = () => {
    setStarterIds(prev => {
      const next = new Set(prev)

      POSITIONS.forEach(position => {
        const hasStarter = team.roster.some(p => next.has(p.playerId) && p.position === position)
        if (hasStarter) return

        const candidates = team.roster
          .filter(p => !next.has(p.playerId) && p.position === position)
          .sort((a, b) => scoreValue(b) - scoreValue(a))

        if (candidates[0]) next.add(candidates[0].playerId)
      })

      return next
    })
  }

  const handleConfirm = async () => {
    if (!lineupComplete || isSubmitting) return

    try {
      setIsSubmitting(true)
      setMessage('')

      await saveStarters({ teamId: team.teamId, playerIds: [...starterIds] })
      await onTeamUpdated?.()

      navigate('/')
    } catch (error) {
      console.error(error)
      setMessage(error.message || t('setLineup.errorGeneric'))
    } finally {
      setIsSubmitting(false)
    }
  }

  if (!team) {
    return <div style={{ textAlign: 'center', marginTop: 100 }}>{t('setLineup.loading')}</div>
  }

  return (
    <main className="setlineup-page">
      <section className="setlineup-frame">
        <header className="setlineup-header">
          <button type="button" className="setlineup-back" onClick={() => navigate('/')}>
            <svg width="9" height="15" viewBox="0 0 9 15" fill="none">
              <path d="M8 1L1.5 7.5L8 14" stroke="#0b0b0c" strokeWidth="1.8" strokeLinecap="round" />
            </svg>
          </button>
          <span className="setlineup-header-title">{t('setLineup.headerTitle')}</span>
        </header>

        <section className="setlineup-week">
          <div className="setlineup-week-info">
            <span className="setlineup-week-label">{t('setLineup.week', { week: weekNumber })}</span>
            <span className="setlineup-week-countdown">{countdownStr}</span>
          </div>
          <button type="button" className="setlineup-autofill" onClick={autoFill}>
            {t('setLineup.autoFill')}
          </button>
        </section>

        <section className="setlineup-scroll">
          {startingSlots.map(slot => (
            slot.player ? (
              <div className="setlineup-player" key={slot.position}>
                <span className="setlineup-info">
                  <svg width="4" height="11" viewBox="0 0 4 11" fill="#6a6a6f">
                    <circle cx="2" cy="1.4" r="1.4" />
                    <rect x="1.1" y="3.7" width="1.8" height="7" rx="0.9" />
                  </svg>
                </span>
                <div className="setlineup-player-main">
                  <div className="setlineup-player-title">
                    <span>{slot.player.playerName}</span>
                    <span className="setlineup-player-score">
                      {slot.player.lastSeasonScore ?? '0,321'}
                    </span>
                  </div>
                  <p>{slot.player.teamName} | {POS_LABEL[slot.player.position]}</p>
                </div>
                <strong className="setlineup-point">10P</strong>
                <button
                  type="button"
                  className="setlineup-demote"
                  title={t('setLineup.demoteTooltip')}
                  onClick={() => demote(slot.player.playerId)}
                >
                  <svg width="13" height="13" viewBox="0 0 14 14" fill="none">
                    <path d="M7 2v9M3.2 7.4L7 11.2l3.8-3.8" stroke="#0b0b0c" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </button>
              </div>
            ) : (
              <div className="setlineup-slot-empty" key={slot.position}>
                <span>{POS_LABEL[slot.position]}</span>
              </div>
            )
          ))}

          <div className="setlineup-divider" />
          <div className="setlineup-bench-row">
            <span className="setlineup-bench-label">{t('myTeam.benchLabel')}</span>
          </div>

          {benchList.map(player => (
            <div className="setlineup-bench-player" key={player.playerId}>
              <span className="setlineup-info">
                <svg width="4" height="11" viewBox="0 0 4 11" fill="#6a6a6f">
                  <circle cx="2" cy="1.4" r="1.4" />
                  <rect x="1.1" y="3.7" width="1.8" height="7" rx="0.9" />
                </svg>
              </span>
              <div className="setlineup-player-main">
                <div className="setlineup-player-title">
                  <span>{player.playerName}</span>
                  <span className="setlineup-player-score">
                    {player.lastSeasonScore ?? '0,321'}
                  </span>
                </div>
                <p>{player.teamName} | {POS_LABEL[player.position]}</p>
              </div>
              <strong className="setlineup-point">10P</strong>
              <button
                type="button"
                className="setlineup-promote"
                title={t('setLineup.promoteTooltip')}
                onClick={() => promote(player.playerId)}
              >
                <svg width="13" height="13" viewBox="0 0 14 14" fill="none">
                  <path d="M7 12V3M3.2 6.6L7 2.8l3.8 3.8" stroke="#6a6a6f" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </button>
            </div>
          ))}

          <footer className="setlineup-footer">
            <div>
              <span>{t('common.privacyPolicy')}</span>
              <span style={{ color: '#9a9a9e' }}>{t('common.scorePolicy')}</span>
              <span style={{ color: '#9a9a9e' }}>{t('common.contactUs')}</span>
              <LanguageToggle />
            </div>
            <p>
              {t('common.disclaimer')}
            </p>
            <div className="setlineup-footer-wordmark">{t('common.wordmark')}</div>
          </footer>
        </section>

        <section className="setlineup-action">
          {message && <p className="setlineup-error">{message}</p>}
          <button
            type="button"
            disabled={!lineupComplete || isSubmitting}
            onClick={handleConfirm}
          >
            {t('setLineup.confirmBtn')}
          </button>
        </section>

        <BottomNav user={user} onLogout={onLogout} />
      </section>
    </main>
  )
}
