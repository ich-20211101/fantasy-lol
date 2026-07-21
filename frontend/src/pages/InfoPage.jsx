import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { getPlayerRankings } from '../api/players'
import { getUpcomingMatches } from '../api/matches'
import Header from '../components/Header'
import Footer from '../components/Footer'
import BottomNav from '../components/BottomNav'
import { useProTeamAbbreviations, abbreviateTeam } from '../hooks/useProTeamAbbreviations'
import './InfoPage.css'

const POSITIONS = ['ALL', 'TOP', 'JUG', 'MID', 'ADC', 'SPT']
const SEASON_LABEL = '2026 Round 3 Week8'

function toKst(dateTimeUtc) {
  const parsed = new Date(dateTimeUtc.replace(' ', 'T') + 'Z')
  const kst = new Date(parsed.getTime() + 9 * 60 * 60 * 1000)

  return {
    date: `${kst.getUTCMonth() + 1}월 ${kst.getUTCDate()}일`,
    time: `${String(kst.getUTCHours()).padStart(2, '0')}:${String(kst.getUTCMinutes()).padStart(2, '0')}`,
  }
}

export default function InfoPage({ team }) {
  const { t } = useTranslation()
  const scrollRef = useRef(null)
  const teamAbbreviations = useProTeamAbbreviations()

  const [rankInfoOpen, setRankInfoOpen] = useState(false)
  const [mineOnly, setMineOnly] = useState(false)
  const [activeTab, setActiveTab] = useState('ALL')

  const [rows, setRows] = useState([])
  const [page, setPage] = useState(1)
  const [hasMore, setHasMore] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [tallying, setTallying] = useState(false)
  const [showToTop, setShowToTop] = useState(false)

  const [upcomingMatches, setUpcomingMatches] = useState([])

  useEffect(() => {
    getUpcomingMatches().then((data) => {
      if (data && data.length) setUpcomingMatches(data.slice(0, 2))
    })
  }, [])

  const loadPage = useCallback(async (pageToLoad, position) => {
    setLoadingMore(true)

    try {
      const data = await getPlayerRankings({ position, page: pageToLoad })

      if (!data) {
        setHasMore(false)
        return
      }

      setTallying(Boolean(data.tallying))
      setRows((prev) => (pageToLoad === 1 ? (data.rows ?? []) : [...prev, ...(data.rows ?? [])]))
      setHasMore(Boolean(data.hasMore))
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

  const upcomingDate = upcomingMatches[0] ? toKst(upcomingMatches[0].dateTimeUtc).date : null

  return (
    <main className="info-page">
      <section className="info-frame">
        {rankInfoOpen && (
          <div className="info-popover-overlay" onClick={() => setRankInfoOpen(false)}>
            <div className="info-popover" onClick={(e) => e.stopPropagation()}>
              <div className="info-popover-title">{t('info.rankInfoTitle')}</div>
              <p className="info-popover-desc">{t('info.rankInfoDesc')}</p>
              <button type="button" className="info-popover-confirm" onClick={() => setRankInfoOpen(false)}>
                {t('info.rankInfoConfirm')}
              </button>
            </div>
          </div>
        )}

        <Header variant="logo" />

        <div className="info-scroll" ref={scrollRef} onScroll={handleScroll}>
          {upcomingMatches.length > 0 && (
            <div className="info-upcoming">
              <div className="info-upcoming-header">
                <span className="info-upcoming-label">Upcoming</span>
                <span className="info-upcoming-date">{upcomingDate}</span>
              </div>
              <div className="info-upcoming-matches">
                {upcomingMatches.map((m, i) => {
                  const { time } = toKst(m.dateTimeUtc)

                  return (
                    <div className="info-upcoming-match" key={i}>
                      <div className="info-upcoming-teams">
                        {m.team1} <span className="info-upcoming-vs">vs</span> {m.team2}
                      </div>
                      <div className="info-upcoming-time">{time}</div>
                    </div>
                  )
                })}
              </div>
              <div className="info-upcoming-divider" />
            </div>
          )}

          <div className="info-season-label">{SEASON_LABEL}</div>

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

            <div className="info-mine-toggle" onClick={() => setMineOnly((prev) => !prev)}>
              <span className={`info-mine-checkbox ${mineOnly ? 'checked' : ''}`}>
                <svg width="10" height="8" viewBox="0 0 11 9" fill="none" style={{ display: mineOnly ? 'block' : 'none' }}>
                  <path d="M1 4.5L4 7.5L10 1.2" stroke="#f8f9fa" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </span>
              <span className="info-mine-label">{t('info.mineOnly')}</span>
            </div>
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
                  <span className="info-row-rank">{i + 1}</span>
                  <div className="info-row-info">
                    <div className="info-row-name">{row.name}</div>
                    <div className="info-row-sub">{abbreviateTeam(teamAbbreviations, row.team)} | {row.pos}</div>
                  </div>
                  <span className="info-row-score">{row.score?.toLocaleString()}</span>
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
