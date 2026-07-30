import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getPlayer } from '../../lib/api/players'
import { getTeam } from '../../lib/api/teams'
import PlayerProgressGrid from '../players/PlayerProgressGrid'

export default function CoachPlayerProgressPage() {
  const { teamId, playerId } = useParams()
  const [player, setPlayer] = useState(null)
  const [team, setTeam] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [p, t] = await Promise.all([getPlayer(playerId), getTeam(teamId)])
        if (cancelled) return
        setPlayer(p)
        setTeam(t)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load player/team.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [playerId, teamId])

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
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">{player.fullName}'s skills</h1>
          <p className="text-base-content/60 text-sm">{team.name}</p>
        </div>
        <Link to="/dashboard/coach" className="btn btn-ghost btn-sm">
          Back to dashboard
        </Link>
      </div>

      <PlayerProgressGrid playerId={playerId} teamId={teamId} />
    </div>
  )
}
