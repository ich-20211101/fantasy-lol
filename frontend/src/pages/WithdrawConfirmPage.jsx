import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { deleteAccount } from '../api/users'
import Header from '../components/Header'
import Footer from '../components/Footer'
import BottomNav from '../components/BottomNav'
import './WithdrawConfirmPage.css'

export default function WithdrawConfirmPage({ user, onWithdrawn }) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    if (!location.state) {
      navigate('/withdraw', { replace: true })
    }
  }, [location.state, navigate])

  if (!location.state) return null

  const { reason, note } = location.state

  const handleContinue = () => {
    navigate('/profile')
  }

  const handleConfirm = async () => {
    if (isSubmitting) return

    try {
      setIsSubmitting(true)
      setErrorMessage('')

      await deleteAccount(reason, note)
      onWithdrawn?.()
      navigate('/')
    } catch (error) {
      console.error(error)
      setErrorMessage(error.message || t('withdraw.errorGeneric'))
      setIsSubmitting(false)
    }
  }

  return (
    <main className="withdraw-confirm-page">
      <section className="withdraw-confirm-frame">
        <Header variant="logo" />

        <div className="withdraw-confirm-scroll">
          <h1 className="withdraw-confirm-title">
            {t('withdraw.confirmTitleLine1', { username: user.username })}<br />
            {t('withdraw.confirmTitleLine2')}
          </h1>

          <div className="withdraw-confirm-box">
            <span>
              {t('withdraw.warningPrefix')}
              <u>{t('withdraw.warningUnderline')}</u>
            </span>
          </div>

          {errorMessage && <p className="withdraw-confirm-error">{errorMessage}</p>}

          <div className="withdraw-confirm-actions">
            <button type="button" className="withdraw-confirm-continue" onClick={handleContinue}>
              {t('withdraw.continueBtn')}
            </button>
            <button
              type="button"
              className="withdraw-confirm-delete"
              disabled={isSubmitting}
              onClick={handleConfirm}
            >
              {t('withdraw.confirmBtn')}
            </button>
          </div>

          <div className="withdraw-confirm-spacer" />

          <Footer marginTop="0" padding="24px 0 40px" />
        </div>

        <BottomNav />
      </section>
    </main>
  )
}
