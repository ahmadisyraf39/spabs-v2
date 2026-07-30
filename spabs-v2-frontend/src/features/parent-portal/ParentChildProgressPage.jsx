import { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import { getPlayer } from '../../lib/api/players'
import { getTeamHistoryForPlayer } from '../../lib/teamRelations'
import PlayerProgressGrid from '../players/PlayerProgressGrid'

export default function ParentChildProgressPage() {
  const { id } = useParams()
  const [player, setPlayer] = useState(null)
  const [teams, setTeams] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [statusFilter, setStatusFilter] = useState('ACTIVE')

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [p, history] = await Promise.all([getPlayer(id), getTeamHistoryForPlayer(id)])
        if (cancelled) return
        setPlayer(p)
        setTeams(history)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load this child.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [id])

  const filteredTeams = useMemo(
    () => teams.filter((t) => !statusFilter || t.status === statusFilter),
    [teams, statusFilter],
  )

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="mx-auto max-w-lg">
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-2xl font-semibold">{player.fullName}'s skills</h1>
        <select
          className="select select-bordered select-sm"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">All statuses</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
        </select>
      </div>

      {filteredTeams.length === 0 ? (
        <div className="card bg-base-100 shadow-md">
          <div className="card-body">
            <p className="text-base-content/60 text-sm">No teams found.</p>
          </div>
        </div>
      ) : (
        filteredTeams.map((team) => (
          <div key={team.linkId} className="flex flex-col gap-3">
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-medium">{team.name}</h2>
              <span
                className={`badge badge-soft badge-sm ${team.status === 'ACTIVE' ? 'badge-success' : 'badge-neutral'}`}
              >
                {team.status}
              </span>
            </div>
            <PlayerProgressGrid playerId={id} teamId={team.id} editable={false} />
          </div>
        ))
      )}
    </div>
  )
}
