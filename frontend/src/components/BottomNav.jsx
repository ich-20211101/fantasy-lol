import { useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import './BottomNav.css'

function BottomNav() {
  const { t } = useTranslation()
  const location = useLocation()
  const navigate = useNavigate()

  const TABS = [
    {
      key: 'myTeam',
      label: t('bottomNav.myTeam'),
      path: '/',
      icon: (
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 2.5l7.5 2.8v5.7c0 4.8-3.2 7.7-7.5 9.5-4.3-1.8-7.5-4.7-7.5-9.5V5.3z" />
          <path d="M8.6 11.6l2.4 2.4 4.4-4.6" />
        </svg>
      ),
    },
    {
      key: 'leaderboard',
      label: t('bottomNav.leaderboard'),
      path: '/leaderboard',
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
      label: t('bottomNav.info'),
      path: '/info',
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
      label: t('bottomNav.profile'),
      path: '/profile',
      icon: (
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="12" cy="8" r="3.6" />
          <path d="M5 20c1.5-3.6 4.2-5.2 7-5.2s5.5 1.6 7 5.2" />
        </svg>
      ),
    },
  ]

  const handleClick = (tab) => {
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