import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'

export default function Register() {
  const { login } = useAuth()
  const [form, setForm] = useState({ displayName: '', username: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const navigate = useNavigate()

  function update(key) {
    return (e) => setForm((f) => ({ ...f, [key]: e.target.value }))
  }

  async function submit(e) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await api.post('/users/register', {
        displayName: form.displayName.trim(),
        username: form.username.trim(),
        password: form.password,
      })
      await login(form.username.trim(), form.password)
      navigate('/')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-screen">
      <div className="auth-card">
        <div className="logo-badge">L</div>
        <h1 className="auth-title">Tạo tài khoản</h1>
        <p className="auth-subtitle">Tham gia và chia sẻ khoảnh khắc với bạn bè.</p>

        <form onSubmit={submit} className="auth-form">
          <input
            className="field"
            placeholder="Tên hiển thị"
            value={form.displayName}
            onChange={update('displayName')}
            required
          />
          <input
            className="field"
            placeholder="Tên đăng nhập"
            value={form.username}
            onChange={update('username')}
            autoComplete="username"
            minLength={3}
            required
          />
          <input
            className="field"
            type="password"
            placeholder="Mật khẩu (tối thiểu 6 ký tự)"
            value={form.password}
            onChange={update('password')}
            autoComplete="new-password"
            minLength={6}
            required
          />
          {error && <div className="error-banner">{error}</div>}
          <button className="btn-primary" disabled={busy}>
            {busy ? 'Đang tạo…' : 'Đăng ký'}
          </button>
        </form>

        <p className="auth-switch">
          Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
        </p>
      </div>
    </div>
  )
}
