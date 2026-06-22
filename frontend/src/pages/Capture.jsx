import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api.js'

export default function Capture() {
  const videoRef = useRef(null)
  const streamRef = useRef(null)
  const fileInputRef = useRef(null)

  const [photo, setPhoto] = useState(null) // { blob, url }
  const [caption, setCaption] = useState('')
  const [camError, setCamError] = useState(false)
  const [sending, setSending] = useState(false)
  const [sent, setSent] = useState(false)
  const [error, setError] = useState('')

  const navigate = useNavigate()

  // Start the webcam whenever we are in "live preview" mode (no captured photo yet).
  useEffect(() => {
    let active = true

    async function start() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: 'user', width: { ideal: 1080 }, height: { ideal: 1080 } },
          audio: false,
        })
        if (!active) {
          stream.getTracks().forEach((t) => t.stop())
          return
        }
        streamRef.current = stream
        if (videoRef.current) videoRef.current.srcObject = stream
      } catch {
        if (active) setCamError(true)
      }
    }

    if (!photo) start()
    return () => {
      active = false
      stopCamera()
    }
  }, [photo])

  function stopCamera() {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((t) => t.stop())
      streamRef.current = null
    }
  }

  function capture() {
    const video = videoRef.current
    if (!video || !video.videoWidth) return

    const size = Math.min(video.videoWidth, video.videoHeight)
    const canvas = document.createElement('canvas')
    canvas.width = size
    canvas.height = size
    const ctx = canvas.getContext('2d')

    const sx = (video.videoWidth - size) / 2
    const sy = (video.videoHeight - size) / 2
    // Mirror horizontally so the selfie matches what the user sees in the preview.
    ctx.translate(size, 0)
    ctx.scale(-1, 1)
    ctx.drawImage(video, sx, sy, size, size, 0, 0, size, size)

    canvas.toBlob(
      (blob) => {
        if (blob) {
          stopCamera()
          setPhoto({ blob, url: URL.createObjectURL(blob) })
        }
      },
      'image/jpeg',
      0.9,
    )
  }

  function onFile(e) {
    const file = e.target.files?.[0]
    if (file) {
      stopCamera()
      setPhoto({ blob: file, url: URL.createObjectURL(file) })
    }
  }

  function retake() {
    if (photo?.url) URL.revokeObjectURL(photo.url)
    setPhoto(null)
    setCaption('')
    setError('')
    setSent(false)
  }

  async function send() {
    if (!photo || sending) return
    setSending(true)
    setError('')
    try {
      const formData = new FormData()
      const filename = photo.blob.name || 'moment.jpg'
      formData.append('image', photo.blob, filename)
      if (caption.trim()) formData.append('caption', caption.trim())
      await api.postForm('/moments', formData)
      setSent(true)
      setTimeout(() => navigate('/feed'), 800)
    } catch (err) {
      setError(err.message || 'Gửi thất bại')
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="page capture-page">
      <div className="viewfinder">
        {photo ? (
          <img src={photo.url} alt="Ảnh đã chụp" className="viewfinder-media" />
        ) : camError ? (
          <div className="cam-fallback">
            <span className="cam-fallback-emoji">📷</span>
            <p>Không truy cập được camera.</p>
            <button className="btn-ghost" onClick={() => fileInputRef.current?.click()}>
              Chọn ảnh từ thiết bị
            </button>
          </div>
        ) : (
          <video ref={videoRef} className="viewfinder-media mirrored" autoPlay playsInline muted />
        )}

        {caption && photo && (
          <div className="moment-caption viewfinder-caption">
            <span>{caption}</span>
          </div>
        )}
      </div>

      {!photo ? (
        <div className="capture-controls">
          <button
            className="gallery-btn"
            onClick={() => fileInputRef.current?.click()}
            aria-label="Chọn từ thư viện"
          >
            🖼️
          </button>
          <button
            className="shutter"
            onClick={capture}
            disabled={camError}
            aria-label="Chụp ảnh"
          >
            <span className="shutter-ring" />
          </button>
          <div className="control-spacer" />
        </div>
      ) : (
        <div className="compose">
          <input
            className="caption-input"
            placeholder="Thêm tin nhắn…"
            value={caption}
            maxLength={120}
            onChange={(e) => setCaption(e.target.value)}
          />
          {error && <div className="error-banner">{error}</div>}
          <div className="compose-actions">
            <button className="btn-ghost" onClick={retake} disabled={sending}>
              ↺ Chụp lại
            </button>
            <button className="btn-primary send-btn" onClick={send} disabled={sending || sent}>
              {sent ? 'Đã gửi ✓' : sending ? 'Đang gửi…' : 'Gửi 🚀'}
            </button>
          </div>
        </div>
      )}

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        hidden
        onChange={onFile}
      />
    </div>
  )
}
