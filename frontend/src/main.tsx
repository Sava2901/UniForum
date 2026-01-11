import React from 'react'

import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'
import { AlertProvider } from './context/AlertContext'


if (typeof (window as any).global === 'undefined') {
  (window as any).global = window;
}

ReactDOM.createRoot(document.getElementById('app') as HTMLElement).render(
  <React.StrictMode>
    <AlertProvider>
      <App />
    </AlertProvider>
  </React.StrictMode>,
)
