import { useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import './MyTeamPage.css'

import BottomNav from '../components/BottomNav'
import LanguageToggle from '../components/LanguageToggle'
import { deleteMyTeam } from '../api/teams'

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

const RANK_POPUP_STORAGE_KEY = 'lfm_rank_popup_dismissed_at'
const RANK_POPUP_SUPPRESS_DAYS = 3

function isRankPopupSuppressed() {
  const dismissedAt = Number(localStorage.getItem(RANK_POPUP_STORAGE_KEY))
  if (!dismissedAt) return false

  const elapsedMs = Date.now() - dismissedAt
  return elapsedMs < RANK_POPUP_SUPPRESS_DAYS * 24 * 60 * 60 * 1000
}

export default function MyTeamPage({ user, team, onLogout, onTeamDeleted }) {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const scrollRef = useRef(null)

  const [rankPopupOpen, setRankPopupOpen] = useState(() => !isRankPopupSuppressed())
  const [rankDontShow, setRankDontShow] = useState(false)
  const [collapsed, setCollapsed] = useState(false)
  const [countdown, setCountdown] = useState(getSecondsUntilNextMonday)
  const [isDeleting, setIsDeleting] = useState(false)

  useEffect(() => {
    const timer = setInterval(() => {
      setCountdown(prev => (prev > 0 ? prev - 1 : 0))
    }, 1000)
    return () => clearInterval(timer)
  }, [])

  useEffect(() => {
    const el = scrollRef.current
    if (!el) return

    const onScroll = () => setCollapsed(el.scrollTop > 24)
    el.addEventListener('scroll', onScroll, { passive: true })
    return () => el.removeEventListener('scroll', onScroll)
  }, [])

  const startersByPosition = useMemo(() => {
    const map = {}
    team.roster.forEach(player => {
      if (player.isStarter) map[player.position] = player
    })
    return map
  }, [team.roster])

  const bench = useMemo(
    () => team.roster.filter(player => !player.isStarter),
    [team.roster]
  )

  const closeRankPopup = () => {
    if (rankDontShow) {
      localStorage.setItem(RANK_POPUP_STORAGE_KEY, String(Date.now()))
    }
    setRankPopupOpen(false)
  }

  const countdownStr = useMemo(() => {
    const DAY = 86400
    if (countdown <= DAY) {
      const pad = (n) => String(n).padStart(2, '0')
      return `${pad(Math.floor(countdown / 3600))}:${pad(Math.floor((countdown % 3600) / 60))}:${pad(countdown % 60)}`
    }
    const days = Math.ceil(countdown / DAY)
    return t('myTeam.daysLeft', { count: days })
  }, [countdown, i18n.language, t])

  const weekNumber = useMemo(() => getIsoWeek(new Date()), [])

  // [TEST] 테스트 편의용 — 나중에 제거 예정
  const handleDeleteTeam = async () => {
    if (isDeleting) return
    if (!window.confirm('[TEST] 로스터를 삭제하고 처음부터 다시 만드시겠어요?')) return

    try {
      setIsDeleting(true)
      await deleteMyTeam()
      await onTeamDeleted?.()
    } catch (error) {
      console.error(error)
      window.alert('삭제 중 오류가 발생했습니다.')
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <main className="myteam-page">
      <section className="myteam-frame">
        {rankPopupOpen && (
          <div className="myteam-rank-overlay">
            <div className="myteam-rank-modal">
              <p className="myteam-rank-eyebrow">{t('myTeam.rankEyebrowRound')}</p>
              <p className="myteam-rank-eyebrow">{t('myTeam.rankEyebrowWeek', { week: weekNumber - 1 })}</p>
              <h2 className="myteam-rank-title">
                {t('myTeam.rankTitleLine1')}<br />{t('myTeam.rankTitleLine2')}
              </h2>
              <p className="myteam-rank-desc">
                {t('myTeam.rankDescLine1')}<br />{t('myTeam.rankDescLine2')}
              </p>
              <label className="myteam-rank-check">
                <span
                  className="myteam-rank-checkbox"
                  style={{ background: rankDontShow ? '#0b0b0c' : 'transparent' }}
                >
                  {rankDontShow && (
                    <svg width="11" height="9" viewBox="0 0 11 9" fill="none">
                      <path d="M1 4.5L4 7.5L10 1.2" stroke="#f8f9fa" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  )}
                </span>
                <input
                  type="checkbox"
                  checked={rankDontShow}
                  onChange={() => setRankDontShow(prev => !prev)}
                  style={{ position: 'absolute', opacity: 0, width: 0, height: 0 }}
                />
                {t('myTeam.rankDontShow')}
              </label>
              <div className="myteam-rank-actions">
                <button type="button" className="myteam-rank-close" onClick={closeRankPopup}>
                  {t('myTeam.rankClose')}
                </button>
                <button type="button" className="myteam-rank-view" onClick={closeRankPopup}>
                  {t('myTeam.rankView')}
                </button>
              </div>
            </div>
          </div>
        )}

        <header className="myteam-header">
          <div className="lfm-brand-wrap">
            <span className="lfm-logo">LFM</span>
            <span className="lfm-logo-line" />
            <span className="lfm-brand">
              LoL Fantasy<br />Maker
            </span>
          </div>
        </header>

        <div className={`myteam-title ${collapsed ? 'collapsed' : ''}`}>
          <div className="myteam-title-label">{t('myTeam.rankEyebrowRound')}</div>
          <div className="myteam-title-row">
            <span className="myteam-title-week">{t('myTeam.rankEyebrowWeek', { week: weekNumber })}</span>
            <span className="myteam-title-countdown">{countdownStr}</span>
          </div>
        </div>

        <div className="myteam-scroll" ref={scrollRef}>
          <div className="myteam-team-row">
            <div className="myteam-team-label">{t('myTeam.teamLabel')}</div>
            <div className="myteam-team-name">{team.teamName}</div>
          </div>

          {POSITIONS.map(position => {
            const player = startersByPosition[position]

            return player ? (
              <div className="myteam-player" key={position}>
                <span className="myteam-info">
                  <svg width="4" height="11" viewBox="0 0 4 11" fill="#6a6a6f">
                    <circle cx="2" cy="1.4" r="1.4" />
                    <rect x="1.1" y="3.7" width="1.8" height="7" rx="0.9" />
                  </svg>
                </span>
                <div className="myteam-player-main">
                  <div className="myteam-player-title">
                    <span>{player.playerName}</span>
                    <span className="myteam-player-score">
                      {player.lastSeasonScore ?? '0,321'}
                    </span>
                  </div>
                  <p>{player.teamName} | {POS_LABEL[player.position]}</p>
                </div>
                <span className="myteam-point">10P</span>
              </div>
            ) : (
              <div className="myteam-slot-empty" key={position}>
                <span>{POS_LABEL[position]}</span>
              </div>
            )
          })}

          <div className="myteam-divider" />
          <div className="myteam-bench-label">{t('myTeam.benchLabel')}</div>

          {bench.map(player => (
            <div className="myteam-player" key={player.playerId}>
              <span className="myteam-info">
                <svg width="4" height="11" viewBox="0 0 4 11" fill="#6a6a6f">
                  <circle cx="2" cy="1.4" r="1.4" />
                  <rect x="1.1" y="3.7" width="1.8" height="7" rx="0.9" />
                </svg>
              </span>
              <div className="myteam-player-main">
                <div className="myteam-player-title">
                  <span>{player.playerName}</span>
                  <span className="myteam-player-score">
                    {player.lastSeasonScore ?? '0,321'}
                  </span>
                </div>
                <p>{player.teamName} | {POS_LABEL[player.position]}</p>
              </div>
              <span className="myteam-point">10P</span>
            </div>
          ))}

          <footer className="myteam-footer">
            <div className="myteam-footer-links">
              <span>{t('common.privacyPolicy')}</span>
              <span style={{ color: '#9a9a9e' }}>{t('common.scorePolicy')}</span>
              <span style={{ color: '#9a9a9e' }}>{t('common.contactUs')}</span>
              <LanguageToggle />
            </div>
            <p>
              {t('common.disclaimer')}
            </p>
            <div className="myteam-footer-wordmark">{t('common.wordmark')}</div>
          </footer>

          {/* [TEST] 테스트 편의용 — 나중에 제거 예정 */}
          <button
            type="button"
            onClick={handleDeleteTeam}
            disabled={isDeleting}
            style={{
              marginTop: 4,
              marginBottom: 16,
              padding: '10px',
              border: '1px dashed #d92d20',
              borderRadius: 10,
              background: 'transparent',
              color: '#d92d20',
              fontSize: 12,
              fontWeight: 700,
              cursor: isDeleting ? 'not-allowed' : 'pointer',
            }}
          >
            [TEST] 내 로스터 삭제{isDeleting ? '...' : ''}
          </button>
        </div>

        <div className="myteam-cta-area">
          <button type="button" className="myteam-cta" onClick={() => navigate('/starters')}>
            {t('myTeam.ctaLabel')}
          </button>
        </div>

        <BottomNav user={user} onLogout={onLogout} />
      </section>
    </main>
  )
}
