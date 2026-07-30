import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { createFeeRecord, getFeeRecordsByTeam } from '../../lib/api/feeRecords'
import { getFeeItems } from '../../lib/api/feeItems'
import { getPlayers } from '../../lib/api/players'
import { getTeams } from '../../lib/api/teams'
import { formatCurrency } from '../../lib/formatCurrency'
import { getRosterForTeam } from '../../lib/teamRelations'

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

export default function FeeRecordFormPage() {
  const navigate = useNavigate()

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const [result, setResult] = useState(null)

  const [players, setPlayers] = useState([])
  const [teams, setTeams] = useState([])
  const [feeItems, setFeeItems] = useState([])

  const [scope, setScope] = useState('single')
  const [playerId, setPlayerId] = useState('')
  const [teamId, setTeamId] = useState('')
  const [feeItemId, setFeeItemId] = useState('')
  const [dueDate, setDueDate] = useState(todayIso())

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const [playerList, teamList, feeItemList] = await Promise.all([
          getPlayers(),
          getTeams(),
          getFeeItems(),
        ])
        if (cancelled) return
        setPlayers(playerList)
        setTeams(teamList)
        setFeeItems(feeItemList)
        setTeamId((prev) => prev || String(teamList[0]?.id ?? ''))
        setFeeItemId((prev) => prev || String(feeItemList[0]?.id ?? ''))
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load form data.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  async function assignToTeam(team) {
    const [roster, existingRecords] = await Promise.all([
      getRosterForTeam(team.id),
      getFeeRecordsByTeam(team.id),
    ])
    const activePlayers = roster.filter((r) => r.status === 'ACTIVE')
    const alreadyAssigned = new Set(
      existingRecords
        .filter((r) => r.feeItemId === Number(feeItemId) && r.dueDate === dueDate)
        .map((r) => r.playerId),
    )
    const toCreate = activePlayers.filter((p) => !alreadyAssigned.has(p.playerId))
    await Promise.all(
      toCreate.map((p) =>
        createFeeRecord({
          playerId: p.playerId,
          teamId: team.id,
          feeItemId: Number(feeItemId),
          dueDate,
        }),
      ),
    )
    return { created: toCreate.length, skipped: activePlayers.length - toCreate.length }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setResult(null)

    if (scope === 'single' && !playerId) {
      setError('Select a player.')
      return
    }
    if (scope === 'team' && !teamId) {
      setError('Select a team.')
      return
    }

    setSubmitting(true)
    try {
      if (scope === 'single') {
        await createFeeRecord({
          playerId: Number(playerId),
          teamId: Number(teamId),
          feeItemId: Number(feeItemId),
          dueDate,
        })
        navigate('/fee-records')
        return
      }

      const targetTeams = scope === 'team' ? teams.filter((t) => t.id === Number(teamId)) : teams
      let created = 0
      let skipped = 0
      for (const team of targetTeams) {
        const outcome = await assignToTeam(team)
        created += outcome.created
        skipped += outcome.skipped
      }
      setResult({ created, skipped })
    } catch (err) {
      setError(err.message ?? 'Unable to assign fee.')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    )
  }

  if (result) {
    return (
      <div className="mx-auto max-w-lg">
        <h1 className="mb-4 text-2xl font-semibold">New fee record</h1>
        <div className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md">
          <div role="alert" className="alert alert-success text-sm">
            <span>
              Created {result.created} fee record{result.created === 1 ? '' : 's'}.
              {result.skipped > 0 &&
                ` Skipped ${result.skipped} player${result.skipped === 1 ? '' : 's'} who already had this fee for this due date.`}
            </span>
          </div>
          <div className="flex gap-3">
            <Link to="/fee-records" className="btn btn-primary">
              View fee records
            </Link>
            <button type="button" className="btn btn-ghost" onClick={() => setResult(null)}>
              Assign another batch
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-lg">
      <h1 className="mb-4 text-2xl font-semibold">New fee record</h1>

      <form className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        <label className="form-control">
          <span className="label-text mb-1">Assign to</span>
          <select
            className="select select-bordered w-full"
            value={scope}
            onChange={(e) => setScope(e.target.value)}
          >
            <option value="single">One player</option>
            <option value="team">Everyone active on a team</option>
            <option value="all">Everyone active, every team</option>
          </select>
        </label>

        {scope === 'single' && (
          <label className="form-control">
            <span className="label-text mb-1">Player</span>
            <select
              className="select select-bordered w-full"
              value={playerId}
              onChange={(e) => setPlayerId(e.target.value)}
              required
            >
              <option value="">Select a player</option>
              {players.map((player) => (
                <option key={player.id} value={player.id}>
                  {player.fullName}
                </option>
              ))}
            </select>
          </label>
        )}

        {(scope === 'single' || scope === 'team') && (
          <label className="form-control">
            <span className="label-text mb-1">Team</span>
            <select
              className="select select-bordered w-full"
              value={teamId}
              onChange={(e) => setTeamId(e.target.value)}
              required
            >
              {teams.length === 0 && <option value="">No teams found</option>}
              {teams.map((team) => (
                <option key={team.id} value={team.id}>
                  {team.name}
                </option>
              ))}
            </select>
          </label>
        )}

        <label className="form-control">
          <span className="label-text mb-1">Fee item</span>
          <select
            className="select select-bordered w-full"
            value={feeItemId}
            onChange={(e) => setFeeItemId(e.target.value)}
            required
          >
            {feeItems.length === 0 && <option value="">No fee items found</option>}
            {feeItems.map((item) => (
              <option key={item.id} value={item.id}>
                {item.name} ({formatCurrency(item.amount)})
              </option>
            ))}
          </select>
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Due date</span>
          <input
            type="date"
            className="input input-bordered w-full"
            value={dueDate}
            onChange={(e) => setDueDate(e.target.value)}
            required
          />
        </label>

        <div className="mt-2 flex justify-end gap-3">
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/fee-records')}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? <span className="loading loading-spinner loading-sm" /> : 'Save'}
          </button>
        </div>
      </form>
    </div>
  )
}
