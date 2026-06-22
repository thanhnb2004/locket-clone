import AuthedImage from './AuthedImage.jsx'
import { timeAgo, initials } from '../utils.js'

export default function MomentCard({ moment }) {
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
