import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import LanguageToggle from './LanguageToggle'
import './Footer.css'

export default function Footer({ marginTop = '20px', padding = '24px 4px 40px' }) {
  const { t } = useTranslation()
  const navigate = useNavigate()

  return (
    <footer className="app-footer" style={{ marginTop, padding }}>
      <div className="app-footer-links">
        <span onClick={() => navigate('/privacy')}>{t('common.privacyPolicy')}</span>
        <span onClick={() => navigate('/score-policy')}>{t('common.scorePolicy')}</span>
        <span style={{ color: '#9a9a9e' }}>{t('common.contactUs')}</span>
        <LanguageToggle />
      </div>
      <p>
        {t('common.disclaimer')}
      </p>
      <p>
        {t('common.copyright', { year: new Date().getFullYear() })}
      </p>
      <div className="app-footer-wordmark">{t('common.wordmark')}</div>
    </footer>
  )
}
