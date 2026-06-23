import { useState } from 'react'
import AuthedImage from './AuthedImage.jsx'
import { api } from '../api.js'
import { useAuth } from '../auth.jsx'
import { timeAgo, initials } from '../utils.js'

const EMOJIS = ['❤️', '😂', '😮', '😢', '🔥', '👍']

export default function MomentCard({ moment: initialMoment }) {
  const { user } = useAuth()
  const [moment, setMoment] = useState(initialMoment)
  const [pickerOpen, setPickerOpen] = useState(false)
  const [viewerOpen, setViewerOpen] = useState(false)
  const [busy, setBusy] = useState(false)

  const reactions = moment.reactions || []
  const isMine = user && moment.owner.username === user.username

  // Group reactions by emoji so we can show "❤️ 3" style chips.
  const counts = reactions.reduce((acc, r) => {
    acc[r.emoji] = (acc[r.emoji] || 0) + 1
    return acc
  }, {})

  async function choose(emoji) {
    if (busy) return
    setPickerOpen(false)
    setBusy(true)
    try {
      // Tapping your current reaction again removes it (toggle off).
      const updated =
        moment.myReaction === emoji
          ? await api.del(`/moments/${moment.id}/reactions`)
          : await api.post(`/moments/${moment.id}/reactions`, { emoji })
      setMoment(updated)
    } catch (e) {
      // Keep the previous state on failure; the user can retry.
      console.error('Reaction failed:', e.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <article className="moment-card">
      <div className="moment-photo">
        <AuthedImage
          path={`/moments/${moment.id}/image`}
          alt={moment.caption || 'moment'}
          className="moment-img"
        />
        {moment.caption && (
          <div className="moment-caption">
            <span>{moment.caption}</span>
          </div>
        )}
      </div>

      {/* Reaction chips + picker. You can react to friends' moments, not your own.
          Tapping a chip on your own moment opens the list of who reacted instead. */}
      <div className="reaction-bar">
        {Object.entries(counts).map(([emoji, count]) => (
          <button
            key={emoji}
            type="button"
            className={`reaction-chip${moment.myReaction === emoji ? ' mine' : ''}`}
            onClick={() => (isMine ? setViewerOpen(true) : choose(emoji))}
            disabled={busy}
            title={reactions.filter((r) => r.emoji === emoji).map((r) => r.user.displayName).join(', ')}
          >
            <span className="reaction-emoji">{emoji}</span>
            <span className="reaction-count">{count}</span>
          </button>
        ))}

        {reactions.length > 0 && (
          <button
            type="button"
            className="reaction-viewers"
            onClick={() => setViewerOpen(true)}
            aria-label="Xem ai đã thả cảm xúc"
          >
            👀 {reactions.length}
          </button>
        )}

        {!isMine && (
          <div className="reaction-add-wrap">
            <button
              type="button"
              className="reaction-add"
              onClick={() => setPickerOpen((o) => !o)}
              disabled={busy}
              aria-label="Thả cảm xúc"
            >
              {moment.myReaction ? moment.myReaction : '🙂﹢'}
            </button>
            {pickerOpen && (
              <div className="reaction-picker">
                {EMOJIS.map((emoji) => (
                  <button
                    key={emoji}
                    type="button"
                    className={`reaction-option${moment.myReaction === emoji ? ' active' : ''}`}
                    onClick={() => choose(emoji)}
                  >
                    {emoji}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      <div className="moment-meta">
        <div className="avatar small">{initials(moment.owner.displayName)}</div>
        <div className="moment-who">
          <strong>{moment.owner.displayName}</strong>
          <span className="muted">
            @{moment.owner.username} · {timeAgo(moment.createdAt)}
          </span>
        </div>
      </div>

      {viewerOpen && (
        <div
          className="reactors-overlay"
          onClick={() => setViewerOpen(false)}
          role="presentation"
        >
          <div
            className="reactors-sheet"
            onClick={(e) => e.stopPropagation()}
            role="dialog"
            aria-label="Những người đã thả cảm xúc"
          >
            <div className="reactors-head">
              <strong>Cảm xúc ({reactions.length})</strong>
              <button
                type="button"
                className="reactors-close"
                onClick={() => setViewerOpen(false)}
                aria-label="Đóng"
              >
                ✕
              </button>
            </div>

            {reactions.length === 0 ? (
              <p className="muted center">Chưa có ai thả cảm xúc.</p>
            ) : (
              <ul className="reactors-list">
                {reactions.map((r) => (
                  <li className="reactor-row" key={r.id}>
                    <div className="avatar small">{initials(r.user.displayName)}</div>
                    <div className="friend-name">
                      <strong>{r.user.displayName}</strong>
                      <span className="muted">@{r.user.username}</span>
                    </div>
                    <span className="reactor-emoji">{r.emoji}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      )}
    </article>
  )
}
