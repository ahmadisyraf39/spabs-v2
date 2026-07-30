import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import { createCoachTeam, deleteCoachTeam, updateCoachTeam } from '../../lib/api/coachTeams'
import { getCoachOptions, getCoachingStaffForTeam } from '../../lib/teamRelations'
import { COACH_TEAM_ROLES } from './teamEnums'

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

export default function CoachSection({ teamId }) {
  const [staff, setStaff] = useState([])
  const [coachOptions, setCoachOptions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [selectedCoachId, setSelectedCoachId] = useState('')
  const [selectedRole, setSelectedRole] = useState(COACH_TEAM_ROLES[0])
  const [joinedAt, setJoinedAt] = useState(todayIso())
  const [adding, setAdding] = useState(false)

  const [actionId, setActionId] = useState(null)
  const [editingRoleId, setEditingRoleId] = useState(null)
  const [editingRoleValue, setEditingRoleValue] = useState(COACH_TEAM_ROLES[0])
  const [endAssignmentTarget, setEndAssignmentTarget] = useState(null)
  const [removeTarget, setRemoveTarget] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [staffList, options] = await Promise.all([
          getCoachingStaffForTeam(teamId),
          getCoachOptions(),
        ])
        if (cancelled) return
        setStaff(staffList)
        setCoachOptions(options)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load coaching staff.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [teamId])

  async function handleAdd() {
    if (!selectedCoachId) return
    setAdding(true)
    setError(null)
    try {
      const link = await createCoachTeam({
        coachId: Number(selectedCoachId),
        teamId: Number(teamId),
        role: selectedRole,
        status: 'ACTIVE',
        joinedAt,
        leftAt: null,
      })
      const coach = coachOptions.find((o) => o.coachId === Number(selectedCoachId))
      setStaff((prev) => [...prev, { ...link, ...coach }])
      setSelectedCoachId('')
    } catch (err) {
      setError(err.message ?? 'Unable to add coach.')
    } finally {
      setAdding(false)
    }
  }

  async function handleSaveRole(link) {
    setActionId(link.id)
    setError(null)
    try {
      const updated = await updateCoachTeam(link.id, {
        coachId: link.coachId,
        teamId: Number(teamId),
        role: editingRoleValue,
        status: link.status,
        joinedAt: link.joinedAt,
        leftAt: link.leftAt,
      })
      setStaff((prev) => prev.map((s) => (s.id === link.id ? { ...s, ...updated } : s)))
      setEditingRoleId(null)
    } catch (err) {
      setError(err.message ?? 'Unable to update role.')
    } finally {
      setActionId(null)
    }
  }

  async function handleEndAssignment() {
    const link = endAssignmentTarget
    setActionId(link.id)
    setError(null)
    try {
      const updated = await updateCoachTeam(link.id, {
        coachId: link.coachId,
        teamId: Number(teamId),
        role: link.role,
        status: 'INACTIVE',
        joinedAt: link.joinedAt,
        leftAt: todayIso(),
      })
      setStaff((prev) => prev.map((s) => (s.id === link.id ? { ...s, ...updated } : s)))
      setEndAssignmentTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to end assignment.')
    } finally {
      setActionId(null)
    }
  }

  async function handleRemove() {
    const link = removeTarget
    setActionId(link.id)
    setError(null)
    try {
      await deleteCoachTeam(link.id)
      setStaff((prev) => prev.filter((s) => s.id !== link.id))
      setRemoveTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to remove coach.')
    } finally {
      setActionId(null)
    }
  }

  return (
    <div className="card bg-base-100 shadow-md">
      <div className="card-body">
        <h2 className="card-title text-lg">Coaching staff</h2>

        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        {loading ? (
          <div className="flex justify-center py-6">
            <span className="loading loading-spinner text-primary" />
          </div>
        ) : (
          <>
            <div className="mb-4 flex flex-wrap items-end gap-3">
              <label className="form-control">
                <span className="label-text mb-1">Coach</span>
                <select
                  className="select select-bordered select-sm"
                  value={selectedCoachId}
                  onChange={(e) => setSelectedCoachId(e.target.value)}
                  disabled={coachOptions.length === 0}
                >
                  <option value="">
                    {coachOptions.length === 0 ? 'No coach accounts found' : 'Select a coach'}
                  </option>
                  {coachOptions.map((option) => (
                    <option key={option.coachId} value={option.coachId}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="form-control">
                <span className="label-text mb-1">Role</span>
                <select
                  className="select select-bordered select-sm"
                  value={selectedRole}
                  onChange={(e) => setSelectedRole(e.target.value)}
                >
                  {COACH_TEAM_ROLES.map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </select>
              </label>

              <label className="form-control">
                <span className="label-text mb-1">Joined</span>
                <input
                  type="date"
                  className="input input-bordered input-sm"
                  value={joinedAt}
                  onChange={(e) => setJoinedAt(e.target.value)}
                />
              </label>

              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={handleAdd}
                disabled={adding || !selectedCoachId}
              >
                {adding ? <span className="loading loading-spinner loading-xs" /> : 'Add coach'}
              </button>
            </div>

            {staff.length === 0 && (
              <p className="text-base-content/60 text-sm">No coaches assigned yet.</p>
            )}
            {staff.length > 0 && (
              <div className="overflow-x-auto">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Coach</th>
                      <th>Role</th>
                      <th>Status</th>
                      <th className="hidden sm:table-cell">Joined</th>
                      <th className="hidden sm:table-cell">Left</th>
                      <th className="text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {staff.map((link) => (
                      <tr key={link.id}>
                        <td>
                          <Link to={`/users/${link.userId}`} className="link link-primary text-sm">
                            {link.label ?? `Coach #${link.coachId}`}
                          </Link>
                        </td>
                        <td>
                          {editingRoleId === link.id ? (
                            <div className="flex items-center gap-1">
                              <select
                                className="select select-bordered select-xs"
                                value={editingRoleValue}
                                onChange={(e) => setEditingRoleValue(e.target.value)}
                              >
                                {COACH_TEAM_ROLES.map((value) => (
                                  <option key={value} value={value}>
                                    {value}
                                  </option>
                                ))}
                              </select>
                              <button
                                type="button"
                                className="btn btn-ghost btn-xs"
                                onClick={() => setEditingRoleId(null)}
                              >
                                Cancel
                              </button>
                              <button
                                type="button"
                                className="btn btn-ghost btn-xs"
                                onClick={() => handleSaveRole(link)}
                                disabled={actionId === link.id}
                              >
                                Save
                              </button>
                            </div>
                          ) : (
                            <div className="flex items-center gap-1">
                              <span className="badge badge-ghost badge-sm">{link.role}</span>
                              {link.status === 'ACTIVE' && (
                                <button
                                  type="button"
                                  className="btn btn-ghost btn-xs"
                                  onClick={() => {
                                    setEditingRoleId(link.id)
                                    setEditingRoleValue(link.role)
                                  }}
                                >
                                  Edit
                                </button>
                              )}
                            </div>
                          )}
                        </td>
                        <td>
                          <span className={`badge badge-soft ${link.status === 'ACTIVE' ? 'badge-success' : 'badge-neutral'}`}>
                            {link.status}
                          </span>
                        </td>
                        <td className="hidden sm:table-cell">{link.joinedAt}</td>
                        <td className="hidden sm:table-cell">{link.leftAt ?? '—'}</td>
                        <td className="text-right whitespace-nowrap">
                          {link.status === 'ACTIVE' && (
                            <button
                              type="button"
                              className="btn btn-ghost btn-xs"
                              onClick={() => setEndAssignmentTarget(link)}
                              disabled={actionId === link.id}
                            >
                              End assignment
                            </button>
                          )}
                          <button
                            type="button"
                            className="btn btn-ghost btn-xs text-error"
                            onClick={() => setRemoveTarget(link)}
                            disabled={actionId === link.id}
                          >
                            Remove
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </div>

      {endAssignmentTarget && (
        <ConfirmModal
          title="End assignment"
          body={`End ${endAssignmentTarget.label ?? `Coach #${endAssignmentTarget.coachId}`}'s assignment to this team as of today? There's no way to resume it afterward — only start a new assignment.`}
          confirmLabel={actionId === endAssignmentTarget.id ? 'Ending…' : 'End assignment'}
          confirmClass="btn-primary"
          onConfirm={handleEndAssignment}
          onCancel={() => setEndAssignmentTarget(null)}
        />
      )}

      {removeTarget && (
        <ConfirmModal
          title="Remove coach"
          body={`Remove ${removeTarget.label ?? `Coach #${removeTarget.coachId}`} from this team entirely? This cannot be undone.`}
          confirmLabel={actionId === removeTarget.id ? 'Removing…' : 'Remove'}
          onConfirm={handleRemove}
          onCancel={() => setRemoveTarget(null)}
        />
      )}
    </div>
  )
}
