import './BottomNav.css'

function BottomNav() {
  return (
    <nav className="bottom-nav">
      <div className="active">
        <ShieldIcon />
        <span>My Team</span>
      </div>
      <div>
        <ChartIcon />
        <span>리더보드</span>
      </div>
      <div>
        <InfoIcon />
        <span>정보</span>
      </div>
      <div>
        <UserIcon />
        <span>프로필</span>
      </div>
    </nav>
  )
}

function ShieldIcon() {
  return <svg viewBox="0 0 24 24"><path d="M12 2.5l7.5 2.8v5.7c0 4.8-3.2 7.7-7.5 9.5-4.3-1.8-7.5-4.7-7.5-9.5V5.3z" /><path d="M8.6 11.6l2.4 2.4 4.4-4.6" /></svg>
}

function ChartIcon() {
  return <svg viewBox="0 0 24 24"><rect x="4" y="12" width="4" height="8" rx="1" /><rect x="10" y="5" width="4" height="15" rx="1" /><rect x="16" y="9" width="4" height="11" rx="1" /></svg>
}

function InfoIcon() {
  return <svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9" /><path d="M12 11v5" /><circle cx="12" cy="7.6" r="0.6" fill="currentColor" stroke="none" /></svg>
}

function UserIcon() {
  return <svg viewBox="0 0 24 24"><circle cx="12" cy="8" r="3.6" /><path d="M5 20c1.5-3.6 4.2-5.2 7-5.2s5.5 1.6 7 5.2" /></svg>
}

export default BottomNav