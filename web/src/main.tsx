import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { LocaleProvider } from '@douyinfe/semi-ui-19'
import en_US from '@douyinfe/semi-ui-19/lib/es/locale/source/en_US'
import '../node_modules/@douyinfe/semi-ui-19/dist/css/semi.min.css'
import { BrowserRouter } from 'react-router-dom'
import './index.scss'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <LocaleProvider locale={en_US}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </LocaleProvider>
  </StrictMode>,
)
