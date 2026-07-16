import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import Header from '../components/Header'
import Footer from '../components/Footer'
import BottomNav from '../components/BottomNav'
import './PrivacyPolicyPage.css'

export default function PrivacyPolicyPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()

  return (
    <main className="privacy-page">
      <section className="privacy-frame">
        <Header variant="back" title={t('privacyPolicy.headerTitle')} onBack={() => navigate(-1)} />

        <div className="privacy-scroll">
          <p className="privacy-intro">{t('privacyPolicy.intro')}</p>

          <div className="privacy-section">
            <div className="privacy-section-title">{t('privacyPolicy.section1Title')}</div>
            <p className="privacy-section-body">{t('privacyPolicy.section1Body')}</p>
            <ul className="privacy-list">
              <li>{t('privacyPolicy.section1Item1')}</li>
            </ul>
          </div>

          <div className="privacy-section">
            <div className="privacy-section-title">{t('privacyPolicy.section2Title')}</div>
            <p className="privacy-section-body">{t('privacyPolicy.section2Body')}</p>
            <ul className="privacy-list">
              <li>{t('privacyPolicy.section2Item1')}</li>
              <li>{t('privacyPolicy.section2Item2')}</li>
            </ul>
          </div>

          <div className="privacy-section">
            <div className="privacy-section-title">{t('privacyPolicy.section3Title')}</div>
            <p className="privacy-section-body">{t('privacyPolicy.section3Body')}</p>
            <ul className="privacy-list">
              <li>{t('privacyPolicy.section3Item1')}</li>
              <li>{t('privacyPolicy.section3Item2')}</li>
            </ul>
          </div>

          <div className="privacy-section">
            <div className="privacy-section-title">{t('privacyPolicy.section4Title')}</div>
            <p className="privacy-section-body">{t('privacyPolicy.section4Body')}</p>
          </div>

          <div className="privacy-section">
            <div className="privacy-section-title">{t('privacyPolicy.section5Title')}</div>
            <p className="privacy-section-body">{t('privacyPolicy.section5Body')}</p>
          </div>

          <div className="privacy-dates">
            <p>
              {t('privacyPolicy.noticeDate')}
              <br />
              {t('privacyPolicy.effectiveDate')}
            </p>
          </div>

          <div className="privacy-spacer" />
          <Footer marginTop="20px" padding="20px 0 40px" />
        </div>

        <BottomNav />
      </section>
    </main>
  )
}
