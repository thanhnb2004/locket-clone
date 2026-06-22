import { useEffect, useState } from 'react'
import { api } from '../api.js'
import MomentCard from '../components/MomentCard.jsx'

export default function Feed() {
  const [moments, setMoments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    api
      .get('/moments/feed')
      .then(setMoments)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="page feed-page">
      <h2 className="page-title">Khoảnh khắc</h2>

      {loading && <div className="muted center">Đang tải…</div>}
      {error && <div className="error-banner">{error}</div>}
      {!loading && !error && moments.length === 0 && (
        <div className="empty">
          <div className="empty-emoji">📸</div>
          <p>Chưa có khoảnh khắc nào.</p>
          <p className="muted">Hãy chụp tấm đầu tiên hoặc kết bạn để xem ảnh của họ!</p>
        </div>
      )}

      <div className="feed-list">
        {moments.map((m) => (
          <MomentCard key={m.id} moment={m} />
        ))}
      </div>
    </div>
  )
}
