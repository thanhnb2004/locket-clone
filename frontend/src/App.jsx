import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './auth.jsx'
import Layout from './components/Layout.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Capture from './pages/Capture.jsx'
import Feed from './pages/Feed.jsx'
import Friends from './pages/Friends.jsx'

function Protected({ children }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="full-loading">Đang tải…</div>
  if (!user) return <Navigate to="/login" replace />
  return children
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route
        element={
          <Protected>
            <Layout />
          </Protected>
        }
      >
        <Route path="/" element={<Capture />} />
        <Route path="/feed" element={<Feed />} />
        <Route path="/friends" element={<Friends />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
