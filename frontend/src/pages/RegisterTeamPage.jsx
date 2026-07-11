import { useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import './RegisterTeamPage.css'

import { saveRoster } from '../api/teams'
import LanguageToggle from '../components/LanguageToggle'

const POS_LABEL = {
  Top: 'TOP',
  Jungle: 'JUG',
  Mid: 'MID',
  Bot: 'ADC',
  Support: 'SPT',
}

const PLAYER_POINT = 10

const BANNED = ['시발', '씨발', '병신', '새끼', '좆', '존나', 'ㅅㅂ', 'ㅄ', 'fuck', 'shit']

export default function RegisterTeamPage({ user, onTeamCreated }) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()
  const selectedPlayers = location.state?.selectedPlayers

  const [teamName, setTeamName] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [showDone, setShowDone] = useState(false)

  const totalValue = (selectedPlayers?.length ?? 0) * PLAYER_POINT

  const { nameAlertText, nameAlertColor, isValid } = useMemo(() => {
    const hasProfanity = BANNED.some(word => teamName.toLowerCase().includes(word))
    const tooLong = teamName.length > 10
    const empty = teamName.trim().length === 0

    if (hasProfanity) {
      return { nameAlertText: t('registerTeam.alertProfanity'), nameAlertColor: '#e11d2e', isValid: false }
    }
    if (tooLong) {
      return { nameAlertText: t('registerTeam.alertTooLong'), nameAlertColor: '#e11d2e', isValid: false }
    }
    return { nameAlertText: t('registerTeam.alertTooLong'), nameAlertColor: '#9a9a9e', isValid: !empty }
  }, [teamName, t])

  if (!selectedPlayers) {
    navigate('/roster')
    return null
  }

  const handleSubmit = async () => {
    if (!isValid || isSubmitting) return

    try {
      setIsSubmitting(true)
      setErrorMessage('')

      await saveRoster({
        teamName,
        playerIds: selectedPlayers.map(player => player.playerId),
      })

      await onTeamCreated?.()
      setShowDone(true)
    } catch (error) {
      console.error(error)
      setErrorMessage(error.message || t('registerTeam.errorGeneric'))
    } finally {
      setIsSubmitting(false)
    }
  }

  if (showDone) {
    return (
      <main className="register-page">
        <section className="register-frame">
          <header className="register-header">
            <div className="register-back-spacer" />
            <span className="register-header-title">{t('registerTeam.headerTitle')}</span>
          </header>

          <div className="register-done-body">
            <h1 className="register-done-title">
              {t('registerTeam.doneTitleLine1')}<br />{t('registerTeam.doneTitleLine2')}
            </h1>

            <div className="register-done-card">
              <div className="register-done-eyebrow">{t('registerTeam.doneEyebrow')}</div>
              <div className="register-done-row">
                <span className="register-done-label">{t('registerTeam.doneTeamLabel')}</span>
                <span className="register-done-team">{teamName}</span>
              </div>
              <div className="register-done-row">
                <span className="register-done-label">{t('registerTeam.doneOwnerLabel')}</span>
                <span className="register-done-owner">{user?.username}</span>
              </div>
            </div>

            <p className="register-done-desc">
              {t('registerTeam.doneDesc')}
            </p>
          </div>

          <div className="register-done-actions">
            <button type="button" className="register-done-home" onClick={() => navigate('/')}>
              {t('registerTeam.doneHomeBtn')}
            </button>
            <button type="button" className="register-done-lineup" onClick={() => navigate('/starters')}>
              {t('registerTeam.doneLineupBtn')}
            </button>
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="register-page">
      <section className="register-frame">
        <header className="register-header">
          <button type="button" className="register-back" onClick={() => navigate('/roster')}>
            <svg width="9" height="15" viewBox="0 0 9 15" fill="none">
              <path d="M8 1L1.5 7.5L8 14" stroke="#0b0b0c" strokeWidth="1.8" strokeLinecap="round" />
            </svg>
          </button>
          <span className="register-header-title">{t('registerTeam.headerTitle')}</span>
        </header>

        <div className="register-intro">
          <h2>
            {t('registerTeam.introLine1')}<br />{t('registerTeam.introLine2')}
          </h2>
        </div>

        <div className="register-input-area">
          <input
            type="text"
            value={teamName}
            onChange={(e) => setTeamName(e.target.value)}
            placeholder={t('registerTeam.inputPlaceholder')}
            className="register-input"
          />
          <div className="register-input-alert" style={{ color: nameAlertColor }}>
            {nameAlertText}
          </div>
        </div>

        <div className="register-list-header">
          <span>{t('registerTeam.listHeaderLabel')}</span>
          <span className="register-value-label">
            {t('registerTeam.valueLabel')} <span className="register-value-badge">{totalValue}P</span>
          </span>
        </div>

        <div className="register-list">
          {selectedPlayers.map(player => (
            <div className="register-list-row" key={player.playerId}>
              <span className="register-list-name">{player.playerName}</span>
              <span className="register-list-pos">{POS_LABEL[player.position]}</span>
              <span className="register-list-score">{player.lastSeasonScore ?? '0,321'}</span>
              <span className="register-list-spacer" />
              <span className="register-list-point">{PLAYER_POINT}P</span>
            </div>
          ))}

          <footer className="register-footer">
            <div>
              <span>{t('common.privacyPolicy')}</span>
              <span style={{ color: '#9a9a9e' }}>{t('common.scorePolicy')}</span>
              <span style={{ color: '#9a9a9e' }}>{t('common.contactUs')}</span>
              <LanguageToggle />
            </div>
            <p>
              {t('common.disclaimer')}
            </p>
            <div className="register-footer-wordmark">{t('common.wordmark')}</div>
          </footer>
        </div>

        <div className="register-submit-area">
          {errorMessage && <p className="register-error">{errorMessage}</p>}
          <button
            type="button"
            className="register-submit"
            disabled={!isValid || isSubmitting}
            onClick={handleSubmit}
          >
            {t('registerTeam.submitBtn')}
          </button>
        </div>
      </section>
    </main>
  )
}
