import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { getLeaderboard, getLeaderboardRounds, getMyScores } from '../api/scores'
import Header from '../components/Header'
import Footer from '../components/Footer'
import BottomNav from '../components/BottomNav'
import './LeaderboardPage.css'

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
  const [rounds, setRounds] = useState([])
  const [expandedSeasonName, setExpandedSeasonName] = useState(null)
  const [selection, setSelection] = useState({ seasonName: null, weekNumber: null })

  const [rows, setRows] = useState([])
  const [page, setPage] = useState(1)
  const [hasMore, setHasMore] = useState(true)
  const [loadingMore, setLoadingMore] = useState(false)
  const [tallying, setTallying] = useState(false)
  const [showToTop, setShowToTop] = useState(false)
  const [resolvedWeekNumber, setResolvedWeekNumber] = useState(null)
  const [resolvedSeasonLabel, setResolvedSeasonLabel] = useState(null)

  const isOverall = selection.weekNumber == null

  useEffect(() => {
    getLeaderboardRounds().then((data) => {
      if (data && data.length) {
        setRounds(data)
        setExpandedSeasonName(data[0].seasonName)
      }
    })
  }, [])

  const loadPage = useCallback(async (pageToLoad, weekNumber, seasonName) => {
    setLoadingMore(true)

    try {
      const data = await getLeaderboard(weekNumber, seasonName, { page: pageToLoad, pageSize: PAGE_SIZE })

      if (!data) {
        setHasMore(false)
        return
      }

      setTallying(Boolean(data.tallying))
      setRows((prev) => (pageToLoad === 1 ? (data.rows ?? []) : [...prev, ...(data.rows ?? [])]))
      setHasMore(Boolean(data.hasMore))
      setResolvedWeekNumber(data.weekNumber ?? null)
      setResolvedSeasonLabel(data.seasonLabel ?? null)
    } finally {
      setLoadingMore(false)
    }
  }, [])

  useEffect(() => {
    setPage(1)
    setHasMore(true)
    loadPage(1, selection.weekNumber, selection.seasonName)
  }, [selection, loadPage])

  useEffect(() => {
    if (!user) {
      setMyScore(null)
      return
    }

    getMyScores(selection.weekNumber, selection.seasonName).then(setMyScore)
  }, [user, selection])

  const handleScroll = useCallback(() => {
    const el = scrollRef.current
    if (!el) return

    setShowToTop(el.scrollTop > 400)

    const nearBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - 120

    if (nearBottom && !loadingMore && hasMore) {
      const nextPage = page + 1
      setPage(nextPage)
      loadPage(nextPage, selection.weekNumber, selection.seasonName)
    }
  }, [loadingMore, hasMore, page, loadPage, selection])

  const scrollToTop = () => {
    scrollRef.current?.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const toggleWeekSelect = () => setWeekSelectOpen((prev) => !prev)

  const toggleRoundExpand = (seasonName) => {
    setExpandedSeasonName((prev) => (prev === seasonName ? null : seasonName))
  }

  const pickOverall = (seasonName) => {
    setSelection({ seasonName, weekNumber: null })
    setWeekSelectOpen(false)
    scrollToTop()
  }

  const pickWeek = (seasonName, weekNumber) => {
    setSelection({ seasonName, weekNumber })
    setWeekSelectOpen(false)
    scrollToTop()
  }

  const goToGoogleLogin = () => navigate('/')

  const hasScoreHistory = Boolean(myScore) && Boolean(team) && myScore.score > 0

  const myRankLoggedOut = !user
  const myRankNoHistory = Boolean(user) && !hasScoreHistory
  const myRankActive = Boolean(user) && hasScoreHistory

  const mostRecentRound = rounds[0]
  const selectedRound = selection.seasonName
    ? rounds.find((r) => r.seasonName === selection.seasonName)
    : mostRecentRound

  const headerRoundLabel = isOverall
    ? (resolvedSeasonLabel ?? selectedRound?.seasonLabel ?? '')
    : (selectedRound?.seasonLabel ?? '')
  const headerWeekLabel = isOverall
    ? (resolvedWeekNumber != null ? `WEEK ${resolvedWeekNumber}` : 'Overall')
    : `WEEK ${selection.weekNumber}`

  return (
    <main className="leaderboard-page">
      <section className="leaderboard-frame">
        <Header variant="logo" />

        <div className="leaderboard-week-selector">
          <span className="leaderboard-round-eyebrow">{headerRoundLabel}</span>

          <div className="leaderboard-week-toggle" onClick={toggleWeekSelect}>
            <span className="leaderboard-week-label">{headerWeekLabel}</span>
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
              {rounds.map((round, roundIndex) => {
                const isMostRecent = roundIndex === 0
                const isRoundSelected = isMostRecent ? true : selection.seasonName === round.seasonName

                return (
                  <div key={round.seasonName}>
                    <div className="leaderboard-round-header" onClick={() => toggleRoundExpand(round.seasonName)}>
                      <span
                        className="leaderboard-round-header-label"
                        style={{ color: isRoundSelected ? '#0b0b0c' : '#9a9a9e' }}
                      >
                        {round.seasonLabel}
                      </span>
                      <svg
                        width="11"
                        height="7"
                        viewBox="0 0 11 7"
                        fill="none"
                        style={{
                          transform: expandedSeasonName === round.seasonName ? 'rotate(180deg)' : 'rotate(0deg)',
                          transition: 'transform .16s ease',
                        }}
                      >
                        <path d="M1 1l4.5 4.5L10 1" stroke="#6a6a6f" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    </div>

                    {expandedSeasonName === round.seasonName && (
                      <div className="leaderboard-round-weeks">
                        {(() => {
                          const isOverallSelected = isOverall
                            && (selection.seasonName ? selection.seasonName === round.seasonName : isMostRecent)

                          return (
                            <div
                              className="leaderboard-week-option"
                              style={{ fontWeight: isOverallSelected ? 700 : 500, color: isOverallSelected ? '#0b0b0c' : '#6a6a6f' }}
                              onClick={() => pickOverall(round.seasonName)}
                            >
                              Overall
                            </div>
                          )
                        })()}
                        {round.weeks.map((weekNumber) => {
                          const isSelected = !isOverall && selection.seasonName === round.seasonName && selection.weekNumber === weekNumber

                          return (
                            <div
                              key={weekNumber}
                              className="leaderboard-week-option"
                              style={{ fontWeight: isSelected ? 700 : 500, color: isSelected ? '#0b0b0c' : '#6a6a6f' }}
                              onClick={() => pickWeek(round.seasonName, weekNumber)}
                            >
                              WEEK {weekNumber}
                            </div>
                          )
                        })}
                      </div>
                    )}
                  </div>
                )
              })}
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
              <span className="leaderboard-row-score">{myScore?.score?.toLocaleString()}P</span>
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
