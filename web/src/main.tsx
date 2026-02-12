import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '../node_modules/@douyinfe/semi-ui-19/dist/css/semi.min.css'
import { BrowserRouter } from 'react-router-dom'
import './index.scss'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
