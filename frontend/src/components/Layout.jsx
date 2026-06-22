import { Outlet } from 'react-router-dom'
import { useAuth } from '../auth.jsx'
import { initials } from '../utils.js'
import BottomNav from './BottomNav.jsx'

export default function Layout() {
  const { user, logout } = useAuth()

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="avatar" title={user.displayName}>
          {initials(user.displayName)}
        </div>
        <div className="brand">Locket</div>
        <button className="icon-btn" onClick={logout} title="Đăng xuất" aria-label="Đăng xuất">
          ⏻
        </button>
      </header>

      <main className="content">
        <Outlet />
      </main>

      <BottomNav />
    </div>
  )
}
