import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { api, saveCredentials, clearCredentials, getToken } from './api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  // On first load, if we have stored credentials, verify them by fetching the profile.
  useEffect(() => {
    if (!getToken()) {
      setLoading(false)
      return
    }
    api
      .get('/users/me')
      .then(setUser)
      .catch(() => clearCredentials())
      .finally(() => setLoading(false))
  }, [])

  const login = useCallback(async (username, password) => {
    saveCredentials(username, password)
    try {
      const me = await api.get('/users/me')
      setUser(me)
      return me
    } catch (error) {
      clearCredentials()
      throw error
    }
  }, [])

  const logout = useCallback(() => {
    clearCredentials()
    setUser(null)
  }, [])

  return (
    <AuthContext.Provider value={{ user, setUser, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
