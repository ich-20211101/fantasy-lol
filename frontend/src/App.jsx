import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import heroImg from './assets/hero.png'
import './App.css'

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
    <div style={{ textAlign: 'center', marginTop: '100px' }}>
      <h1>⚔️ Fantasy LoL</h1>
      <p>나만의 판타지 리그 오브 레전드</p>
    </div>
    </>
  )
}

export default App
