import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import { getAttendanceSummary } from '../../lib/api/attendances'
import { createPlayerTeam, deletePlayerTeam, updatePlayerTeam } from '../../lib/api/playerTeams'
import { getPlayerTeamProgress } from '../../lib/api/playerProgress'
import { attendanceProgressColor } from '../../lib/attendanceUtils'
import { moduleProgressColor } from '../progress/progressEnums'
import { getPlayerOptions, getRosterForTeam } from '../../lib/teamRelations'

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

export default function RosterSection({ teamId }) {
  const [roster, setRoster] = useState([])
  const [playerOptions, setPlayerOptions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [selectedPlayerId, setSelectedPlayerId] = useState('')
  const [jerseyNumber, setJerseyNumber] = useState('')
  const [joinedAt, setJoinedAt] = useState(todayIso())
  const [adding, setAdding] = useState(false)

  const [actionId, setActionId] = useState(null)
  const [editingJerseyId, setEditingJerseyId] = useState(null)
  const [editingJerseyValue, setEditingJerseyValue] = useState('')
  const [endStintTarget, setEndStintTarget] = useState(null)
  const [removeTarget, setRemoveTarget] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [rosterList, options] = await Promise.all([
          getRosterForTeam(teamId),
          getPlayerOptions(),
        ])
        if (cancelled) return
        setPlayerOptions(options)

        const activeLinks = rosterList.filter((r) => r.status === 'ACTIVE')
        const [summaries, progressSummaries] = await Promise.all([
          Promise.all(activeLinks.map((link) => getAttendanceSummary(link.playerId, teamId))),
          Promise.all(activeLinks.map((link) => getPlayerTeamProgress(link.playerId, teamId))),
        ])
        if (cancelled) return
        const summaryByPlayerId = new Map(activeLinks.map((link, i) => [link.playerId, summaries[i]]))
        const progressByPlayerId = new Map(
          activeLinks.map((link, i) => [link.playerId, progressSummaries[i]]),
        )
        setRoster(
          rosterList.map((link) => {
            const summary = summaryByPlayerId.get(link.playerId)
            const progress = progressByPlayerId.get(link.playerId)
            return {
              ...link,
              ...(summary && {
                attendancePercentage: summary.attendancePercentage,
                attendanceRecorded: summary.totalRecords > 0,
              }),
              ...(progress && {
                skillPercentage: progress.overallPercentage,
                skillRecorded: progress.totalModules > 0,
              }),
            }
          }),
        )
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load roster.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [teamId])

  const activePlayerIds = new Set(roster.filter((r) => r.status === 'ACTIVE').map((r) => r.playerId))
  const availableOptions = playerOptions.filter((o) => !activePlayerIds.has(o.playerId))

  async function handleAdd() {
    if (!selectedPlayerId) return
    setAdding(true)
    setError(null)
    try {
      const link = await createPlayerTeam({
        playerId: Number(selectedPlayerId),
        teamId: Number(teamId),
        jerseyNumber: jerseyNumber === '' ? null : Number(jerseyNumber),
        status: 'ACTIVE',
        joinedAt,
        leftAt: null,
      })
      const player = playerOptions.find((o) => o.playerId === Number(selectedPlayerId))
      setRoster((prev) => [...prev, { ...link, fullName: player?.label }])
      setSelectedPlayerId('')
      setJerseyNumber('')
    } catch (err) {
      setError(err.message ?? 'Unable to add player.')
    } finally {
      setAdding(false)
    }
  }

  async function handleSaveJersey(link) {
    setActionId(link.id)
    setError(null)
    try {
      const updated = await updatePlayerTeam(link.id, {
        playerId: link.playerId,
        teamId: Number(teamId),
        jerseyNumber: editingJerseyValue === '' ? null : Number(editingJerseyValue),
        status: link.status,
        joinedAt: link.joinedAt,
        leftAt: link.leftAt,
      })
      setRoster((prev) => prev.map((r) => (r.id === link.id ? { ...r, ...updated } : r)))
      setEditingJerseyId(null)
    } catch (err) {
      setError(err.message ?? 'Unable to update jersey number.')
    } finally {
      setActionId(null)
    }
  }

  async function handleEndStint() {
    const link = endStintTarget
    setActionId(link.id)
    setError(null)
    try {
      const updated = await updatePlayerTeam(link.id, {
        playerId: link.playerId,
        teamId: Number(teamId),
        jerseyNumber: link.jerseyNumber,
        status: 'INACTIVE',
        joinedAt: link.joinedAt,
        leftAt: todayIso(),
      })
      setRoster((prev) => prev.map((r) => (r.id === link.id ? { ...r, ...updated } : r)))
      setEndStintTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to end stint.')
    } finally {
      setActionId(null)
    }
  }

  async function handleRemove() {
    const link = removeTarget
    setActionId(link.id)
    setError(null)
    try {
      await deletePlayerTeam(link.id)
      setRoster((prev) => prev.filter((r) => r.id !== link.id))
      setRemoveTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to remove roster entry.')
    } finally {
      setActionId(null)
    }
  }

  return (
    <div className="card bg-base-100 shadow-md">
      <div className="card-body">
        <h2 className="card-title text-lg">Roster</h2>

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
                <span className="label-text mb-1">Player</span>
                <select
                  className="select select-bordered select-sm"
                  value={selectedPlayerId}
                  onChange={(e) => setSelectedPlayerId(e.target.value)}
                  disabled={availableOptions.length === 0}
                >
                  <option value="">
                    {availableOptions.length === 0 ? 'No available players' : 'Select a player'}
                  </option>
                  {availableOptions.map((option) => (
                    <option key={option.playerId} value={option.playerId}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="form-control">
                <span className="label-text mb-1">Jersey #</span>
                <input
                  type="number"
                  className="input input-bordered input-sm w-24"
                  value={jerseyNumber}
                  onChange={(e) => setJerseyNumber(e.target.value)}
                />
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
                disabled={adding || !selectedPlayerId}
              >
                {adding ? <span className="loading loading-spinner loading-xs" /> : 'Add player'}
              </button>
            </div>

            {roster.length === 0 && (
              <p className="text-base-content/60 text-sm">No players on this roster yet.</p>
            )}
            {roster.length > 0 && (
              <div className="overflow-x-auto">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Player</th>
                      <th>Jersey #</th>
                      <th>Status</th>
                      <th className="hidden sm:table-cell">Joined</th>
                      <th className="hidden sm:table-cell">Left</th>
                      <th>Attendance</th>
                      <th>Skills</th>
                      <th className="text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {roster.map((link) => (
                      <tr key={link.id}>
                        <td>
                          <Link to={`/players/${link.playerId}`} className="link link-primary text-sm">
                            {link.fullName}
                          </Link>
                        </td>
                        <td>
                          {editingJerseyId === link.id ? (
                            <div className="flex items-center gap-1">
                              <input
                                type="number"
                                className="input input-bordered input-xs w-16"
                                value={editingJerseyValue}
                                onChange={(e) => setEditingJerseyValue(e.target.value)}
                              />
                              <button
                                type="button"
                                className="btn btn-ghost btn-xs"
                                onClick={() => setEditingJerseyId(null)}
                              >
                                Cancel
                              </button>
                              <button
                                type="button"
                                className="btn btn-ghost btn-xs"
                                onClick={() => handleSaveJersey(link)}
                                disabled={actionId === link.id}
                              >
                                Save
                              </button>
                            </div>
                          ) : (
                            <div className="flex items-center gap-1">
                              <span>{link.jerseyNumber ?? '—'}</span>
                              {link.status === 'ACTIVE' && (
                                <button
                                  type="button"
                                  className="btn btn-ghost btn-xs"
                                  onClick={() => {
                                    setEditingJerseyId(link.id)
                                    setEditingJerseyValue(link.jerseyNumber ?? '')
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
                        <td>
                          {link.attendanceRecorded ? (
                            <div className="flex items-center gap-3">
                              <div>
                                <progress
                                  className={`progress ${attendanceProgressColor(link.attendancePercentage)} w-24`}
                                  value={link.attendancePercentage}
                                  max="100"
                                />
                                <div className="text-xs whitespace-nowrap">{link.attendancePercentage}%</div>
                              </div>
                              <Link
                                to={`/players/${link.playerId}/attendance?teamId=${teamId}`}
                                className="btn btn-ghost btn-xs"
                              >
                                View
                              </Link>
                            </div>
                          ) : (
                            <span className="text-base-content/60 text-xs">No data</span>
                          )}
                        </td>
                        <td>
                          {link.skillRecorded ? (
                            <div className="flex items-center gap-3">
                              <div>
                                <progress
                                  className={`progress ${moduleProgressColor(link.skillPercentage)} w-24`}
                                  value={link.skillPercentage}
                                  max="100"
                                />
                                <div className="text-xs whitespace-nowrap">{link.skillPercentage}%</div>
                              </div>
                              <Link
                                to={`/players/${link.playerId}/progress?teamId=${teamId}`}
                                className="btn btn-ghost btn-xs"
                              >
                                View
                              </Link>
                            </div>
                          ) : (
                            <span className="text-base-content/60 text-xs">No data</span>
                          )}
                        </td>
                        <td className="text-right whitespace-nowrap">
                          {link.status === 'ACTIVE' && (
                            <button
                              type="button"
                              className="btn btn-ghost btn-xs"
                              onClick={() => setEndStintTarget(link)}
                              disabled={actionId === link.id}
                            >
                              End stint
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

      {endStintTarget && (
        <ConfirmModal
          title="End stint"
          body={`End ${endStintTarget.fullName}'s stint on this team as of today? There's no way to resume it afterward — only start a new stint.`}
          confirmLabel={actionId === endStintTarget.id ? 'Ending…' : 'End stint'}
          confirmClass="btn-primary"
          onConfirm={handleEndStint}
          onCancel={() => setEndStintTarget(null)}
        />
      )}

      {removeTarget && (
        <ConfirmModal
          title="Remove from roster"
          body={`Remove ${removeTarget.fullName} from this team's roster entirely? This cannot be undone.`}
          confirmLabel={actionId === removeTarget.id ? 'Removing…' : 'Remove'}
          onConfirm={handleRemove}
          onCancel={() => setRemoveTarget(null)}
        />
      )}
    </div>
  )
}
