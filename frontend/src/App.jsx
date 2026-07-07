import { useEffect, useMemo, useState } from 'react'
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import './App.css'

import RosterPage from './pages/RosterPage'
import StarterPage from './pages/StarterPage'
import BottomNav from './components/BottomNav'

import { getMe, loginWithGoogle, logout } from './api/users'
import { getMyTeam } from './api/teams'
import { getPlayers } from './api/players'

function Home({ user, team, players, handleGoogleLogin, handleLogout }) {
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

  return (
    <main className="lfm-page">
      <section className="lfm-frame">
        <header className="lfm-header">
          <div className="lfm-brand-wrap">
            <span className="lfm-logo">LFM</span>
            <span className="lfm-logo-line" />
            <span className="lfm-brand">
              LoL Fantasy<br />Maker
            </span>
          </div>
        </header>

        <div className="lfm-scroll">
          <section className="lfm-hero">
            <h1>
              PROVE<br />YOUR<br />SQUAD
            </h1>
            <p>
              제한된 예산 내 최적의 8인 로스터 구성<br />
              실제 LCK 성적 기반의 랭킹 레이스
            </p>
          </section>

          <section className="lfm-marquee">
            {rows.map((row, rowIndex) => (
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

          <footer className="lfm-footer-info">
            <div className="lfm-footer-links">
              <span>PRIVACY POLICY</span>
              <span>SCORE POLICY</span>
              <span>CONTACT US</span>
            </div>
            <p>
              본 사이트는 LCK 팬페이지입니다. 라이엇 및 LCK와 무관하며,
              경기 데이터를 기반으로 한 시뮬레이션 콘텐츠만을 제공합니다.
            </p>
          </footer>
        </div>

        <div className="lfm-cta-area">
          {user && (
            <p className="lfm-user">
              Hello, <strong>{user.username}</strong>
            </p>
          )}

          {user ? (
            team ? (
              <Link className="lfm-cta" to="/starters">
                내 팀 확인하기1
                <svg width="17" height="17" viewBox="0 0 17 17" fill="none">
                  <path d="M2 8.5h11M9 4l4.5 4.5L9 13" stroke="#ffffff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </Link>
            ) : (
              <Link className="lfm-cta" to="/roster">
                팀 창단하기2
                <svg width="17" height="17" viewBox="0 0 17 17" fill="none">
                  <path d="M2 8.5h11M9 4l4.5 4.5L9 13" stroke="#ffffff" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </Link>
            )
          ) : (
            <button className="lfm-cta" onClick={handleGoogleLogin}>
              <svg width="20" height="20" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
                <path fill="#4285F4" d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" />
                <path fill="#34A853" d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z" />
                <path fill="#FBBC05" d="M3.964 10.71c-.18-.54-.282-1.117-.282-1.71s.102-1.17.282-1.71V4.958H.957C.347 6.173 0 7.548 0 9s.348 2.827.957 4.042l3.007-2.332z" />
                <path fill="#EA4335" d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z" />
              </svg>
              Google 계정으로 시작하기
            </button>
          )}

          {user && (
            <button className="lfm-signout" onClick={handleLogout}>
              Sign Out
            </button>
          )}
        </div>

        <BottomNav />
      </section>
    </main>
  )
}

function App() {
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

  useEffect(() => {
    getMe()
      .then(setUser)
      .catch(error => {
        console.error('Failed to load user:', error)
        setUser(null)
      })
  }, [])

  useEffect(() => {
    if (!user) {
      setTeam(null)
      return
    }

    getMyTeam()
      .then(setTeam)
      .catch(error => {
        console.error('Failed to load team:', error)
        setTeam(null)
      })
  }, [user])

  const handleGoogleLogin = () => {
    loginWithGoogle()
  }

  const handleLogout = async () => {
    await logout()
    setUser(null)
    setTeam(null)
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            <Home
              user={user}
              team={team}
              players={players}
              handleGoogleLogin={handleGoogleLogin}
              handleLogout={handleLogout}
            />
          }
        />

        <Route
          path="/roster"
          element={user ? <RosterPage /> : <div>로그인이 필요합니다.</div>}
        />

        <Route
          path="/starters"
          element={user ? <StarterPage /> : <div>로그인이 필요합니다.</div>}
        />
      </Routes>
    </BrowserRouter>
  )
}

export default App