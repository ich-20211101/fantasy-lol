import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { getLeaderboard, getMyScores } from '../api/scores'
import Header from '../components/Header'
import Footer from '../components/Footer'
import BottomNav from '../components/BottomNav'
import './LeaderboardPage.css'

const CURRENT_SEASON_NAME = 'LCK/2026 Season/Rounds 3-4'

const ROUNDS = [
  {
    key: 'r3',
    label: '2026 LCK ROUND3',
    weeks: [
      { key: 'overall', label: 'Overall', weekNumber: null },
      { key: 'w12', label: 'WEEK 12', weekNumber: 12 },
      { key: 'w11', label: 'WEEK 11', weekNumber: 11 },
      { key: 'w10', label: 'WEEK 10', weekNumber: 10 },
    ],
  },
  {
    key: 'r2',
    label: '2026 LCK ROUND2',
    weeks: [
      { key: 'w9', label: 'WEEK 9', weekNumber: 9 },
      { key: 'w8', label: 'WEEK 8', weekNumber: 8 },
    ],
  },
  {
    key: 'r1',
    label: '2026 LCK ROUND1',
    weeks: [
      { key: 'w4', label: 'WEEK 4', weekNumber: 4 },
      { key: 'w3', label: 'WEEK 3', weekNumber: 3 },
    ],
  },
  {
    key: 'cup',
    label: '2026 LCK CUP',
    weeks: [{ key: 'cupf', label: 'Final', weekNumber: null }],
  },
]

const PAGE_SIZE = 20

function rankFontSize(rank) {
  return String(rank).replace(/,/g, '').length >= 4 ? '11px' : '15px'
}

export default function LeaderboardPage({ user, team }) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const scrollRef = useRef(null)

  const [myScore, setMyScore] = useState(null)
  const [weekSelectOpen, setWeekSelectOpen] = useState(false)
  const [expandedRoundKey, setExpandedRoundKey] = useState('r3')
  const [selectedRoundKey, setSelectedRoundKey] = useState('r3')
  const [selectedWeekKey, setSelectedWeekKey] = useState('overall')

  const [rows, setRows] = useState([])
  const [page, setPage] = useState(1)
  const [hasMore, setHasMore] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [tallying, setTallying] = useState(false)
  const [showToTop, setShowToTop] = useState(false)

  const selectedRound = useMemo(
    () => ROUNDS.find((r) => r.key === selectedRoundKey) ?? ROUNDS[0],
    [selectedRoundKey],
  )
  const selectedWeek = useMemo(
    () => selectedRound.weeks.find((w) => w.key === selectedWeekKey) ?? selectedRound.weeks[0],
    [selectedRound, selectedWeekKey],
  )

  const loadPage = useCallback(async (pageToLoad, weekNumber) => {
    setLoadingMore(true)

    try {
      const data = await getLeaderboard(weekNumber, CURRENT_SEASON_NAME, { page: pageToLoad, pageSize: PAGE_SIZE })

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
    loadPage(1, selectedWeek.weekNumber)
  }, [selectedWeek, loadPage])

  useEffect(() => {
    if (!user) {
      setMyScore(null)
      return
    }

    getMyScores().then(setMyScore)
  }, [user])

  const handleScroll = useCallback(() => {
    const el = scrollRef.current
    if (!el) return

    setShowToTop(el.scrollTop > 400)

    const nearBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - 120

    if (nearBottom && !loadingMore && hasMore) {
      const nextPage = page + 1
      setPage(nextPage)
      loadPage(nextPage, selectedWeek.weekNumber)
    }
  }, [loadingMore, hasMore, page, loadPage, selectedWeek])

  const scrollToTop = () => {
    scrollRef.current?.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const toggleWeekSelect = () => setWeekSelectOpen((prev) => !prev)

  const toggleRoundExpand = (key) => {
    setExpandedRoundKey((prev) => (prev === key ? null : key))
  }

  const pickWeek = (roundKey, weekKey) => {
    setSelectedRoundKey(roundKey)
    setSelectedWeekKey(weekKey)
    setWeekSelectOpen(false)
    scrollToTop()
  }

  const goToGoogleLogin = () => navigate('/')

  const hasScoreHistory = Boolean(myScore) && Boolean(team) && (myScore.weeklyScore > 0 || myScore.seasonalScore > 0)

  const myRankLoggedOut = !user
  const myRankNoHistory = Boolean(user) && !hasScoreHistory
  const myRankActive = Boolean(user) && hasScoreHistory

  return (
    <main className="leaderboard-page">
      <section className="leaderboard-frame">
        <Header variant="logo" />

        <div className="leaderboard-week-selector">
          <span className="leaderboard-round-eyebrow">{selectedRound.label}</span>

          <div className="leaderboard-week-toggle" onClick={toggleWeekSelect}>
            <span className="leaderboard-week-label">{selectedWeek.label}</span>
            <svg
              width="15"
              height="9"
              viewBox="0 0 15 9"
              fill="none"
              style={{ transform: weekSelectOpen ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform .18s ease' }}
            >
              <path d="M1 1.2l6.5 6 6.5-6" stroke="#0b0b0c" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </div>

          {weekSelectOpen && (
            <div className="leaderboard-week-dropdown">
              {ROUNDS.map((round) => (
                <div key={round.key}>
                  <div className="leaderboard-round-header" onClick={() => toggleRoundExpand(round.key)}>
                    <span
                      className="leaderboard-round-header-label"
                      style={{ color: round.key === selectedRoundKey ? '#0b0b0c' : '#9a9a9e' }}
                    >
                      {round.label}
                    </span>
                    <svg
                      width="11"
                      height="7"
                      viewBox="0 0 11 7"
                      fill="none"
                      style={{
                        transform: expandedRoundKey === round.key ? 'rotate(180deg)' : 'rotate(0deg)',
                        transition: 'transform .16s ease',
                      }}
                    >
                      <path d="M1 1l4.5 4.5L10 1" stroke="#6a6a6f" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                  </div>

                  {expandedRoundKey === round.key && (
                    <div className="leaderboard-round-weeks">
                      {round.weeks.map((week) => {
                        const isSelected = round.key === selectedRoundKey && week.key === selectedWeekKey

                        return (
                          <div
                            key={week.key}
                            className="leaderboard-week-option"
                            style={{ fontWeight: isSelected ? 700 : 500, color: isSelected ? '#0b0b0c' : '#6a6a6f' }}
                            onClick={() => pickWeek(round.key, week.key)}
                          >
                            {week.label}
                          </div>
                        )
                      })}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="leaderboard-scroll" ref={scrollRef} onScroll={handleScroll}>
          {!tallying && myRankLoggedOut && (
            <div className="leaderboard-my-rank-empty">
              <span>
                <span className="leaderboard-my-rank-login" onClick={goToGoogleLogin}>
                  {t('leaderboard.loginLink')}
                </span>
                {t('leaderboard.loginPrompt')}
              </span>
            </div>
          )}

          {!tallying && myRankNoHistory && (
            <div className="leaderboard-my-rank-empty">
              <span>{t('leaderboard.noHistory')}</span>
            </div>
          )}

          {!tallying && myRankActive && (
            <div className="leaderboard-my-rank-card">
              <span className="leaderboard-row-rank" style={{ fontSize: rankFontSize(myScore?.rank ?? '') }}>
                {myScore?.rank ?? '-'}
              </span>
              <div className="leaderboard-row-info">
                <div className="leaderboard-row-team">{team?.teamName}</div>
                <div className="leaderboard-row-owner">{user?.username}</div>
              </div>
              <span className="leaderboard-row-score">{myScore?.weeklyScore?.toLocaleString()}P</span>
            </div>
          )}

          {tallying ? (
            <div className="leaderboard-tallying-empty">
              <span className="leaderboard-tallying-title">{t('leaderboard.tallyingTitle')}</span>
              <span className="leaderboard-tallying-desc">
                {t('leaderboard.tallyingDescLine1')}
                <br />
                {t('leaderboard.tallyingDescLine2')}
              </span>
            </div>
          ) : (
            <>
              {rows.map((row) => {
                const isTop3 = row.rank <= 3

                return (
                  <div key={row.rank} className="leaderboard-row">
                    <span className="leaderboard-row-rank">
                      {isTop3 ? (
                        <span className="leaderboard-row-rank-badge">{row.rank}</span>
                      ) : (
                        <span style={{ fontSize: rankFontSize(row.rank) }}>{row.rank}</span>
                      )}
                    </span>
                    <div className="leaderboard-row-info">
                      <div className="leaderboard-row-team">{row.team}</div>
                      <div className="leaderboard-row-owner">{row.owner}</div>
                    </div>
                    <span className="leaderboard-row-score">{row.score?.toLocaleString()}P</span>
                  </div>
                )
              })}

              {loadingMore && (
                <div className="leaderboard-loading">
                  <div className="leaderboard-spinner" />
                </div>
              )}
            </>
          )}

          <div className="leaderboard-spacer" />
          <Footer marginTop="4px" padding="20px 0 40px" />
        </div>

        {showToTop && (
          <button type="button" className="leaderboard-scroll-to-top" onClick={scrollToTop} aria-label="scroll to top">
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
