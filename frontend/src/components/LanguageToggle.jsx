import { useEffect, useRef, useState } from 'react'
import { useTranslation } from 'react-i18next'
import './LanguageToggle.css'

export default function LanguageToggle() {
  const { i18n } = useTranslation()
  const [open, setOpen] = useState(false)
  const rootRef = useRef(null)

  useEffect(() => {
    if (!open) return

    const onClickOutside = (event) => {
      if (rootRef.current && !rootRef.current.contains(event.target)) {
        setOpen(false)
      }
    }

    document.addEventListener('mousedown', onClickOutside)
    return () => document.removeEventListener('mousedown', onClickOutside)
  }, [open])

  const setLang = (lng) => {
    i18n.changeLanguage(lng)
    setOpen(false)
  }

  const currentLabel = i18n.language === 'en' ? 'EN' : 'KR'

  return (
    <div className="lang-toggle" ref={rootRef}>
      <div className="lang-toggle-trigger" onClick={() => setOpen(prev => !prev)}>
        <span>{currentLabel}</span>
        <svg width="8" height="5" viewBox="0 0 8 5" fill="none">
          <path d="M1 1l3 3 3-3" stroke="#6a6a6f" strokeWidth="1.3" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      </div>

      {open && (
        <div className="lang-toggle-menu">
          <div onClick={() => setLang('ko')}>KR</div>
          <div onClick={() => setLang('en')}>EN</div>
        </div>
      )}
    </div>
  )
}
