import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

const POSITIONS = ['Top', 'Jungle', 'Mid', 'Bot', 'Support']
const POS_LABEL = { Top: '탑', Jungle: '정글', Mid: '미드', Bot: '원딜', Support: '서포터' }

export default function RosterPage() {
  const navigate = useNavigate()
  const [players, setPlayers] = useState([])
  const [selected, setSelected] = useState(new Set())
  const [posFilter, setPosFilter] = useState('')
  const [teamFilter, setTeamFilter] = useState('')
  const [teams, setTeams] = useState([])
  const [step, setStep] = useState(1)
  const [teamName, setTeamName] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    fetch('http://localhost:8080/players', { credentials: 'include' })
      .then(r => r.json())
      .then(data => {
        setPlayers(data)
        setTeams([...new Set(data.map(p => p.teamName))].sort())
      })
  }, [])

  const filtered = players
    .filter(p => !posFilter || p.position === posFilter)
    .filter(p => !teamFilter || p.teamName === teamFilter)

  const selectedPlayers = players.filter(p => selected.has(p.playerId))

  const toggle = (id) => {
    setSelected(prev => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else if (next.size < 8) {
        next.add(id)
      }
      return next
    })
  }

const submit = async () => {
  if (!teamName.trim()) {
    setMessage('팀 이름을 입력해주세요.')
    return
  }

  try {
    const res = await fetch('http://localhost:8080/teams/roster', {
      method: 'PUT',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        teamName: teamName.trim(),
        playerIds: [...selected]
      })
    })

    const result = await res.json()

    if (res.ok) {
      window.location.href = '/'
    } else {
      setMessage(result.error || '등록 실패')
    }
  } catch (e) {
    console.error('submit error:', e)
    setMessage('오류가 발생했습니다.')
  }
}

  if (step === 2) {
    return (
      <div style={{ maxWidth: 480, margin: '0 auto', padding: '16px' }}>
        <button onClick={() => setStep(1)} style={{ marginBottom: 16 }}>← 뒤로</button>
        <h3 style={{ marginBottom: 16 }}>선택한 선수 ({selectedPlayers.length}명)</h3>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 24 }}>
          {selectedPlayers.map(p => (
            <div key={p.playerId} style={{ padding: '10px 14px', border: '1px solid #ddd', borderRadius: 8, display: 'flex', justifyContent: 'space-between' }}>
              <div>
                <div style={{ fontWeight: 500 }}>{p.playerName}</div>
                <div style={{ fontSize: 12, color: '#888', marginTop: 2 }}>{p.teamName} | {POS_LABEL[p.position]}</div>
              </div>
            </div>
          ))}
        </div>

        <div style={{ marginBottom: 16 }}>
          <label style={{ display: 'block', marginBottom: 8, fontWeight: 500 }}>팀 이름</label>
          <input
            type="text"
            value={teamName}
            onChange={e => setTeamName(e.target.value)}
            placeholder="팀 이름을 입력하세요"
            style={{ width: '100%', padding: '10px 12px', border: '1px solid #ddd', borderRadius: 8, fontSize: 15 }}
          />
        </div>

        {message && <p style={{ marginBottom: 12, color: 'red' }}>{message}</p>}

        <button
          onClick={submit}
          disabled={!teamName.trim()}
          style={{
            width: '100%',
            padding: '12px',
            background: teamName.trim() ? '#000' : '#ccc',
            color: '#fff',
            border: 'none',
            borderRadius: 8,
            fontSize: 15,
            cursor: teamName.trim() ? 'pointer' : 'not-allowed'
          }}
        >
          저장
        </button>
      </div>
    )
  }

  return (
    <div style={{ maxWidth: 480, margin: '0 auto', padding: '16px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <span>선수 선택</span>
        <span style={{ fontWeight: 500 }}>{selected.size} / 8</span>
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        <select value={posFilter} onChange={e => setPosFilter(e.target.value)} style={{ flex: 1 }}>
          <option value=''>포지션 전체</option>
          {POSITIONS.map(p => <option key={p} value={p}>{POS_LABEL[p]}</option>)}
        </select>
        <select value={teamFilter} onChange={e => setTeamFilter(e.target.value)} style={{ flex: 1 }}>
          <option value=''>팀 전체</option>
          {teams.map(t => <option key={t} value={t}>{t}</option>)}
        </select>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 16 }}>
        {filtered.map(p => (
          <div
            key={p.playerId}
            onClick={() => toggle(p.playerId)}
            style={{
              padding: '12px 14px',
              border: selected.has(p.playerId) ? '2px solid #000' : '1px solid #ddd',
              borderRadius: 8,
              cursor: 'pointer',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              background: selected.has(p.playerId) ? '#f5f5f5' : '#fff'
            }}
          >
            <div>
              <div style={{ fontWeight: 500 }}>{p.playerName}</div>
              <div style={{ fontSize: 12, color: '#888', marginTop: 2 }}>
                {p.teamName} | {POS_LABEL[p.position]}
              </div>
            </div>
            {selected.has(p.playerId) && <span>✓</span>}
          </div>
        ))}
      </div>

      <button
        onClick={() => setStep(2)}
        disabled={selected.size !== 8}
        style={{
          width: '100%',
          padding: '12px',
          background: selected.size === 8 ? '#000' : '#ccc',
          color: '#fff',
          border: 'none',
          borderRadius: 8,
          fontSize: 15,
          cursor: selected.size === 8 ? 'pointer' : 'not-allowed'
        }}
      >
        로스터 구성
      </button>
    </div>
  )
}