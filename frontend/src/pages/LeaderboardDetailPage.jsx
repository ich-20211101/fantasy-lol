import { useCallback, useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { getLeaderboardDetail, getLeaderboardRounds } from '../api/scores'
import Header from '../components/Header'
import Footer from '../components/Footer'
import BottomNav from '../components/BottomNav'
import './LeaderboardDetailPage.css'

export default function LeaderboardDetailPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { userId } = useParams()

  const [weekSelectOpen, setWeekSelectOpen] = useState(false)
  const [rounds, setRounds] = useState([])
  const [expandedSeasonName, setExpandedSeasonName] = useState(null)
  const [selection, setSelection] = useState({
    seasonName: location.state?.seasonName ?? null,
    weekNumber: location.state?.weekNumber ?? null,
  })

  const [detail, setDetail] = useState(null)

  const isOverall = selection.weekNumber == null

  useEffect(() => {
    getLeaderboardRounds().then((data) => {
      if (data && data.length) {
        setRounds(data)
        setExpandedSeasonName((prev) => prev ?? data[0].seasonName)
      }
    })
  }, [])

  const loadDetail = useCallback((weekNumber, seasonName) => {
    getLeaderboardDetail(userId, weekNumber, seasonName).then(setDetail)
  }, [userId])

  useEffect(() => {
    loadDetail(selection.weekNumber, selection.seasonName)
  }, [selection, loadDetail])

  const toggleWeekSelect = () => setWeekSelectOpen((prev) => !prev)

  const toggleRoundExpand = (seasonName) => {
    setExpandedSeasonName((prev) => (prev === seasonName ? null : seasonName))
  }

  const pickOverall = (seasonName) => {
    setSelection({ seasonName, weekNumber: null })
    setWeekSelectOpen(false)
  }

  const pickWeek = (seasonName, weekNumber) => {
    setSelection({ seasonName, weekNumber })
    setWeekSelectOpen(false)
  }

  const mostRecentRound = rounds[0]
  const selectedRound = selection.seasonName
    ? rounds.find((r) => r.seasonName === selection.seasonName)
    : mostRecentRound

  const headerRoundLabel = isOverall
    ? (detail?.seasonLabel ?? selectedRound?.seasonLabel ?? '')
    : (selectedRound?.seasonLabel ?? '')
  const headerWeekLabel = isOverall
    ? (detail?.weekNumber != null ? `WEEK ${detail.weekNumber}` : 'Overall')
    : `WEEK ${selection.weekNumber}`

  return (
    <main className="lbd-page">
      <section className="lbd-frame">
        <Header variant="back" title={detail?.ownerName ?? ''} onBack={() => navigate(-1)} />

        <div className="lbd-scroll">
          <div className="lbd-week-selector">
            <span className="lbd-round-eyebrow">{headerRoundLabel}</span>

            <div className="lbd-week-toggle" onClick={toggleWeekSelect}>
              <span className="lbd-week-label">{headerWeekLabel}</span>
              <svg
                width="12"
                height="7"
                viewBox="0 0 12 7"
                fill="none"
                style={{ transform: weekSelectOpen ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform .18s ease' }}
              >
                <path d="M1 1l5 5 5-5" stroke="#6a6a6f" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </div>

            {weekSelectOpen && (
              <div className="lbd-week-dropdown">
                {rounds.map((round, roundIndex) => {
                  const isMostRecent = roundIndex === 0
                  const isRoundSelected = isMostRecent ? true : selection.seasonName === round.seasonName

                  return (
                    <div key={round.seasonName}>
                      <div className="lbd-round-header" onClick={() => toggleRoundExpand(round.seasonName)}>
                        <span
                          className="lbd-round-header-label"
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
                        <div className="lbd-round-weeks">
                          {(() => {
                            const isOverallSelected = isOverall
                              && (selection.seasonName ? selection.seasonName === round.seasonName : isMostRecent)

                            return (
                              <div
                                className="lbd-week-option"
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
                                className="lbd-week-option"
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

          <div className="lbd-team-row">
            <span className="lbd-team-label">Team</span>
            <span className="lbd-team-name">{detail?.teamName ?? '-'}</span>
          </div>

          <div className="lbd-summary">
            <div className="lbd-summary-cell">
              <div className="lbd-summary-label">Rank</div>
              <div className="lbd-summary-value">{detail?.rank ?? '-'}</div>
            </div>
            <div className="lbd-summary-divider" />
            <div className="lbd-summary-cell">
              <div className="lbd-summary-label">Score</div>
              <div className="lbd-summary-value lbd-summary-value-mono">{detail?.score?.toLocaleString() ?? '-'}</div>
            </div>
          </div>

          <div className="lbd-col-header">
            <span className="lbd-col-header-name" />
            <span className="lbd-col-header-cell" style={{ width: 52 }}>구매가</span>
            <span className="lbd-col-header-cell" style={{ width: 64 }}>지난시즌점수</span>
            <span className="lbd-col-header-cell" style={{ width: 56 }}>현재점수</span>
          </div>

          {(detail?.players ?? []).map((player, i) => (
            <div className="lbd-player-row" key={`${player.playerId}-${i}`}>
              <div className="lbd-player-info">
                <div className="lbd-player-name">{player.name}</div>
                <div className="lbd-player-sub">{player.team} | {player.pos}</div>
              </div>
              <span className="lbd-player-cell" style={{ width: 52 }}>{player.price?.toFixed(1) ?? '-'}</span>
              <span className="lbd-player-cell" style={{ width: 64 }}>{player.lastScore?.toFixed(1) ?? '-'}</span>
              <span className="lbd-player-cell" style={{ width: 56 }}>{player.curScore?.toFixed(1) ?? '-'}</span>
            </div>
          ))}

          <Footer marginTop="20px" padding="20px 0 40px" />
        </div>

        <BottomNav />
      </section>
    </main>
  )
}
