import React from 'react'
// @ts-ignore
import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'
import { AlertProvider } from './context/AlertContext'

// Polyfill for global used by sockjs-client
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
