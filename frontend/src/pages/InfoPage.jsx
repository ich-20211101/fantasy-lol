import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { getPlayerRankings } from '../api/players'
import { getWeekMatches } from '../api/matches'
import Header from '../components/Header'
import Footer from '../components/Footer'
import BottomNav from '../components/BottomNav'
import { useProTeamAbbreviations, abbreviateTeam } from '../hooks/useProTeamAbbreviations'
import { PLAYER_SCORE_FORMULA } from '../constants/scoreFormula'
import './InfoPage.css'

const POSITIONS = ['ALL', 'TOP', 'JUG', 'MID', 'ADC', 'SPT']

const POSITION_TO_API_VALUE = {
  ALL: 'ALL',
  TOP: 'Top',
  JUG: 'Jungle',
  MID: 'Mid',
  ADC: 'Bot',
  SPT: 'Support',
}

const DOW_LABELS = ['일', '월', '화', '수', '목', '금', '토']
const MAX_WEEK_DAYS = 5

function dayKeyFromKst(kst) {
  return `${kst.getUTCFullYear()}-${String(kst.getUTCMonth() + 1).padStart(2, '0')}-${String(kst.getUTCDate()).padStart(2, '0')}`
}

function dateLabelFromKst(kst) {
  return `${String(kst.getUTCMonth() + 1).padStart(2, '0')}/${String(kst.getUTCDate()).padStart(2, '0')}`
}

function toKst(dateTimeUtc) {
  const parsed = new Date(dateTimeUtc.replace(' ', 'T') + 'Z')
  const kst = new Date(parsed.getTime() + 9 * 60 * 60 * 1000)

  return {
    dayKey: dayKeyFromKst(kst),
    date: dateLabelFromKst(kst),
    dow: DOW_LABELS[kst.getUTCDay()],
    time: `${String(kst.getUTCHours()).padStart(2, '0')}:${String(kst.getUTCMinutes()).padStart(2, '0')}`,
  }
}

// 오늘부터 시작해서 경기 있는 날만, 최대 MAX_WEEK_DAYS개까지
function buildUpcomingDays(weekMatches) {
  const nowKst = new Date(Date.now() + 9 * 60 * 60 * 1000)

  const days = Array.from({ length: 7 }, (_, i) => {
    const d = new Date(nowKst)
    d.setUTCDate(nowKst.getUTCDate() + i)

    return {
      key: dayKeyFromKst(d),
      date: dateLabelFromKst(d),
      dow: DOW_LABELS[d.getUTCDay()],
      matches: [],
    }
  })

  const byKey = new Map(days.map((d) => [d.key, d]))

  weekMatches.forEach((m) => {
    const { dayKey, time } = toKst(m.dateTimeUtc)
    byKey.get(dayKey)?.matches.push({ ...m, time })
  })

  days.forEach((d) => d.matches.sort((a, b) => a.time.localeCompare(b.time)))

  return days.filter((d) => d.matches.length > 0).slice(0, MAX_WEEK_DAYS)
}

export default function InfoPage({ user, team }) {
  const { t } = useTranslation()
  const scrollRef = useRef(null)
  const teamAbbreviations = useProTeamAbbreviations()

  const [rankInfoOpen, setRankInfoOpen] = useState(false)
  const [mineOnly, setMineOnly] = useState(false)
  const [activeTab, setActiveTab] = useState('ALL')

  const [rows, setRows] = useState([])
  const [seasonLabel, setSeasonLabel] = useState(null)
  const [page, setPage] = useState(1)
  const [hasMore, setHasMore] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [tallying, setTallying] = useState(false)
  const [showToTop, setShowToTop] = useState(false)

  const [weekMatches, setWeekMatches] = useState([])
  const [weekIndex, setWeekIndex] = useState(0)
  const weekScrollRef = useRef(null)

  useEffect(() => {
    getWeekMatches().then((data) => {
      if (data) setWeekMatches(data)
    })
  }, [])

  // 목록은 이미 오늘부터 시작하도록 정렬되어 있으므로 초기 위치는 0
  const weekDays = useMemo(() => buildUpcomingDays(weekMatches), [weekMatches])

  const goToWeekIndex = (i) => {
    const clamped = Math.max(0, Math.min(weekDays.length - 1, i))
    setWeekIndex(clamped)

    const el = weekScrollRef.current
    if (el) el.scrollTo({ left: clamped * el.clientWidth, behavior: 'smooth' })
  }

  const handleWeekScroll = () => {
    const el = weekScrollRef.current
    if (!el || el.clientWidth === 0) return

    const idx = Math.round(el.scrollLeft / el.clientWidth)
    setWeekIndex((prev) => (prev === idx ? prev : idx))
  }

  const loadPage = useCallback(async (pageToLoad, position) => {
    setLoadingMore(true)

    try {
      const data = await getPlayerRankings({ position: POSITION_TO_API_VALUE[position], page: pageToLoad })

      if (!data) {
        setHasMore(false)
        return
      }

      setTallying(Boolean(data.tallying))
      setRows((prev) => (pageToLoad === 1 ? (data.rows ?? []) : [...prev, ...(data.rows ?? [])]))
      setHasMore(Boolean(data.hasMore))
      setSeasonLabel(data.seasonLabel ?? null)
    } finally {
      setLoadingMore(false)
    }
  }, [])

  useEffect(() => {
    setPage(1)
    setHasMore(true)
    loadPage(1, activeTab)
  }, [activeTab, loadPage])

  const rosterPlayerIds = useMemo(
    () => new Set((team?.roster ?? []).map((r) => r.playerId)),
    [team],
  )

  const visibleRows = useMemo(() => {
    const withMine = rows.map((r) => ({ ...r, mine: rosterPlayerIds.has(r.playerId) }))
    return mineOnly ? withMine.filter((r) => r.mine) : withMine
  }, [rows, mineOnly, rosterPlayerIds])

  const handleScroll = useCallback(() => {
    const el = scrollRef.current
    if (!el) return

    setShowToTop(el.scrollTop > 400)

    const nearBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - 120

    if (nearBottom && !loadingMore && hasMore) {
      const nextPage = page + 1
      setPage(nextPage)
      loadPage(nextPage, activeTab)
    }
  }, [loadingMore, hasMore, page, loadPage, activeTab])

  const scrollToTop = () => {
    scrollRef.current?.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <main className="info-page">
      <section className="info-frame">
        {rankInfoOpen && (
          <div className="info-popover-overlay" onClick={() => setRankInfoOpen(false)}>
            <div className="info-popover" onClick={(e) => e.stopPropagation()}>
              <div className="info-popover-title">{t('info.rankInfoTitle')}</div>
              <p className="info-popover-desc">{t('info.rankInfoDesc')}</p>
              <p className="info-popover-formula">{PLAYER_SCORE_FORMULA}</p>
              <button type="button" className="info-popover-confirm" onClick={() => setRankInfoOpen(false)}>
                {t('info.rankInfoConfirm')}
              </button>
            </div>
          </div>
        )}

        <Header variant="logo" />

        <div className="info-scroll" ref={scrollRef} onScroll={handleScroll}>
          {weekDays.length > 0 && (
            <div className="info-week">
              <div className="info-week-scroll" ref={weekScrollRef} onScroll={handleWeekScroll}>
                {weekDays.map((day) => (
                  <div className="info-week-day" key={day.key}>
                    <div className="info-week-day-label">
                      <div className="info-week-date">{day.date}</div>
                      <div className="info-week-dow">{day.dow}</div>
                    </div>
                    <div className="info-week-matches">
                      {day.matches.map((m, i) => {
                        const started = m.team1Score + m.team2Score > 0

                        return (
                          <div className="info-week-match" key={i}>
                            <span className="info-week-match-time">{m.time}</span>
                            <div className="info-week-match-team">
                              <div className="info-week-match-team-name">{abbreviateTeam(teamAbbreviations, m.team1)}</div>
                              <div className="info-week-match-score">{started ? m.team1Score : '-'}</div>
                            </div>
                            <span className="info-week-match-vs">vs</span>
                            <div className="info-week-match-team">
                              <div className="info-week-match-team-name">{abbreviateTeam(teamAbbreviations, m.team2)}</div>
                              <div className="info-week-match-score">{started ? m.team2Score : '-'}</div>
                            </div>
                          </div>
                        )
                      })}
                    </div>
                  </div>
                ))}
              </div>

              <div className="info-week-nav">
                <span
                  className="info-week-arrow"
                  onClick={() => goToWeekIndex(weekIndex - 1)}
                  style={{ opacity: weekIndex === 0 ? 0.3 : 1 }}
                >
                  <svg width="5" height="8" viewBox="0 0 6 10" fill="none">
                    <path d="M5 1L1 5l4 4" stroke="#6a6a6f" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </span>
                {weekDays.map((day, i) => (
                  <span key={day.key} className={`info-week-dot ${i === weekIndex ? 'active' : ''}`} />
                ))}
                <span
                  className="info-week-arrow"
                  onClick={() => goToWeekIndex(weekIndex + 1)}
                  style={{ opacity: weekIndex === weekDays.length - 1 ? 0.3 : 1 }}
                >
                  <svg width="5" height="8" viewBox="0 0 6 10" fill="none">
                    <path d="M1 1l4 4-4 4" stroke="#6a6a6f" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </span>
              </div>

              <div className="info-week-divider" />
            </div>
          )}

          {seasonLabel && <div className="info-season-label">{seasonLabel}</div>}

          <div className="info-toolbar">
            <div className="info-ranking-title-group">
              <span className="info-ranking-title">Player Ranking</span>
              <button type="button" className="info-rank-info-btn" onClick={() => setRankInfoOpen(true)} aria-label="info">
                <svg width="3" height="8.2" viewBox="0 0 4 11" fill="#6a6a6f">
                  <circle cx="2" cy="1.4" r="1.4" />
                  <rect x="1.1" y="3.7" width="1.8" height="7" rx="0.9" />
                </svg>
              </button>
            </div>

            {user && (
              <div className="info-mine-toggle" onClick={() => setMineOnly((prev) => !prev)}>
                <span className={`info-mine-checkbox ${mineOnly ? 'checked' : ''}`}>
                  <svg width="10" height="8" viewBox="0 0 11 9" fill="none" style={{ display: mineOnly ? 'block' : 'none' }}>
                    <path d="M1 4.5L4 7.5L10 1.2" stroke="#f8f9fa" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </span>
                <span className="info-mine-label">{t('info.mineOnly')}</span>
              </div>
            )}
          </div>

          <div className="info-tabs">
            {POSITIONS.map((pos) => (
              <div
                key={pos}
                className={`info-tab ${pos === activeTab ? 'active' : ''}`}
                onClick={() => setActiveTab(pos)}
              >
                <span className="info-tab-label">{pos}</span>
              </div>
            ))}
          </div>

          {tallying ? (
            <div className="info-tallying-empty">
              <span className="info-tallying-title">{t('leaderboard.tallyingTitle')}</span>
              <span className="info-tallying-desc">
                {t('leaderboard.tallyingDescLine1')}
                <br />
                {t('leaderboard.tallyingDescLine2')}
              </span>
            </div>
          ) : (
            <div className="info-rows">
              {visibleRows.map((row, i) => (
                <div key={`${row.playerId}-${i}`} className="info-row">
                  <span className="info-row-rank">{row.rank}</span>
                  <div className="info-row-info">
                    <div className="info-row-name">{row.name}</div>
                    <div className="info-row-sub">
                      {abbreviateTeam(teamAbbreviations, row.team)} | {row.pos}
                    </div>
                  </div>
                  <span className="info-row-score">{row.score?.toFixed(1)}</span>
                </div>
              ))}

              {mineOnly && visibleRows.length === 0 && (
                <div className="info-rows-empty">{t('info.rowsEmpty')}</div>
              )}

              {loadingMore && (
                <div className="info-loading">
                  <div className="info-spinner" />
                </div>
              )}
            </div>
          )}

          <Footer marginTop="20px" padding="20px 22px 40px" />
        </div>

        {showToTop && (
          <button type="button" className="info-scroll-to-top" onClick={scrollToTop} aria-label="scroll to top">
            <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
              <path d="M9 14V4M9 4L4 9M9 4l5 5" stroke="#f8f9fa" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </button>
        )}

        <BottomNav />
      </section>
    </main>
  )
}
