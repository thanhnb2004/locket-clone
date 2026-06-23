import { NavLink } from 'react-router-dom'

export default function BottomNav() {
  return (
    <nav className="bottom-nav">
      <NavLink to="/feed" className="nav-item" aria-label="Khoảnh khắc">
        <span className="nav-icon">🗂️</span>
        <span className="nav-label">Khoảnh khắc</span>
      </NavLink>

      <NavLink to="/" end className="nav-item nav-capture" aria-label="Trang chủ">
        <span className="capture-dot">🏠</span>
      </NavLink>

      <NavLink to="/friends" className="nav-item" aria-label="Bạn bè">
        <span className="nav-icon">👥</span>
        <span className="nav-label">Bạn bè</span>
      </NavLink>
    </nav>
  )
}
