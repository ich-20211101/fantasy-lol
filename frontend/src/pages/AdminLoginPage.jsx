import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { adminLogin } from '../api/admin'
import './AdminLoginPage.css'

export default function AdminLoginPage() {
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [hasError, setHasError] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async () => {
    if (submitting) return

    try {
      setSubmitting(true)
      setHasError(false)
      await adminLogin(email, password)
      navigate('/admin')
    } catch (error) {
      setHasError(true)
    } finally {
      setSubmitting(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') handleSubmit()
  }

  return (
    <main className="admin-login-page">
      <div className="admin-login-frame">
        <div className="admin-login-logo">
          <span className="admin-login-logo-text">LFM</span>
          <span className="admin-login-logo-divider" />
          <span className="admin-login-logo-label">Admin</span>
        </div>

        <div className="admin-login-card">
          <div className="admin-login-field">
            <label>이메일</label>
            <input
              type="text"
              value={email}
              onChange={(e) => { setEmail(e.target.value); setHasError(false) }}
              onKeyDown={handleKeyDown}
            />
          </div>

          <div className="admin-login-field">
            <label>비밀번호</label>
            <input
              type="password"
              value={password}
              onChange={(e) => { setPassword(e.target.value); setHasError(false) }}
              onKeyDown={handleKeyDown}
            />
          </div>

          {hasError && (
            <div className="admin-login-error">이메일 또는 비밀번호가 올바르지 않아요.</div>
          )}

          <button
            type="button"
            className="admin-login-submit"
            disabled={submitting}
            onClick={handleSubmit}
          >
            {submitting ? '로그인 중…' : '로그인'}
          </button>
        </div>

        <p className="admin-login-copyright">© 2026 REDTONGUE GAMES.</p>
      </div>
    </main>
  )
}
