import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import Header from '../components/Header'
import Footer from '../components/Footer'
import BottomNav from '../components/BottomNav'
import './ScorePolicyPage.css'

const PLAYER_SCORE_FORMULA = [
  'kills × 3',
  '+ assists × 1',
  '− deaths × 1',
  '+ win_bonus × 5',
  '+ cs × 0.01',
  '+ damage_to_champions × 0.001',
  '+ vision_score × 0.2',
]

export default function ScorePolicyPage() {
  const { t } = useTranslation()
  const navigate = useNavigate()

  return (
    <main className="score-policy-page">
      <section className="score-policy-frame">
        <Header variant="back" title={t('scorePolicy.headerTitle')} onBack={() => navigate(-1)} />

        <div className="score-policy-scroll">
          <div className="score-policy-section" style={{ marginTop: 26 }}>
            <div className="score-policy-section-title">{t('scorePolicy.section1Title')}</div>
            <p className="score-policy-section-body">{t('scorePolicy.section1Body')}</p>

            <div className="score-policy-formula-box">
              {PLAYER_SCORE_FORMULA.map((line) => (
                <div className="score-policy-formula-row" key={line}>
                  <span>{line}</span>
                </div>
              ))}
            </div>

            <div className="score-policy-note">
              <p>{t('scorePolicy.section1Note')}</p>
            </div>
          </div>

          <div className="score-policy-section" style={{ marginTop: 32 }}>
            <div className="score-policy-section-title">{t('scorePolicy.section2Title')}</div>
            <div className="score-policy-coming-soon">
              <span>{t('scorePolicy.section2ComingSoon')}</span>
            </div>
          </div>

          <div className="score-policy-section" style={{ marginTop: 24 }}>
            <div className="score-policy-section-title">{t('scorePolicy.section3Title')}</div>
            <p className="score-policy-section-body">{t('scorePolicy.section3Body')}</p>
            <div className="score-policy-formula-box">
              <div className="score-policy-formula-summary">{t('scorePolicy.section3Formula')}</div>
            </div>
          </div>

          <div className="score-policy-spacer" />
          <Footer marginTop="28px" padding="20px 0 40px" />
        </div>

        <BottomNav />
      </section>
    </main>
  )
}
