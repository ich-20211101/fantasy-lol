import { useState, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import './App.css'
import RosterPage from './pages/RosterPage'

const POS_LABEL = { Top: '탑', Jungle: '정글', Mid: '미드', Bot: '원딜', Support: '서포터' }

function Home({ user, team, handleGoogleLogin, handleLogout }) {
  return (
    <div style={{ textAlign: 'center', marginTop: '100px' }}>
      <h1>⚔️ Fantasy LoL</h1>
      <p>나만의 판타지 리그 오브 레전드</p>
      {user ? (
        <div>
          <img src={user.profileImageUrl} alt="profile" />
          <p>Hello, <strong>{user.username}</strong>!</p>

          {team ? (
            <div style={{ maxWidth: 480, margin: '24px auto', textAlign: 'left' }}>
              <h3 style={{ textAlign: 'center' }}>{team.teamName}</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 16 }}>
                {team.roster.map(p => (
                  <div key={p.teamRosterId} style={{
                    padding: '10px 14px',
                    border: p.isStarter ? '2px solid #000' : '1px solid #ddd',
                    borderRadius: 8,
                    display: 'flex',
                    justifyContent: 'space-between'
                  }}>
                    <div>
                      <div style={{ fontWeight: 500 }}>{p.playerName}</div>
                      <div style={{ fontSize: 12, color: '#888', marginTop: 2 }}>
                        {p.teamName} | {POS_LABEL[p.position]}
                      </div>
                    </div>
                    {p.isStarter && <span style={{ fontSize: 12, color: '#000' }}>스타터</span>}
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div>
              <p>아직 팀이 없어요.</p>
              <Link to="/roster">로스터 구성하기</Link>
            </div>
          )}

          <br /><br />
          <button onClick={handleLogout}>Sign Out</button>
        </div>
      ) : (
        <button onClick={handleGoogleLogin}>Sign in with Google</button>
      )}
    </div>
  )
}

function App() {
  const [user, setUser] = useState(null)
  const [team, setTeam] = useState(null)

  useEffect(() => {
    fetch('http://localhost:8080/users/me', {
      credentials: 'include',
      redirect: 'manual'
    })
      .then(res => res.ok ? res.json() : null)
      .then(data => setUser(data))
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (!user) return
    fetch('http://localhost:8080/teams/me', {
      credentials: 'include'
    })
      .then(res => res.ok ? res.json() : null)
      .then(data => setTeam(data))
      .catch(() => {})
  }, [user])

  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google'
  }

  const handleLogout = async () => {
    await fetch('http://localhost:8080/users/logout', {
      method: 'POST',
      credentials: 'include'
    })
    setUser(null)
    setTeam(null)
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home user={user} team={team} handleGoogleLogin={handleGoogleLogin} handleLogout={handleLogout} />} />
        <Route path="/roster" element={user ? <RosterPage /> : <div>로그인이 필요합니다.</div>} />
      </Routes>
    </BrowserRouter>
  )
}

export default App