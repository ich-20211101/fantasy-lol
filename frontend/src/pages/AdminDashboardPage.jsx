import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAdminMe, adminLogout } from '../api/admin'
import { getPlayers } from '../api/players'
import { syncMatches, syncPlayers } from '../api/matches'
import { detectNewSeasons, registerSeason, lockWeek } from '../api/seasons'
import './AdminDashboardPage.css'

const POS_LABEL = { Top: 'TOP', Jungle: 'JUG', Mid: 'MID', Bot: 'ADC', Support: 'SPT' }
const POSITION_FILTERS = ['ALL', 'TOP', 'JUG', 'MID', 'ADC', 'SPT']

const INITIAL_TEAMS = [
  { fullName: 'T1', shortName: 'T1', status: 'current' },
  { fullName: 'Gen.G', shortName: 'GEN', status: 'current' },
  { fullName: 'Dplus KIA', shortName: 'DK', status: 'current' },
  { fullName: 'Hanwha Life Esports', shortName: 'HLE', status: 'current' },
  { fullName: 'KT Rolster', shortName: 'KT', status: 'current' },
  { fullName: 'Kwangdong Freecs', shortName: 'KDF', status: 'current' },
  { fullName: 'BNK FEARX', shortName: 'BFX', status: 'current' },
  { fullName: 'OKSavingsBank BRION', shortName: 'BRO', status: 'current' },
  { fullName: 'DRX', shortName: 'DRX', status: 'current' },
  { fullName: 'Nongshim RedForce', shortName: 'NS', status: 'current' },
]

const NAV_ITEMS = [
  { key: 'teams', label: '팀명 관리' },
  { key: 'players', label: '선수 관리' },
  { key: 'sync', label: 'API' },
]

function todayIso() {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}

function mapApiPlayers(data) {
  return (data || []).map((p) => ({
    id: p.playerId,
    name: p.playerName,
    team: p.teamName,
    pos: POS_LABEL[p.position] || p.position,
    nickname: p.playerName,
    status: 'current',
  }))
}

export default function AdminDashboardPage() {
  const navigate = useNavigate()

  const [checkingAuth, setCheckingAuth] = useState(true)
  const [adminEmail, setAdminEmail] = useState(null)
  const [activeTab, setActiveTab] = useState('teams')

  const [teams, setTeams] = useState(INITIAL_TEAMS)
  const [editingTeamIndex, setEditingTeamIndex] = useState(null)
  const [teamDeleteConfirmIndex, setTeamDeleteConfirmIndex] = useState(null)

  const [players, setPlayers] = useState([])
  const [playersLoading, setPlayersLoading] = useState(false)
  const [playerSearch, setPlayerSearch] = useState('')
  const [posFilter, setPosFilter] = useState('ALL')
  const [editingPlayerIndex, setEditingPlayerIndex] = useState(null)
  const [playerDeleteConfirmIndex, setPlayerDeleteConfirmIndex] = useState(null)
  const [playerSyncOverviewPage, setPlayerSyncOverviewPage] = useState('')
  const [playerSyncRunning, setPlayerSyncRunning] = useState(false)
  const [playerSyncMessage, setPlayerSyncMessage] = useState(null)

  const [detecting, setDetecting] = useState(false)
  const [detectedSeasons, setDetectedSeasons] = useState(null)
  const [detectMessage, setDetectMessage] = useState(null)

  const [registerSeasonName, setRegisterSeasonName] = useState('')
  const [registering, setRegistering] = useState(false)
  const [registerMessage, setRegisterMessage] = useState(null)

  const [lockDate, setLockDate] = useState('')
  const [lockSeasonName, setLockSeasonName] = useState('')
  const [locking, setLocking] = useState(false)
  const [lockMessage, setLockMessage] = useState(null)

  const [syncDate, setSyncDate] = useState(todayIso())
  const [syncRunning, setSyncRunning] = useState(false)
  const [syncMessage, setSyncMessage] = useState(null)

  useEffect(() => {
    getAdminMe().then((me) => {
      if (!me) {
        navigate('/admin/login')
        return
      }
      setAdminEmail(me.email)
      setCheckingAuth(false)
    })
  }, [navigate])

  useEffect(() => {
    if (checkingAuth || activeTab !== 'players' || players.length > 0) return

    setPlayersLoading(true)
    getPlayers()
      .then((data) => setPlayers(mapApiPlayers(data)))
      .catch((error) => console.error('Failed to load players:', error))
      .finally(() => setPlayersLoading(false))
  }, [checkingAuth, activeTab, players.length])

  const handleLogout = async () => {
    await adminLogout()
    navigate('/admin/login')
  }

  // ---------- 팀명 관리 ----------

  const sortedTeams = useMemo(() => {
    return teams
      .map((t, i) => ({ ...t, origIndex: i }))
      .sort((a, b) => {
        if (a.status !== b.status) return a.status === 'current' ? -1 : 1
        return a.fullName.localeCompare(b.fullName)
      })
  }, [teams])

  const setTeamField = (i, key, value) => {
    setTeams((prev) => prev.map((t, idx) => (idx === i ? { ...t, [key]: value } : t)))
  }

  const saveTeam = () => setEditingTeamIndex(null)

  const confirmDeleteTeam = () => {
    setTeams((prev) => prev.filter((_, idx) => idx !== teamDeleteConfirmIndex))
    setTeamDeleteConfirmIndex(null)
  }

  // ---------- 선수 관리 ----------

  const filteredPlayers = useMemo(() => {
    let list = players
      .map((p, i) => ({ ...p, origIndex: i }))
      .sort((a, b) => {
        if (a.status !== b.status) return a.status === 'current' ? -1 : 1
        return a.name.localeCompare(b.name)
      })

    if (posFilter !== 'ALL') list = list.filter((p) => p.pos === posFilter)
    if (playerSearch.trim()) {
      const q = playerSearch.trim().toLowerCase()
      list = list.filter((p) => p.name.toLowerCase().includes(q))
    }

    return list
  }, [players, posFilter, playerSearch])

  const totalPlayers = filteredPlayers.length

  const setPlayerField = (i, key, value) => {
    setPlayers((prev) => prev.map((p, idx) => (idx === i ? { ...p, [key]: value } : p)))
  }

  const savePlayer = () => setEditingPlayerIndex(null)

  const confirmDeletePlayer = () => {
    setPlayers((prev) => prev.filter((_, idx) => idx !== playerDeleteConfirmIndex))
    setPlayerDeleteConfirmIndex(null)
  }

  const runPlayerSync = async () => {
    if (playerSyncRunning || !playerSyncOverviewPage.trim()) return

    setPlayerSyncRunning(true)
    setPlayerSyncMessage(null)

    try {
      const result = await syncPlayers(playerSyncOverviewPage.trim())
      setPlayerSyncMessage({ type: 'success', text: result })
      const data = await getPlayers()
      setPlayers(mapApiPlayers(data))
    } catch (error) {
      setPlayerSyncMessage({ type: 'error', text: error.message })
    } finally {
      setPlayerSyncRunning(false)
    }
  }

  // ---------- API ----------

  const runDetectNewSeasons = async () => {
    if (detecting) return

    setDetecting(true)
    setDetectMessage(null)

    try {
      const names = await detectNewSeasons()
      setDetectedSeasons(names)
      if (names.length === 0) setDetectMessage({ type: 'success', text: '새로 감지된 시즌이 없어요' })
    } catch (error) {
      setDetectMessage({ type: 'error', text: error.message })
    } finally {
      setDetecting(false)
    }
  }

  const pickDetectedSeason = (name) => setRegisterSeasonName(name)

  const runRegisterSeason = async () => {
    if (registering || !registerSeasonName.trim()) return

    setRegistering(true)
    setRegisterMessage(null)

    try {
      const result = await registerSeason(registerSeasonName.trim())
      setRegisterMessage({ type: 'success', text: result })
      setDetectedSeasons((prev) => (prev || []).filter((n) => n !== registerSeasonName.trim()))
    } catch (error) {
      setRegisterMessage({ type: 'error', text: error.message })
    } finally {
      setRegistering(false)
    }
  }

  const runLockWeek = async () => {
    if (locking || !lockDate || !lockSeasonName.trim()) return

    setLocking(true)
    setLockMessage(null)

    try {
      const result = await lockWeek(lockDate, lockSeasonName.trim())
      setLockMessage({ type: 'success', text: result })
    } catch (error) {
      setLockMessage({ type: 'error', text: error.message })
    } finally {
      setLocking(false)
    }
  }

  const runSync = async () => {
    if (syncRunning || !syncDate) return

    setSyncRunning(true)
    setSyncMessage(null)

    try {
      const result = await syncMatches(syncDate)
      setSyncMessage({ type: 'success', text: result })
    } catch (error) {
      setSyncMessage({ type: 'error', text: error.message })
    } finally {
      setSyncRunning(false)
    }
  }

  if (checkingAuth) {
    return <div className="admin-dash-loading">확인 중…</div>
  }

  return (
    <div className="admin-dash">
      <aside className="admin-dash-sidebar">
        <div className="admin-dash-logo">
          <span className="admin-dash-logo-text">LFM</span>
          <span className="admin-dash-logo-divider" />
          <span className="admin-dash-logo-label">Admin</span>
        </div>

        <div className="admin-dash-nav-label">운영</div>
        {NAV_ITEMS.map((nav) => (
          <div
            key={nav.key}
            className={`admin-dash-nav-item ${activeTab === nav.key ? 'active' : ''}`}
            onClick={() => setActiveTab(nav.key)}
          >
            <span>{nav.label}</span>
          </div>
        ))}

        <div className="admin-dash-sidebar-spacer" />

        <div className="admin-dash-account">
          <div className="admin-dash-account-label">관리자</div>
          <div className="admin-dash-account-email">{adminEmail}</div>
          <button type="button" className="admin-dash-logout" onClick={handleLogout}>로그아웃</button>
        </div>
      </aside>

      <div className="admin-dash-main">

        {activeTab === 'teams' && (
          <>
            <h1 className="admin-dash-title">팀명 관리</h1>

            <div className="admin-dash-table">
              <div className="admin-dash-table-header admin-dash-teams-grid">
                <span>#</span>
                <span>정식 명칭</span>
                <span>약칭</span>
                <span>상태</span>
                <span className="admin-dash-col-right">액션</span>
              </div>

              {sortedTeams.map((t, i) => (
                <div className="admin-dash-table-row admin-dash-teams-grid" key={t.origIndex}>
                  <span className="admin-dash-idx">{i + 1}</span>
                  <span className="admin-dash-cell-strong">{t.fullName}</span>

                  {editingTeamIndex === t.origIndex ? (
                    <input
                      type="text"
                      value={t.shortName}
                      onChange={(e) => setTeamField(t.origIndex, 'shortName', e.target.value)}
                      autoFocus
                    />
                  ) : (
                    <span className="admin-dash-mono">{t.shortName}</span>
                  )}

                  {editingTeamIndex === t.origIndex ? (
                    <select
                      value={t.status}
                      onChange={(e) => setTeamField(t.origIndex, 'status', e.target.value)}
                    >
                      <option value="current">Current</option>
                      <option value="previous">Previous</option>
                    </select>
                  ) : (
                    <span className={`admin-dash-status ${t.status}`}>
                      {t.status === 'current' ? 'Current' : 'Previous'}
                    </span>
                  )}

                  <div className="admin-dash-col-right admin-dash-actions">
                    {editingTeamIndex === t.origIndex ? (
                      <>
                        <button type="button" className="admin-dash-btn" onClick={() => setEditingTeamIndex(null)}>취소</button>
                        <button type="button" className="admin-dash-btn-dark" onClick={saveTeam}>저장</button>
                      </>
                    ) : (
                      <>
                        <button type="button" className="admin-dash-btn" onClick={() => setEditingTeamIndex(t.origIndex)}>수정</button>
                        <button type="button" className="admin-dash-btn-danger" onClick={() => setTeamDeleteConfirmIndex(t.origIndex)}>삭제</button>
                      </>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </>
        )}

        {teamDeleteConfirmIndex !== null && (
          <div className="admin-dash-overlay">
            <div className="admin-dash-confirm">
              <div className="admin-dash-confirm-title">정말 삭제하시겠습니까?</div>
              <p className="admin-dash-confirm-desc">
                <b>{teams[teamDeleteConfirmIndex]?.fullName}</b> 팀을 삭제하면 되돌릴 수 없어요.
              </p>
              <div className="admin-dash-confirm-actions">
                <button type="button" className="admin-dash-btn" onClick={() => setTeamDeleteConfirmIndex(null)}>취소</button>
                <button type="button" className="admin-dash-btn-danger-solid" onClick={confirmDeleteTeam}>삭제</button>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'players' && (
          <>
            <div>
              <h1 className="admin-dash-title">선수 관리</h1>
              <p className="admin-dash-subtitle">선수 정보와 소속·포지션·닉네임을 관리해요.</p>
            </div>

            <div className="admin-dash-sync-card">
              <div className="admin-dash-sync-input-group">
                <div className="admin-dash-sync-label">SEASON OVERVIEW PAGE</div>
                <input
                  type="text"
                  value={playerSyncOverviewPage}
                  onChange={(e) => setPlayerSyncOverviewPage(e.target.value)}
                  placeholder="LCK/2026 Season/Rounds 1-2"
                  className="admin-dash-search admin-dash-sync-input"
                />
              </div>
              <button
                type="button"
                className="admin-dash-sync-btn"
                disabled={playerSyncRunning || !playerSyncOverviewPage.trim()}
                onClick={runPlayerSync}
              >
                {playerSyncRunning && <span className="admin-dash-spinner" />}
                <span>{playerSyncRunning ? '불러오는 중…' : '선수 불러오기'}</span>
              </button>
            </div>

            {playerSyncMessage && (
              <div className={`admin-dash-sync-message ${playerSyncMessage.type}`}>{playerSyncMessage.text}</div>
            )}

            <div className="admin-dash-filters">
              <input
                type="text"
                value={playerSearch}
                onChange={(e) => setPlayerSearch(e.target.value)}
                placeholder="선수명 검색"
                className="admin-dash-search"
              />
              {POSITION_FILTERS.map((pos) => (
                <div
                  key={pos}
                  className={`admin-dash-chip ${posFilter === pos ? 'active' : ''}`}
                  onClick={() => setPosFilter(pos)}
                >
                  {pos}
                </div>
              ))}
            </div>

            <div className="admin-dash-table">
              <div className="admin-dash-table-header admin-dash-players-grid">
                <span>선수</span>
                <span>닉네임</span>
                <span>포지션</span>
                <span>상태</span>
                <span className="admin-dash-col-right">액션</span>
              </div>

              {playersLoading && <div className="admin-dash-empty">불러오는 중…</div>}

              {!playersLoading && filteredPlayers.map((p) => (
                <div className="admin-dash-table-row admin-dash-players-grid" key={p.id}>
                  <span className="admin-dash-cell-strong">{p.name}</span>

                  {editingPlayerIndex === p.origIndex ? (
                    <input
                      type="text"
                      value={p.nickname}
                      onChange={(e) => setPlayerField(p.origIndex, 'nickname', e.target.value)}
                      autoFocus
                    />
                  ) : (
                    <span className="admin-dash-cell-muted">{p.nickname}</span>
                  )}

                  <span className="admin-dash-mono">{p.pos}</span>

                  {editingPlayerIndex === p.origIndex ? (
                    <select
                      value={p.status}
                      onChange={(e) => setPlayerField(p.origIndex, 'status', e.target.value)}
                    >
                      <option value="current">Current</option>
                      <option value="previous">Previous</option>
                    </select>
                  ) : (
                    <span className={`admin-dash-status ${p.status}`}>
                      {p.status === 'current' ? 'Current' : 'Previous'}
                    </span>
                  )}

                  <div className="admin-dash-col-right admin-dash-actions">
                    {editingPlayerIndex === p.origIndex ? (
                      <>
                        <button type="button" className="admin-dash-btn" onClick={() => setEditingPlayerIndex(null)}>취소</button>
                        <button type="button" className="admin-dash-btn-dark" onClick={savePlayer}>저장</button>
                      </>
                    ) : (
                      <>
                        <button type="button" className="admin-dash-btn" onClick={() => setEditingPlayerIndex(p.origIndex)}>수정</button>
                        <button type="button" className="admin-dash-btn-danger" onClick={() => setPlayerDeleteConfirmIndex(p.origIndex)}>삭제</button>
                      </>
                    )}
                  </div>
                </div>
              ))}

              {!playersLoading && filteredPlayers.length === 0 && (
                <div className="admin-dash-empty">조건에 맞는 선수가 없어요</div>
              )}
            </div>

            <div className="admin-dash-page-info">전체 {totalPlayers}명</div>
          </>
        )}

        {playerDeleteConfirmIndex !== null && (
          <div className="admin-dash-overlay">
            <div className="admin-dash-confirm">
              <div className="admin-dash-confirm-title">정말 삭제하시겠습니까?</div>
              <p className="admin-dash-confirm-desc">
                <b>{players[playerDeleteConfirmIndex]?.name}</b> 선수를 삭제하면 되돌릴 수 없어요.
              </p>
              <div className="admin-dash-confirm-actions">
                <button type="button" className="admin-dash-btn" onClick={() => setPlayerDeleteConfirmIndex(null)}>취소</button>
                <button type="button" className="admin-dash-btn-danger-solid" onClick={confirmDeletePlayer}>삭제</button>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'sync' && (
          <>
            <h1 className="admin-dash-title">API</h1>

            {/* 1. 새 시즌 감지 */}
            <div>
              <div className="admin-dash-log-title">1. 새 시즌 감지</div>
              <div className="admin-dash-sync-card">
                <div className="admin-dash-sync-info">
                  <div className="admin-dash-sync-value">GET /seasons/detect-new</div>
                </div>
                <button
                  type="button"
                  className="admin-dash-sync-btn"
                  disabled={detecting}
                  onClick={runDetectNewSeasons}
                >
                  {detecting && <span className="admin-dash-spinner" />}
                  <span>{detecting ? '확인 중…' : '감지하기'}</span>
                </button>
              </div>

              {detectMessage && (
                <div className={`admin-dash-sync-message ${detectMessage.type}`}>{detectMessage.text}</div>
              )}

              {detectedSeasons && detectedSeasons.length > 0 && (
                <div className="admin-dash-detect-list">
                  {detectedSeasons.map((name) => (
                    <div key={name} className="admin-dash-detect-chip" onClick={() => pickDetectedSeason(name)}>
                      {name}
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* 2. 시즌 등록 */}
            <div>
              <div className="admin-dash-log-title">2. 시즌 등록</div>
              <div className="admin-dash-sync-card">
                <div className="admin-dash-sync-input-group">
                  <div className="admin-dash-sync-label">SEASON NAME</div>
                  <input
                    type="text"
                    value={registerSeasonName}
                    onChange={(e) => setRegisterSeasonName(e.target.value)}
                    placeholder="LCK/2026 Season/Rounds 1-2"
                    className="admin-dash-search admin-dash-sync-input"
                  />
                </div>
                <button
                  type="button"
                  className="admin-dash-sync-btn"
                  disabled={registering || !registerSeasonName.trim()}
                  onClick={runRegisterSeason}
                >
                  {registering && <span className="admin-dash-spinner" />}
                  <span>{registering ? '등록 중…' : '등록'}</span>
                </button>
              </div>

              {registerMessage && (
                <div className={`admin-dash-sync-message ${registerMessage.type}`}>{registerMessage.text}</div>
              )}
            </div>

            {/* 3. 주차 강제 락 */}
            <div>
              <div className="admin-dash-log-title">3. 주차 강제 락</div>
              <div className="admin-dash-sync-card">
                <div className="admin-dash-sync-input-group">
                  <div className="admin-dash-sync-label">DATE</div>
                  <input
                    type="date"
                    value={lockDate}
                    onChange={(e) => setLockDate(e.target.value)}
                    className="admin-dash-search admin-dash-sync-input"
                  />
                </div>
                <div className="admin-dash-sync-input-group">
                  <div className="admin-dash-sync-label">SEASON NAME</div>
                  <input
                    type="text"
                    value={lockSeasonName}
                    onChange={(e) => setLockSeasonName(e.target.value)}
                    placeholder="LCK/2026 Season/Road to MSI"
                    className="admin-dash-search admin-dash-sync-input"
                  />
                </div>
                <button
                  type="button"
                  className="admin-dash-sync-btn"
                  disabled={locking || !lockDate || !lockSeasonName.trim()}
                  onClick={runLockWeek}
                >
                  {locking && <span className="admin-dash-spinner" />}
                  <span>{locking ? '락 거는 중…' : '락 걸기'}</span>
                </button>
              </div>

              {lockMessage && (
                <div className={`admin-dash-sync-message ${lockMessage.type}`}>{lockMessage.text}</div>
              )}
            </div>

            {/* 4. 경기 sync 날짜 지정 */}
            <div>
              <div className="admin-dash-log-title">4. 경기 결과 동기화</div>
              <div className="admin-dash-sync-card">
                <div className="admin-dash-sync-input-group">
                  <div className="admin-dash-sync-label">DATE</div>
                  <input
                    type="date"
                    value={syncDate}
                    onChange={(e) => setSyncDate(e.target.value)}
                    className="admin-dash-search admin-dash-sync-input"
                  />
                </div>
                <button
                  type="button"
                  className="admin-dash-sync-btn"
                  disabled={syncRunning || !syncDate}
                  onClick={runSync}
                >
                  {syncRunning && <span className="admin-dash-spinner" />}
                  <span>{syncRunning ? '불러오는 중…' : '동기화'}</span>
                </button>
              </div>

              {syncMessage && (
                <div className={`admin-dash-sync-message ${syncMessage.type}`}>{syncMessage.text}</div>
              )}
            </div>
          </>
        )}

      </div>
    </div>
  )
}
