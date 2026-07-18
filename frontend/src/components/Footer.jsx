import { useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import LanguageToggle from './LanguageToggle'
import './Footer.css'

export default function Footer({ marginTop = '20px', padding = '24px 4px 40px' }) {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()

  return (
    <footer className="app-footer" style={{ marginTop, padding }}>
      <div className="app-footer-links">
        <span
          className={location.pathname === '/privacy' ? 'active' : ''}
          onClick={() => navigate('/privacy')}
        >
          {t('common.privacyPolicy')}
        </span>
        <span
          className={location.pathname === '/score-policy' ? 'active' : ''}
          onClick={() => navigate('/score-policy')}
        >
          {t('common.scorePolicy')}
        </span>
        <span>{t('common.contactUs')}</span>
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
