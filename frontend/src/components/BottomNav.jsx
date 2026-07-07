import { useLocation, useNavigate } from 'react-router-dom'
import './BottomNav.css'

const TABS = [
  {
    key: 'myTeam',
    label: 'My Team',
    path: '/starters',
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 2.5l7.5 2.8v5.7c0 4.8-3.2 7.7-7.5 9.5-4.3-1.8-7.5-4.7-7.5-9.5V5.3z" />
        <path d="M8.6 11.6l2.4 2.4 4.4-4.6" />
      </svg>
    ),
  },
  {
    key: 'leaderboard',
    label: '리더보드',
    path: null, // TODO: 리더보드 페이지 생기면 연결
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <rect x="4" y="12" width="4" height="8" rx="1" />
        <rect x="10" y="5" width="4" height="15" rx="1" />
        <rect x="16" y="9" width="4" height="11" rx="1" />
      </svg>
    ),
  },
  {
    key: 'info',
    label: '정보',
    path: null, // TODO: 정보 페이지 생기면 연결
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="9" />
        <path d="M12 11v5" />
        <circle cx="12" cy="7.6" r="0.6" fill="currentColor" stroke="none" />
      </svg>
    ),
  },
  {
    key: 'profile',
    label: '프로필',
    path: null, // TODO: 프로필 페이지 생기면 연결 (지금은 로그인 시 로그아웃 트리거로 임시 사용)
    icon: (
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="8" r="3.6" />
        <path d="M5 20c1.5-3.6 4.2-5.2 7-5.2s5.5 1.6 7 5.2" />
      </svg>
    ),
  },
]

function BottomNav({ user, onLogout }) {
  const location = useLocation()
  const navigate = useNavigate()

  const handleClick = (tab) => {
    if (tab.key === 'profile' && user) {
      onLogout?.()
      return
    }
    if (tab.path) {
      navigate(tab.path)
    }
  }

  return (
    <div className="lfm-bottomnav">
      {TABS.map((tab) => {
        const active = tab.path && location.pathname === tab.path

        return (
          <div
            key={tab.key}
            className="lfm-bottomnav-item"
            style={{ color: active ? '#0b0b0c' : '#9a9a9e' }}
            onClick={() => handleClick(tab)}
          >
            {tab.icon}
            <span style={{ fontWeight: active ? 700 : 500 }}>{tab.label}</span>
          </div>
        )
      })}
    </div>
  )
}

export default BottomNav