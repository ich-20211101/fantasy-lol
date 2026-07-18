import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { updateNickname } from '../api/users'
import Header from '../components/Header'
import Footer from '../components/Footer'
import BottomNav from '../components/BottomNav'
import './ProfilePage.css'

const BANNED = ['시발', '씨발', '병신', '새끼', '좆', '존나', 'ㅅㅂ', 'ㅄ', 'fuck', 'shit']

export default function ProfilePage({ user, onLogout, onUserUpdated, handleGoogleLogin }) {
  const { t } = useTranslation()
  const navigate = useNavigate()

  const [nickPopupOpen, setNickPopupOpen] = useState(false)
  const [nickName, setNickName] = useState('')
  const [isSaving, setIsSaving] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const { nickAlertText, nickAlertColor, nickIsValid } = useMemo(() => {
    const hasProfanity = BANNED.some(word => nickName.toLowerCase().includes(word))
    const tooLong = nickName.length > 10
    const empty = nickName.trim().length === 0

    if (hasProfanity) {
      return { nickAlertText: t('registerTeam.alertProfanity'), nickAlertColor: '#e11d2e', nickIsValid: false }
    }
    if (tooLong) {
      return { nickAlertText: t('registerTeam.alertTooLong'), nickAlertColor: '#e11d2e', nickIsValid: false }
    }
    return { nickAlertText: t('registerTeam.alertTooLong'), nickAlertColor: '#9a9a9e', nickIsValid: !empty }
  }, [nickName, t])

  const openNickPopup = () => {
    setNickName(user.username)
    setErrorMessage('')
    setNickPopupOpen(true)
  }

  const closeNickPopup = () => setNickPopupOpen(false)

  const handleSaveNickname = async () => {
    if (!nickIsValid || isSaving) return

    try {
      setIsSaving(true)
      setErrorMessage('')

      await updateNickname(nickName.trim())
      await onUserUpdated?.()

      setNickPopupOpen(false)
    } catch (error) {
      console.error(error)
      setErrorMessage(error.message || t('profile.errorGeneric'))
    } finally {
      setIsSaving(false)
    }
  }

  const handleLogout = async () => {
    await onLogout?.()
    navigate('/')
  }

  const handleWithdraw = () => navigate('/withdraw')

  if (!user) {
    return (
      <main className="profile-page">
        <section className="profile-frame">
          <Header variant="logo" />

          <div className="profile-scroll">
            <div className="profile-logged-out-message">
              <span>{t('common.loginRequired')}</span>
            </div>

            <div className="profile-logged-out-cta">
              <button type="button" className="profile-google-btn" onClick={handleGoogleLogin}>
                <svg width="20" height="20" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
                  <path fill="#4285F4" d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.875 2.684-6.615z" />
                  <path fill="#34A853" d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z" />
                  <path fill="#FBBC05" d="M3.964 10.71c-.18-.54-.282-1.117-.282-1.71s.102-1.17.282-1.71V4.958H.957C.347 6.173 0 7.548 0 9s.348 2.827.957 4.042l3.007-2.332z" />
                  <path fill="#EA4335" d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z" />
                </svg>
                {t('home.googleLogin')}
              </button>
            </div>

            <Footer marginTop="0" padding="24px 0 40px" />
          </div>

          <BottomNav />
        </section>
      </main>
    )
  }

  return (
    <main className="profile-page">
      <section className="profile-frame">
        {nickPopupOpen && (
          <div className="profile-nick-overlay" onClick={closeNickPopup}>
            <div className="profile-nick-modal" onClick={(e) => e.stopPropagation()}>
              <h2 className="profile-nick-title">{t('profile.nickPopupTitle')}</h2>
              <input
                type="text"
                value={nickName}
                onChange={(e) => setNickName(e.target.value)}
                placeholder={user.username}
                className="profile-nick-input"
              />
              <div className="profile-nick-alert" style={{ color: nickAlertColor }}>
                {nickAlertText}
              </div>
              {errorMessage && <p className="profile-nick-error">{errorMessage}</p>}
              <div className="profile-nick-actions">
                <button type="button" className="profile-nick-close" onClick={closeNickPopup}>
                  {t('profile.nickClose')}
                </button>
                <button
                  type="button"
                  className="profile-nick-save"
                  disabled={!nickIsValid || isSaving}
                  onClick={handleSaveNickname}
                >
                  {t('profile.nickSave')}
                </button>
              </div>
            </div>
          </div>
        )}

        <Header variant="logo" />

        <div className="profile-scroll">
          <div className="profile-nick-row">
            <span className="profile-nick-name">{user.username}</span>
            <button type="button" className="profile-nick-edit" onClick={openNickPopup}>
              <svg width="17" height="17" viewBox="0 0 17 17" fill="none">
                <path
                  d="M8.5 3H3.5a1 1 0 0 0-1 1v9a1 1 0 0 0 1 1h9a1 1 0 0 0 1-1V8.5"
                  stroke="#0b0b0c"
                  strokeWidth="1.4"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
                <path
                  d="M12.6 2.4a1.4 1.4 0 0 1 2 2L8.7 10.3l-2.6.6.6-2.6z"
                  stroke="#0b0b0c"
                  strokeWidth="1.4"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            </button>
          </div>

          <div className="profile-menu-card">
            <div className="profile-menu-label">{t('profile.accountManagement')}</div>
            <div className="profile-menu-item" onClick={handleLogout}>
              {t('profile.logout')}
            </div>
            <div className="profile-menu-item" onClick={handleWithdraw}>
              {t('profile.withdraw')}
            </div>
          </div>

          <div className="profile-spacer" />

          <Footer marginTop="0" padding="24px 0 40px" />
        </div>

        <BottomNav />
      </section>
    </main>
  )
}
