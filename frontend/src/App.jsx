import { useEffect, useMemo, useRef, useState } from 'react'
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import './App.css'

import RosterPage from './pages/RosterPage'
import MyRosterPage from './pages/MyRosterPage'
import StarterPage from './pages/StarterPage'
import MyTeamPage from './pages/MyTeamPage'
import RegisterTeamPage from './pages/RegisterTeamPage'
import ProfilePage from './pages/ProfilePage'
import WithdrawPage from './pages/WithdrawPage'
import WithdrawConfirmPage from './pages/WithdrawConfirmPage'
import LeaderboardPage from './pages/LeaderboardPage'
import InfoPage from './pages/InfoPage'
import PrivacyPolicyPage from './pages/PrivacyPolicyPage'
import ScorePolicyPage from './pages/ScorePolicyPage'
import BottomNav from './components/BottomNav'
import Header from './components/Header'
import Footer from './components/Footer'

import { getMe, loginWithGoogle, logout } from './api/users'
import { getMyTeam } from './api/teams'
import { getPlayers } from './api/players'

function Home({ user, players, handleGoogleLogin }) {
  const { t } = useTranslation()
  const marqueeRef = useRef(null)

  const rows = useMemo(() => {
    const rowSize = 8
    const rowCount = 5

    return Array.from({ length: rowCount }, (_, rowIndex) => {
      const start = rowIndex * rowSize
      const row = players.slice(start, start + rowSize)

      return [...row, ...row]
    })
  }, [players])

  const shifts = ['0%', '-6%', '-3%', '-9%', '-2%']
  const speeds = ['128s', '142s', '136s', '150s', '132s']
  const isLoading = players.length === 0

  useEffect(() => {
    // iOS Safari sometimes fails to start CSS animations driven by inline
    // custom properties on first paint; forcing a reflow kicks it off.
    const rowEls = marqueeRef.current?.querySelectorAll('.lfm-marquee-row')
    rowEls?.forEach((row) => {
      row.style.animation = 'none'
      void row.offsetHeight
      row.style.animation = ''
    })
  }, [players])

  return (
    <main className="lfm-page">
      <section className="lfm-frame">
        <Header variant="logo" divider={false} />

        <div className="lfm-scroll">
          <section className="lfm-hero">
            <h1>
              {t('home.titleLine1')}<br />{t('home.titleLine2')}<br />{t('home.titleLine3')}
            </h1>
            <p>
              {t('home.subtitleLine1')}<br />
              {t('home.subtitleLine2')}
            </p>
          </section>

          <section className="lfm-marquee" ref={marqueeRef}>
            {isLoading
              ? Array.from({ length: 5 }, (_, rowIndex) => (
                  <div key={`skeleton-${rowIndex}`} className="lfm-marquee-skeleton-row">
                    {Array.from({ length: 8 }, (_, cardIndex) => (
                      <div className="lfm-player-card lfm-player-card-skeleton" key={cardIndex}>
                        <div className="lfm-skeleton-bar lfm-skeleton-bar-name" />
                        <div className="lfm-skeleton-bar lfm-skeleton-bar-sub" />
                      </div>
                    ))}
                  </div>
                ))
              : rows.map((row, rowIndex) => (
                  <div
                    key={rowIndex}
                    className="lfm-marquee-row"
                    style={{
                      '--start': shifts[rowIndex],
                      '--speed': speeds[rowIndex],
                    }}
                  >
                    {row.map((player, index) => (
                      <div
                        className="lfm-player-card"
                        key={`${player.playerId}-${index}`}
                      >
                        <div className="lfm-player-name">
                          {player.playerName}
                        </div>
                        <div className="lfm-player-sub">
                          {player.teamName} | {player.position}
                        </div>
                      </div>
                    ))}
                  </div>
                ))}
          </section>

          <Footer marginTop="24px" padding="24px 24px 40px" />
        </div>

        <div className="lfm-cta-area">
          {user ? (
            <Link className="lfm-cta" to="/roster">
              {t('home.cta')}
            </Link>
          ) : (
            <button className="lfm-cta" onClick={handleGoogleLogin}>
              <svg width="20" height="20" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
                <path fill="#4285F4" d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" />
                <path fill="#34A853" d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z" />
                <path fill="#FBBC05" d="M3.964 10.71c-.18-.54-.282-1.117-.282-1.71s.102-1.17.282-1.71V4.958H.957C.347 6.173 0 7.548 0 9s.348 2.827.957 4.042l3.007-2.332z" />
                <path fill="#EA4335" d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z" />
              </svg>
              {t('home.googleLogin')}
            </button>
          )}
        </div>

        <BottomNav />
      </section>
    </main>
  )
}

function App() {
  const { t } = useTranslation()
  const [user, setUser] = useState(null)
  const [team, setTeam] = useState(null)
  const [players, setPlayers] = useState([])

  useEffect(() => {
    getPlayers()
      .then(setPlayers)
      .catch(error => {
        console.error('Failed to load players:', error)
      })
  }, [])

  const refreshUser = () => {
    return getMe()
      .then(setUser)
      .catch(error => {
        console.error('Failed to load user:', error)
        setUser(null)
      })
  }

  useEffect(() => {
    refreshUser()
  }, [])

  const refreshTeam = () => {
    return getMyTeam()
      .then(setTeam)
      .catch(error => {
        console.error('Failed to load team:', error)
        setTeam(null)
      })
  }

  useEffect(() => {
    if (!user) {
      setTeam(null)
      return
    }

    refreshTeam()
  }, [user])

  const handleGoogleLogin = () => {
    loginWithGoogle()
  }

  const handleLogout = async () => {
    await logout()
    setUser(null)
    setTeam(null)
  }

  const handleWithdrawn = () => {
    setUser(null)
    setTeam(null)
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            user && team ? (
              <MyTeamPage team={team} onTeamDeleted={refreshTeam} />
            ) : (
              <Home
                user={user}
                players={players}
                handleGoogleLogin={handleGoogleLogin}
              />
            )
          }
        />

        <Route
          path="/roster"
          element={user ? <RosterPage /> : <div>{t('common.loginRequired')}</div>}
        />

        <Route
          path="/roster/mine"
          element={user ? <MyRosterPage /> : <div>{t('common.loginRequired')}</div>}
        />

        <Route
          path="/roster/register"
          element={user ? <RegisterTeamPage user={user} onTeamCreated={refreshTeam} /> : <div>{t('common.loginRequired')}</div>}
        />

        <Route
          path="/starters"
          element={
            user ? (
              <StarterPage onTeamUpdated={refreshTeam} />
            ) : (
              <div>{t('common.loginRequired')}</div>
            )
          }
        />

        <Route
          path="/profile"
          element={
            <ProfilePage
              user={user}
              onLogout={handleLogout}
              onUserUpdated={refreshUser}
              handleGoogleLogin={handleGoogleLogin}
            />
          }
        />

        <Route
          path="/withdraw"
          element={user ? <WithdrawPage /> : <div>{t('common.loginRequired')}</div>}
        />

        <Route
          path="/withdraw/confirm"
          element={
            user ? (
              <WithdrawConfirmPage user={user} onWithdrawn={handleWithdrawn} />
            ) : (
              <div>{t('common.loginRequired')}</div>
            )
          }
        />

        <Route
          path="/leaderboard"
          element={<LeaderboardPage user={user} team={team} />}
        />

        <Route
          path="/info"
          element={<InfoPage team={team} />}
        />

        <Route path="/privacy" element={<PrivacyPolicyPage />} />
        <Route path="/score-policy" element={<ScorePolicyPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App