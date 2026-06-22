import { useEffect, useState } from 'react'
import { api } from '../api.js'

// Image endpoints require the Basic auth header, so a plain <img src> won't work.
// We fetch the bytes as a blob and render them via an object URL.
export default function AuthedImage({ path, alt, className }) {
  const [url, setUrl] = useState(null)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let cancelled = false
    let objectUrl

    api
      .blob(path)
      .then((blob) => {
        if (cancelled) return
        objectUrl = URL.createObjectURL(blob)
        setUrl(objectUrl)
      })
      .catch(() => {
        if (!cancelled) setFailed(true)
      })

    return () => {
      cancelled = true
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [path])

  if (failed) return <div className={`img-skeleton ${className || ''}`}>⚠️</div>
  if (!url) return <div className={`img-skeleton ${className || ''}`} />
  return <img src={url} alt={alt || ''} className={className} />
}
