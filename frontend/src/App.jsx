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

  const shifts = ['0%', '-12%', '-5%', '-18%', '-8%']
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
                내 팀 확인하기
              </Link>
            ) : (
              <Link className="lfm-cta" to="/roster">
                팀 창단하기
              </Link>
            )
          ) : (
            <button className="lfm-cta" onClick={handleGoogleLogin}>
              <span>G</span>
              구글 계정으로 팀 창단하기
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