import { useEffect, useState, useCallback } from 'react'
import { api } from '../api.js'
import { initials } from '../utils.js'

export default function Friends() {
  const [friends, setFriends] = useState([])
  const [incoming, setIncoming] = useState([])
  const [outgoing, setOutgoing] = useState([])
  const [username, setUsername] = useState('')
  const [message, setMessage] = useState(null) // { type, text }
  const [loading, setLoading] = useState(true)

  const load = useCallback(async () => {
    const [f, inc, out] = await Promise.all([
      api.get('/friends'),
      api.get('/friends/requests/incoming'),
      api.get('/friends/requests/outgoing'),
    ])
    setFriends(f)
    setIncoming(inc)
    setOutgoing(out)
  }, [])

  useEffect(() => {
    load()
      .catch((e) => setMessage({ type: 'error', text: e.message }))
      .finally(() => setLoading(false))
  }, [load])

  async function sendRequest(e) {
    e.preventDefault()
    if (!username.trim()) return
    try {
      await api.post('/friends/requests', { username: username.trim() })
      setUsername('')
      setMessage({ type: 'success', text: 'Đã gửi lời mời kết bạn!' })
      await load()
    } catch (err) {
      setMessage({ type: 'error', text: err.message })
    }
  }

  async function respond(id, accept) {
    try {
      await api.post(`/friends/requests/${id}/${accept ? 'accept' : 'reject'}`)
      await load()
    } catch (err) {
      setMessage({ type: 'error', text: err.message })
    }
  }

  return (
    <div className="page friends-page">
      <h2 className="page-title">Bạn bè</h2>

      <form onSubmit={sendRequest} className="add-friend">
        <input
          className="field"
          placeholder="Thêm bạn bằng tên đăng nhập…"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <button className="btn-primary compact">Thêm</button>
      </form>

      {message && <div className={`error-banner ${message.type}`}>{message.text}</div>}

      {loading ? (
        <div className="muted center">Đang tải…</div>
      ) : (
        <>
          {incoming.length > 0 && (
            <section className="friend-section">
              <h3 className="section-label">Lời mời đến bạn</h3>
              {incoming.map((req) => (
                <div className="friend-row" key={req.id}>
                  <div className="avatar small">{initials(req.requester.displayName)}</div>
                  <div className="friend-name">
                    <strong>{req.requester.displayName}</strong>
                    <span className="muted">@{req.requester.username}</span>
                  </div>
                  <div className="row-actions">
                    <button className="btn-accept" onClick={() => respond(req.id, true)}>
                      Chấp nhận
                    </button>
                    <button className="btn-reject" onClick={() => respond(req.id, false)}>
                      Từ chối
                    </button>
                  </div>
                </div>
              ))}
            </section>
          )}

          <section className="friend-section">
            <h3 className="section-label">Bạn bè ({friends.length})</h3>
            {friends.length === 0 ? (
              <p className="muted">Chưa có bạn bè nào. Hãy thêm ai đó ở trên!</p>
            ) : (
              friends.map((friend) => (
                <div className="friend-row" key={friend.id}>
                  <div className="avatar small">{initials(friend.displayName)}</div>
                  <div className="friend-name">
                    <strong>{friend.displayName}</strong>
                    <span className="muted">@{friend.username}</span>
                  </div>
                </div>
              ))
            )}
          </section>

          {outgoing.length > 0 && (
            <section className="friend-section">
              <h3 className="section-label">Đã gửi, đang chờ</h3>
              {outgoing.map((req) => (
                <div className="friend-row" key={req.id}>
                  <div className="avatar small">{initials(req.addressee.displayName)}</div>
                  <div className="friend-name">
                    <strong>{req.addressee.displayName}</strong>
                    <span className="muted">@{req.addressee.username}</span>
                  </div>
                  <span className="pending-badge">Đang chờ</span>
                </div>
              ))}
            </section>
          )}
        </>
      )}
    </div>
  )
}
