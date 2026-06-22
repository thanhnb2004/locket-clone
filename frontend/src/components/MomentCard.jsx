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

      {/* Reaction chips + picker. You can react to friends' moments, not your own. */}
      <div className="reaction-bar">
        {Object.entries(counts).map(([emoji, count]) => (
          <button
            key={emoji}
            type="button"
            className={`reaction-chip${moment.myReaction === emoji ? ' mine' : ''}`}
            onClick={() => !isMine && choose(emoji)}
            disabled={isMine || busy}
            title={reactions.filter((r) => r.emoji === emoji).map((r) => r.user.displayName).join(', ')}
          >
            <span className="reaction-emoji">{emoji}</span>
            <span className="reaction-count">{count}</span>
          </button>
        ))}

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
    </article>
  )
}
