import { useState, useEffect } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import heroImg from './assets/hero.png'
import './App.css'

function App() {
  
  const [user, setUser] = useState(null)

  useEffect(() => {
    fetch('http://localhost:8080/users/me', {
      credentials: 'include',
      redirect: 'manual'
    })
      .then(res => res.ok ? res.json() : null)
      .then(data => setUser(data))
      .catch(() => {})
  }, [])

  const handleGoogleLogin = () => { 
    window.location.href = 'http://localhost:8080/oauth2/authorization/google'
  }

  const handleLogout = async () => { 
    await fetch('http://localhost:8080/users/logout', {
      method: 'POST',
      credentials: 'include'
    })
    setUser(null)
  }

  return (
    <>
    <div style={{ textAlign: 'center', marginTop: '100px' }}>
      <h1>⚔️ Fantasy LoL</h1>
      <p>나만의 판타지 리그 오브 레전드</p>

      {user ? (
        <div>
          <img src={user.profileImageUrl} alt="profile" />
          <p>Hello, <strong>{user.username}</strong>!</p>
          <button onClick={handleLogout}>Sign Out</button>
        </div>
      ) : (
        <button onClick={handleGoogleLogin}>Sign in with Google</button>
      )}

    </div>
    </>
  )
}

export default App
