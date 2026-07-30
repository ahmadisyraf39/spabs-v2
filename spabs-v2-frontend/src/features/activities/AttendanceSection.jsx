import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { saveBulkAttendance } from '../../lib/api/attendances'
import { getAttendanceGridForActivity } from '../../lib/activityRelations'
import { useAuth } from '../auth/useAuth'
import { ADMIN_ROLES } from '../../lib/roles'
import { ATTENDANCE_STATUSES, attendanceStatusSelectClass } from './activityEnums'

export default function AttendanceSection({ activityId, teamId, activityDate, editable = true }) {
  const { user } = useAuth()
  const isAdmin = ADMIN_ROLES.includes(user?.role)
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const grid = await getAttendanceGridForActivity(activityId, teamId, activityDate)
        if (cancelled) return
        setRows(grid)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load attendance.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [activityId, teamId, activityDate])

  function handleStatusChange(playerId, value) {
    setSuccess(null)
    setRows((prev) => prev.map((r) => (r.playerId === playerId ? { ...r, status: value } : r)))
  }

  function handleNotesChange(playerId, value) {
    setSuccess(null)
    setRows((prev) => prev.map((r) => (r.playerId === playerId ? { ...r, notes: value } : r)))
  }

  function handleMarkAllPresent() {
    setSuccess(null)
    setRows((prev) => prev.map((r) => ({ ...r, status: 'PRESENT' })))
  }

  async function handleSave() {
    const entries = rows
      .filter((r) => r.status)
      .map((r) => ({ playerId: r.playerId, status: r.status, notes: r.notes || null }))
    if (entries.length === 0) {
      setError('Pick a status for at least one player before saving.')
      return
    }
    setSaving(true)
    setError(null)
    setSuccess(null)
    try {
      const saved = await saveBulkAttendance({ activityId: Number(activityId), entries })
      const savedByPlayerId = new Map(saved.map((s) => [s.playerId, s]))
      setRows((prev) =>
        prev.map((r) => {
          const s = savedByPlayerId.get(r.playerId)
          return s ? { ...r, attendanceId: s.id, status: s.status, notes: s.notes ?? '' } : r
        }),
      )
      setSuccess('Attendance saved.')
    } catch (err) {
      setError(err.message ?? 'Unable to save attendance.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="card bg-base-100 shadow-md">
      <div className="card-body">
        <div className="flex items-center justify-between">
          <h2 className="card-title text-lg">Attendance</h2>
          {editable && rows.length > 0 && (
            <div className="flex gap-2">
              <button type="button" className="btn btn-ghost btn-sm" onClick={handleMarkAllPresent}>
                Mark all Present
              </button>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={handleSave}
                disabled={saving}
              >
                {saving ? <span className="loading loading-spinner loading-xs" /> : 'Save attendance'}
              </button>
            </div>
          )}
        </div>

        {!editable && (
          <p className="text-base-content/60 text-sm">
            This session hasn't happened yet — attendance can be recorded once it starts.
          </p>
        )}

        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}
        {success && (
          <div role="alert" className="alert alert-success text-sm">
            <span>{success}</span>
          </div>
        )}

        {loading ? (
          <div className="flex justify-center py-6">
            <span className="loading loading-spinner text-primary" />
          </div>
        ) : rows.length === 0 ? (
          <p className="text-base-content/60 text-sm">No eligible players for this session.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="table">
              <thead>
                <tr>
                  <th>Player</th>
                  <th>Status</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.playerId}>
                    <td>
                      {isAdmin ? (
                        <Link to={`/players/${row.playerId}`} className="link link-primary text-sm">
                          {row.fullName}
                        </Link>
                      ) : (
                        <span className="text-sm">{row.fullName}</span>
                      )}
                    </td>
                    <td>
                      {editable ? (
                        <select
                          className={`select select-bordered select-sm ${attendanceStatusSelectClass(row.status)}`}
                          value={row.status}
                          onChange={(e) => handleStatusChange(row.playerId, e.target.value)}
                        >
                          <option value="">— not recorded —</option>
                          {ATTENDANCE_STATUSES.map((value) => (
                            <option key={value} value={value}>
                              {value}
                            </option>
                          ))}
                        </select>
                      ) : (
                        <span className="badge badge-ghost badge-sm">
                          {row.status || '— not recorded —'}
                        </span>
                      )}
                    </td>
                    <td>
                      {editable ? (
                        <input
                          type="text"
                          className="input input-bordered input-sm w-full"
                          value={row.notes}
                          onChange={(e) => handleNotesChange(row.playerId, e.target.value)}
                        />
                      ) : (
                        <span className="text-base-content/70 text-sm">{row.notes || '—'}</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
