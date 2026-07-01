import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

const POSITIONS = ['Top', 'Jungle', 'Mid', 'Bot', 'Support']
const POS_LABEL = { Top: '탑', Jungle: '정글', Mid: '미드', Bot: '원딜', Support: '서포터' }

export default function StarterPage() {
  const navigate = useNavigate()
  const [team, setTeam] = useState(null)
  const [starters, setStarters] = useState({}) // { position: playerId }
  const [message, setMessage] = useState('')

  useEffect(() => {
    fetch('http://localhost:8080/teams/me', { credentials: 'include' })
      .then(r => r.json())
      .then(data => {
        if (!data) return
        setTeam(data)

        // 기존 스타터 초기값 세팅
        const initialStarters = {}
        data.roster.forEach(p => {
          if (p.isStarter) initialStarters[p.position] = p.playerId
        })
        setStarters(initialStarters)
      })
  }, [])

  const selectStarter = (position, playerId) => {
    setStarters(prev => ({ ...prev, [position]: playerId }))
  }

  const isComplete = POSITIONS.every(pos => starters[pos])

  const submit = async () => {
    try {
      const res = await fetch(`http://localhost:8080/teams/${team.teamId}/starters`, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ playerIds: Object.values(starters) })
      })

      if (res.ok) {
        window.location.href = '/'
      } else {
        const err = await res.json()
        setMessage(err.error || '저장 실패')
      }
    } catch {
      setMessage('오류가 발생했습니다.')
    }
  }

  if (!team) return <div style={{ textAlign: 'center', marginTop: 100 }}>로딩 중...</div>

  return (
    <div style={{ maxWidth: 480, margin: '0 auto', padding: '16px' }}>
      <button onClick={() => navigate('/')} style={{ marginBottom: 16 }}>← 뒤로</button>
      <h3 style={{ marginBottom: 4 }}>스타터 선택</h3>
      <p style={{ fontSize: 13, color: '#888', marginBottom: 24 }}>각 포지션별 1명씩 선택하세요</p>

      {POSITIONS.map(pos => {
        const posPlayers = team.roster.filter(p => p.position === pos)
        return (
          <div key={pos} style={{ marginBottom: 20 }}>
            <div style={{ fontWeight: 600, marginBottom: 8 }}>{POS_LABEL[pos]}</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              {posPlayers.map(p => {
                const isSelected = starters[pos] === p.playerId
                return (
                  <div
                    key={p.playerId}
                    onClick={() => selectStarter(pos, p.playerId)}
                    style={{
                      padding: '10px 14px',
                      border: isSelected ? '2px solid #000' : '1px solid #ddd',
                      borderRadius: 8,
                      cursor: 'pointer',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      background: isSelected ? '#f5f5f5' : '#fff'
                    }}
                  >
                    <div>
                      <div style={{ fontWeight: 500 }}>{p.playerName}</div>
                      <div style={{ fontSize: 12, color: '#888', marginTop: 2 }}>{p.teamName}</div>
                    </div>
                    {isSelected && <span>✓</span>}
                  </div>
                )
              })}
            </div>
          </div>
        )
      })}

      {message && <p style={{ color: 'red', marginBottom: 12 }}>{message}</p>}

      <button
        onClick={submit}
        disabled={!isComplete}
        style={{
          width: '100%',
          padding: '12px',
          background: isComplete ? '#000' : '#ccc',
          color: '#fff',
          border: 'none',
          borderRadius: 8,
          fontSize: 15,
          cursor: isComplete ? 'pointer' : 'not-allowed'
        }}
      >
        스타터 저장
      </button>
    </div>
  )
}