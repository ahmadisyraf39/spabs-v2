import { createContext, useCallback, useEffect, useState } from 'react'
import * as authApi from '../../lib/api/auth'
import { clearToken, getToken, setToken } from '../../lib/api/client'

export const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [status, setStatus] = useState('idle')

  const loadCurrentUser = useCallback(async () => {
    try {
      const me = await authApi.getMe()
      setUser(me)
      setStatus('authenticated')
      return me
    } catch {
      clearToken()
      setUser(null)
      setStatus('unauthenticated')
      return null
    }
  }, [])

  useEffect(() => {
    if (getToken()) {
      loadCurrentUser()
    } else {
      setStatus('unauthenticated')
    }
  }, [loadCurrentUser])

  const login = useCallback(
    async ({ email, password }) => {
      const { token } = await authApi.login({ email, password })
      setToken(token)
      return loadCurrentUser()
    },
    [loadCurrentUser],
  )

  const logout = useCallback(() => {
    clearToken()
    setUser(null)
    setStatus('unauthenticated')
  }, [])

  return (
    <AuthContext.Provider value={{ user, status, login, logout, refresh: loadCurrentUser }}>
      {children}
    </AuthContext.Provider>
  )
}
