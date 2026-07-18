import { Link } from 'react-router-dom'
import './Header.css'

export default function Header({
  variant = 'logo',
  title = '',
  onBack,
  showBackButton = true,
  divider = true,
}) {
  const isLogo = variant === 'logo'

  return (
    <header
      className={`app-header ${isLogo ? 'app-header-logo' : 'app-header-back'}`}
      style={{ borderBottom: divider ? '1px solid rgba(11, 11, 12, 0.08)' : 'none' }}
    >
      {isLogo ? (
        <Link to="/" className="lfm-brand-wrap">
          <span className="lfm-logo">LFM</span>
          <span className="lfm-logo-line" />
          <span className="lfm-brand">
            LoL Fantasy<br />Maker
          </span>
        </Link>
      ) : (
        <>
          {showBackButton ? (
            <button type="button" className="app-header-back-btn" onClick={onBack}>
              <svg width="9" height="15" viewBox="0 0 9 15" fill="none">
                <path
                  d="M8 1L1.5 7.5L8 14"
                  stroke="#0b0b0c"
                  strokeWidth="1.8"
                  strokeLinecap="round"
                />
              </svg>
            </button>
          ) : (
            <div className="app-header-back-spacer" />
          )}
          <span className="app-header-title">{title}</span>
        </>
      )}
    </header>
  )
}
