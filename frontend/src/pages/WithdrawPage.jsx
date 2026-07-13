import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import Header from '../components/Header'
import Footer from '../components/Footer'
import BottomNav from '../components/BottomNav'
import './WithdrawPage.css'

const REASONS = [
  { code: 'NOT_INTERESTED', i18nKey: 'reason1' },
  { code: 'SITE_INCONVENIENT', i18nKey: 'reason2' },
  { code: 'LACK_OF_TRUST', i18nKey: 'reason3' },
  { code: 'NOT_FUN', i18nKey: 'reason4' },
  { code: 'CHANGED_MIND', i18nKey: 'reason5' },
]
const NOTE_MAX_LENGTH = 500

export default function WithdrawPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()

  const [selectedReason, setSelectedReason] = useState(null)
  const [note, setNote] = useState('')

  const toggleReason = (code) => {
    setSelectedReason(prev => (prev === code ? null : code))
  }

  const handleContinue = () => {
    navigate('/profile')
  }

  const handleNext = () => {
    navigate('/withdraw/confirm', { state: { reason: selectedReason, note: note.trim() } })
  }

  return (
    <main className="withdraw-page">
      <section className="withdraw-frame">
        <Header variant="logo" />

        <div className="withdraw-scroll">
          <h1 className="withdraw-title">
            {t('withdraw.titleLine1')}<br />{t('withdraw.titleLine2')}
          </h1>
          <p className="withdraw-desc">
            {t('withdraw.descLine1')}<br />{t('withdraw.descLine2')}
          </p>

          <div className="withdraw-reasons">
            {REASONS.map(({ code, i18nKey }) => {
              const selected = selectedReason === code

              return (
                <div key={code} className="withdraw-reason-row" onClick={() => toggleReason(code)}>
                  <span className={`withdraw-reason-box ${selected ? 'selected' : ''}`}>
                    <svg width="11" height="9" viewBox="0 0 11 9" fill="none" style={{ display: selected ? 'block' : 'none' }}>
                      <path
                        d="M1 4.5L4 7.5L10 1.2"
                        stroke="#f8f9fa"
                        strokeWidth="1.8"
                        strokeLinecap="round"
                        strokeLinejoin="round"
                      />
                    </svg>
                  </span>
                  <span className="withdraw-reason-label">{t(`withdraw.${i18nKey}`)}</span>
                </div>
              )
            })}
          </div>

          <textarea
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder={t('withdraw.notePlaceholder')}
            maxLength={NOTE_MAX_LENGTH}
            className="withdraw-note"
          />

          <div className="withdraw-actions">
            <button type="button" className="withdraw-continue" onClick={handleContinue}>
              {t('withdraw.continueBtn')}
            </button>
            <button type="button" className="withdraw-next" onClick={handleNext}>
              {t('withdraw.nextBtn')}
            </button>
          </div>

          <div className="withdraw-spacer" />

          <Footer marginTop="0" padding="24px 0 40px" />
        </div>

        <BottomNav />
      </section>
    </main>
  )
}
