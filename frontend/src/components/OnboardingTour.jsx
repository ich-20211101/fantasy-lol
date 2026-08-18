import { useLayoutEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import Header from './Header'
import { POSITIONS, POS_LABEL } from '../constants/positions'
import './OnboardingTour.css'

const TOTAL_STEPS = 4

// exact names/teams confirmed against a screenshot of the actual design
// canvas: step 1 defaults to the MID tab with Showmaker + Faker pre-selected
// and a static "80 P" remaining pill. Score badges are all hardcoded to 21.
const STEP1_PLAYERS = [
  { name: 'Showmaker', sub: 'Dplus Kia | MID', score: '21', selected: true },
  { name: 'Faker', sub: 'T1 | MID', score: '21', selected: true },
  { name: 'Chovy', sub: 'Gen.G | MID', score: '21', selected: false },
  { name: 'Zeka', sub: 'Hanwha Life | MID', score: '21', selected: false },
  { name: 'Bdd', sub: 'KT Rolster | MID', score: '21', selected: false },
  { name: 'Ucal', sub: 'DN Freecs | MID', score: '21', selected: false },
]
// unselected row spotlighted by the "add players" callout — its toggle icon
// is "+", matching the design's guidance copy
const SPOTLIGHT_PLAYER_NAME = 'Chovy'

const STEP2_SLOTS = [
  { position: 'Top', name: 'Zeus', sub: 'T1 | TOP', score: '21' },
  { position: 'Jungle', name: 'Canyon', sub: 'Gen.G | JUG', score: '21' },
  { position: 'Mid', name: 'Showmaker', sub: 'Dplus Kia | MID', score: '21' },
  { position: 'Bot', name: 'Peyz', sub: 'Gen.G | ADC', score: '21' },
  { position: 'Support', name: 'Keria', sub: 'T1 | SUP', score: '21' },
]

const STEP4_STARTERS = [
  { position: 'Top', name: 'Siwoo', sub: 'DK | TOP', score: '21' },
  { position: 'Jungle', name: 'Lucid', sub: 'DK | JUG', score: '21' },
]
const STEP4_EMPTY_POSITIONS = ['Mid', 'Bot', 'Support']
const STEP4_BENCH = [
  { name: 'Showmaker', sub: 'Dplus Kia | MID', score: '21' },
  { name: 'Peyz', sub: 'Gen.G | ADC', score: '21' },
  { name: 'Keria', sub: 'T1 | SUP', score: '21' },
]

// bottom-of-frame anchor shared by every step's primary-action hole + "up" callout.
// derived from the design's 390x844 reference canvas (hole bottom:56, callout bottom:132)
// and kept bottom-anchored (not top-anchored) so it still lines up on real, non-844 viewports.
const ACTION_CALLOUT_BOTTOM = 132

export default function OnboardingTour({ onClose }) {
  const { t } = useTranslation()
  const [step, setStep] = useState(0)
  const stepRef = useRef(null)
  const spotlightRef = useRef(null)
  const [spotlightTop, setSpotlightTop] = useState(null)

  const isLast = step === TOTAL_STEPS - 1
  const goPrev = () => setStep(prev => Math.max(0, prev - 1))
  const goNext = () => setStep(prev => Math.min(TOTAL_STEPS - 1, prev + 1))

  // measure the spotlighted row's real toggle button instead of guessing a
  // fixed px offset, so the callout stays lined up no matter what the list renders
  useLayoutEffect(() => {
    if (step !== 0 || !spotlightRef.current || !stepRef.current) return
    const target = spotlightRef.current.getBoundingClientRect()
    const container = stepRef.current.getBoundingClientRect()
    setSpotlightTop(target.top - container.top + target.height / 2)
  }, [step])

  return (
    <div className="tour-overlay">
      {step === 0 && (
        <div className="tour-step" ref={stepRef}>
          <Header variant="back" title={t('buildRoster.headerTitle')} showBackButton={false} />

          <section className="tour-round">
            <span>{t('onboarding.sampleSeason')}</span>
            <strong>80 P</strong>
          </section>

          <nav className="tour-tabs">
            {POSITIONS.map((position, index) => (
              <div className={`tour-tab ${position === 'Mid' ? 'active' : ''}`} key={position}>
                <div className="tour-tab-row">
                  <span>{POS_LABEL[position]}</span>
                  <svg width="13" height="10" viewBox="0 0 13 10" fill="none" className={position === 'Mid' ? 'filled' : ''}>
                    <path d="M1 5L4.7 8.6L12 1" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </div>
              </div>
            ))}
          </nav>

          <section className="tour-list">
            {STEP1_PLAYERS.map(player => (
              <article className={`tour-player ${player.selected ? 'selected' : ''}`} key={player.name}>
                <div className="tour-player-main">
                  <div className="tour-player-title">
                    <span>{player.name}</span>
                    <span className="tour-player-score">{player.score}</span>
                  </div>
                  <p>{player.sub}</p>
                </div>
                <strong className="tour-point">10P</strong>
                <span
                  ref={player.name === SPOTLIGHT_PLAYER_NAME ? spotlightRef : undefined}
                  className={`tour-toggle ${player.selected ? 'selected' : ''} ${player.name === SPOTLIGHT_PLAYER_NAME ? 'tour-toggle-spotlight' : ''}`}
                >
                  {player.selected ? (
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
                      <path d="M1.6 1.6L10.4 10.4M10.4 1.6L1.6 10.4" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" />
                    </svg>
                  ) : (
                    <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
                      <path d="M6.5 1.4V11.6M1.4 6.5H11.6" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" />
                    </svg>
                  )}
                </span>
              </article>
            ))}
          </section>

          <section className="tour-action">
            <button type="button">
              {t('buildRoster.ctaLabel')} <span>2/8</span>
            </button>
          </section>

          <div className="tour-overlay-layer">
            <div className="tour-hole" style={{ left: 20, right: 20, bottom: 56, height: 57 }} />

            <div className="tour-callout-up" style={{ left: 22, right: 22, bottom: ACTION_CALLOUT_BOTTOM }}>
              <div className="tour-bubble">
                <p>{t('onboarding.step1TooltipCtaLine1')}<br />{t('onboarding.step1TooltipCtaLine2')}</p>
              </div>
              <svg width="14" height="18" viewBox="0 0 14 18" fill="none"><path d="M7 1v14M2 11l5 5 5-5" stroke="#f8f9fa" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>
            </div>

            <div className="tour-callout-side" style={{ left: 60, top: spotlightTop ?? 352 }}>
              <svg width="14" height="18" viewBox="0 0 14 18" fill="none" style={{ transform: 'rotate(-90deg)' }}><path d="M7 1v14M2 11l5 5 5-5" stroke="#f8f9fa" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>
              <div className="tour-bubble" style={{ maxWidth: 270 }}>
                <p>{t('onboarding.step1TooltipListLine1')}<br />{t('onboarding.step1TooltipListLine2')}</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {step === 1 && (
        <div className="tour-step">
          <Header variant="back" title={t('myRoster.headerTitle')} showBackButton={false} />

          <section className="tour-round tour-round-solo">
            <span>{t('onboarding.sampleSeason')}</span>
          </section>

          <section className="tour-scroll">
            <div className="tour-dashboard">
              <div className="tour-dashboard-cell">
                <div className="tour-dashboard-label">{t('myRoster.squadStatus')}</div>
                <div className="tour-dashboard-value">8 <span>/ 8</span></div>
              </div>
              <div className="tour-dashboard-divider" />
              <div className="tour-dashboard-cell right">
                <div className="tour-dashboard-label">{t('myRoster.remaining')}</div>
                <div className="tour-dashboard-value">0 <span>P</span></div>
              </div>
            </div>

            {STEP2_SLOTS.map(slot => (
              <div className="tour-slot" key={slot.position}>
                <div className="tour-slot-label">{POS_LABEL[slot.position]}</div>
                <div className="tour-player filled">
                  <div className="tour-player-main">
                    <div className="tour-player-title">
                      <span>{slot.name}</span>
                      <span className="tour-player-score">{slot.score}</span>
                    </div>
                    <p>{slot.sub}</p>
                  </div>
                  <strong className="tour-point">10P</strong>
                  <span className="tour-remove">
                    <svg width="12" height="12" viewBox="0 0 12 12" fill="none"><path d="M1.6 1.6L10.4 10.4M10.4 1.6L1.6 10.4" stroke="currentColor" strokeWidth="1.9" strokeLinecap="round" /></svg>
                  </span>
                </div>
              </div>
            ))}
          </section>

          <section className="tour-action">
            <button type="button" className="tour-action-secondary">{t('myRoster.selectPlayersBtn')}</button>
            <button type="button" className="tour-btn-alt">{t('myRoster.buyRoster', { point: '100' })}</button>
          </section>

          <div className="tour-overlay-layer">
            <div className="tour-hole" style={{ left: 134, right: 20, bottom: 56, height: 54 }} />

            <div className="tour-callout-up" style={{ left: 22, right: 22, bottom: ACTION_CALLOUT_BOTTOM }}>
              <div className="tour-bubble">
                <p>{t('onboarding.step2TooltipLine1')}<br />{t('onboarding.step2TooltipLine2')}</p>
              </div>
              <svg width="14" height="18" viewBox="0 0 14 18" fill="none"><path d="M7 1v14M2 11l5 5 5-5" stroke="#f8f9fa" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>
            </div>
          </div>
        </div>
      )}

      {step === 2 && (
        <div className="tour-step">
          <Header variant="back" title={t('registerTeam.headerTitle')} showBackButton={false} />

          <section className="tour-done">
            <h1>{t('registerTeam.doneTitleLine1')}<br />{t('registerTeam.doneTitleLine2')}</h1>
            <p className="tour-done-desc">{t('registerTeam.doneDesc')}</p>

            <div className="tour-done-card">
              <span className="tour-done-eyebrow">{t('registerTeam.doneEyebrow')}</span>
              <div className="tour-done-row">
                <span className="tour-done-key">{t('registerTeam.doneTeamLabel')}</span>
                <span className="tour-done-val">{t('onboarding.sampleTeamName')}</span>
              </div>
              <div className="tour-done-row">
                <span className="tour-done-key">{t('registerTeam.doneOwnerLabel')}</span>
                <span className="tour-done-val">{t('onboarding.sampleOwnerName')}</span>
              </div>
            </div>

            <p className="tour-done-lead">{t('registerTeam.doneLeadLine1')}<br />{t('registerTeam.doneLeadLine2')}</p>
            <p className="tour-done-desc">{t('registerTeam.doneSubDesc')}</p>
          </section>

          <section className="tour-action">
            <button type="button" className="tour-btn-alt">{t('myTeam.ctaLabel')}</button>
          </section>

          <div className="tour-overlay-layer">
            <div className="tour-hole" style={{ left: 20, right: 20, bottom: 56, height: 54 }} />

            <div className="tour-callout-up" style={{ left: 22, right: 22, bottom: ACTION_CALLOUT_BOTTOM }}>
              <div className="tour-bubble">
                <p>{t('onboarding.step3TooltipLine1')}<br />{t('onboarding.step3TooltipLine2')}<br />{t('onboarding.step3TooltipLine3')}</p>
              </div>
              <svg width="14" height="18" viewBox="0 0 14 18" fill="none"><path d="M7 1v14M2 11l5 5 5-5" stroke="#f8f9fa" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>
            </div>
          </div>
        </div>
      )}

      {step === 3 && (
        <div className="tour-step">
          <Header variant="back" title={t('setLineup.headerTitle')} showBackButton={false} />
          <button type="button" className="tour-close-x" onClick={onClose} aria-label={t('myTeam.rankClose')}>
            <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
              <path d="M1.4 1.4L11.6 11.6M11.6 1.4L1.4 11.6" stroke="#f8f9fa" strokeWidth="1.8" strokeLinecap="round" />
            </svg>
          </button>

          <section className="tour-week">
            <div className="tour-week-info">
              <span className="tour-week-label">{t('setLineup.week', { week: 13 })}</span>
              <span className="tour-week-countdown">03:00:00</span>
            </div>
            <button type="button" className="tour-autofill">{t('setLineup.autoFill')}</button>
          </section>

          <section className="tour-scroll">
            <div className="tour-list-label">{t('onboarding.starterLabel')}</div>
            {STEP4_STARTERS.map(slot => (
              <div className="tour-player filled" key={slot.position}>
                <div className="tour-player-main">
                  <div className="tour-player-title">
                    <span>{slot.name}</span>
                    <span className="tour-player-score">{slot.score}</span>
                  </div>
                  <p>{slot.sub}</p>
                </div>
                <strong className="tour-point">10P</strong>
                <span className="tour-demote">
                  <svg width="13" height="13" viewBox="0 0 14 14" fill="none"><path d="M7 2v9M3.2 7.4L7 11.2l3.8-3.8" stroke="#0b0b0c" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" /></svg>
                </span>
              </div>
            ))}
            {STEP4_EMPTY_POSITIONS.map(position => (
              <div className="tour-slot-empty" key={position}>
                <span>{POS_LABEL[position]}</span>
              </div>
            ))}

            <div className="tour-divider" />
            <div className="tour-bench-row">
              <span className="tour-list-label">{t('myTeam.benchLabel')}</span>
              <span className="tour-bench-note">{t('onboarding.benchNote')}</span>
            </div>
            {STEP4_BENCH.map(player => (
              <div className="tour-player" key={player.name}>
                <div className="tour-player-main">
                  <div className="tour-player-title">
                    <span>{player.name}</span>
                    <span className="tour-player-score">{player.score}</span>
                  </div>
                  <p>{player.sub}</p>
                </div>
                <strong className="tour-point">10P</strong>
                <span className="tour-promote">
                  <svg width="13" height="13" viewBox="0 0 14 14" fill="none"><path d="M7 12V3M3.2 6.6L7 2.8l3.8 3.8" stroke="#6a6a6f" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" /></svg>
                </span>
              </div>
            ))}
          </section>

          <section className="tour-action">
            <button type="button" disabled>{t('onboarding.confirmLabel')}</button>
          </section>

          <div className="tour-overlay-layer">
            <div className="tour-hole" style={{ left: 22, top: 180, width: 343, height: 69 }} />

            {/* WEEK/카운트다운 복제본 (딤 레이어 위로 노출) */}
            <div className="tour-week-clone" style={{ left: 19, top: 76 }}>
              <span className="tour-week-label">{t('setLineup.week', { week: 13 })}</span>
              <span className="tour-week-countdown">03:00:00</span>
            </div>

            <div className="tour-callout-week" style={{ left: 148, width: 150, top: 77 }}>
              <svg width="14" height="18" viewBox="0 0 14 18" fill="none" style={{ transform: 'rotate(90deg)' }}><path d="M7 1v14M2 11l5 5 5-5" stroke="#f8f9fa" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>
              <div className="tour-bubble tour-bubble-sm">
                <p>{t('onboarding.step4TooltipWeekLine1')}<br />{t('onboarding.step4TooltipWeekLine2')}</p>
              </div>
            </div>

            <div className="tour-callout-starter" style={{ left: 22, right: 22, top: 259 }}>
              <svg width="14" height="18" viewBox="0 0 14 18" fill="none" style={{ transform: 'rotate(180deg)' }}><path d="M7 1v14M2 11l5 5 5-5" stroke="#f8f9fa" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>
              <div className="tour-bubble">
                <p>{t('onboarding.step4TooltipStarter')}</p>
              </div>
            </div>

            {/* 확정 버튼 복제본 (딤 레이어 위로 노출) */}
            <button type="button" className="tour-confirm-clone" style={{ left: 21, bottom: 56 }}>
              {t('onboarding.confirmLabel')}
            </button>

            <div className="tour-callout-up" style={{ left: 22, right: 22, bottom: ACTION_CALLOUT_BOTTOM }}>
              <div className="tour-bubble">
                <p>{t('onboarding.step4TooltipConfirmLine1')}<br />{t('onboarding.step4TooltipConfirmLine2')}</p>
              </div>
              <svg width="14" height="18" viewBox="0 0 14 18" fill="none"><path d="M7 1v14M2 11l5 5 5-5" stroke="#f8f9fa" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" /></svg>
            </div>
          </div>
        </div>
      )}

      <div className="tour-pagination">
        <button
          type="button"
          className="tour-arrow"
          onClick={goPrev}
          disabled={step === 0}
          aria-label={t('onboarding.prev')}
        >
          <svg width="6" height="10" viewBox="0 0 6 10" fill="none"><path d="M5 1L1 5l4 4" stroke="#f8f9fa" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" /></svg>
        </button>

        <div className="tour-dots">
          {Array.from({ length: TOTAL_STEPS }, (_, index) => (
            <span key={index} className={`tour-dot ${index === step ? 'active' : ''}`} />
          ))}
        </div>

        <button
          type="button"
          className="tour-arrow"
          onClick={goNext}
          disabled={isLast}
          aria-label={t('onboarding.next')}
        >
          <svg width="6" height="10" viewBox="0 0 6 10" fill="none"><path d="M1 1l4 4-4 4" stroke="#f8f9fa" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" /></svg>
        </button>

        {isLast && (
          <button type="button" className="tour-close-text" onClick={onClose}>
            {t('myTeam.rankClose')}
          </button>
        )}
      </div>
    </div>
  )
}
