import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { deletePlayer, getPlayers } from '../../lib/api/players'
import { AGE_GROUPS, ageGroupForAge, getAge } from '../../lib/ageUtils'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { deleteAllAttendanceForPlayer } from '../../lib/activityRelations'
import { deleteAllParentLinksForPlayer } from '../../lib/playerRelations'
import { deleteAllTeamLinksForPlayer } from '../../lib/teamRelations'
import { GENDERS } from './playerEnums'

export default function PlayersListPage() {
  const [players, setPlayers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [genderFilter, setGenderFilter] = useState('')
  const [ageGroupFilter, setAgeGroupFilter] = useState('')
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadPlayers() {
    setLoading(true)
    setError(null)
    try {
      setPlayers(await getPlayers())
    } catch (err) {
      setError(err.message ?? 'Unable to load players.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadPlayers()
  }, [])

  const playersWithAge = useMemo(
    () => players.map((p) => ({ ...p, age: getAge(p.dateOfBirth) })),
    [players],
  )

  const filteredPlayers = useMemo(() => {
    const term = search.trim().toLowerCase()
    return playersWithAge.filter((p) => {
      const matchesTerm = !term || p.fullName.toLowerCase().includes(term)
      const matchesGender = !genderFilter || p.gender === genderFilter
      const matchesAgeGroup = !ageGroupFilter || ageGroupForAge(p.age) === ageGroupFilter
      return matchesTerm && matchesGender && matchesAgeGroup
    })
  }, [playersWithAge, search, genderFilter, ageGroupFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredPlayers, 'fullName')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handleDelete() {
    setDeleting(true)
    try {
      await deleteAllParentLinksForPlayer(deleteTarget.id)
      await deleteAllTeamLinksForPlayer(deleteTarget.id)
      await deleteAllAttendanceForPlayer(deleteTarget.id)
      await deletePlayer(deleteTarget.id)
      setPlayers((prev) => prev.filter((p) => p.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete player.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Players</h1>
        <Link to="/players/new" className="btn btn-primary btn-sm">
          New player
        </Link>
      </div>

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center w-full">
        <input
          type="text"
          placeholder="Search name"
          className="input input-bordered input-sm w-full sm:min-w-[200px] sm:flex-1"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value)
            resetToFirstPage()
          }}
        />
        <div className="flex gap-3 w-full sm:contents">
          <select
            className="select select-bordered select-sm flex-1 sm:flex-initial sm:ml-auto"
            value={genderFilter}
            onChange={(e) => {
              setGenderFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All genders</option>
            {GENDERS.map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
          <select
            className="select select-bordered select-sm flex-1 sm:flex-initial"
            value={ageGroupFilter}
            onChange={(e) => {
              setAgeGroupFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All age groups</option>
            {AGE_GROUPS.map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="overflow-x-auto">
          {loading ? (
            <div className="flex justify-center py-10">
              <span className="loading loading-spinner loading-lg text-primary" />
            </div>
          ) : (
            <table className="table table-fixed">
              <thead>
                <tr>
                  <SortableTh
                    label="Name"
                    sortKey="fullName"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[28%]"
                  />
                  <SortableTh
                    label="Date of birth"
                    sortKey="dateOfBirth"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="hidden sm:table-cell w-[22%]"
                  />
                  <SortableTh
                    label="Age"
                    sortKey="age"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[12%]"
                  />
                  <SortableTh
                    label="Gender"
                    sortKey="gender"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[18%]"
                  />
                  <th className="w-[20%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((player) => (
                  <tr key={player.id}>
                    <td className="truncate" title={player.fullName}>
                      {player.fullName}
                    </td>
                    <td className="hidden sm:table-cell">{player.dateOfBirth}</td>
                    <td>{player.age}</td>
                    <td>
                      <span className="badge badge-ghost">{player.gender}</span>
                    </td>
                    <td className="whitespace-nowrap">
                      <Link to={`/players/${player.id}`} className="btn btn-ghost btn-xs text-info">
                        Details
                      </Link>
                      <Link to={`/players/${player.id}/edit`} className="btn btn-ghost btn-xs">
                        Edit
                      </Link>
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => setDeleteTarget(player)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-base-content/60 py-6 text-center">
                      No players found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>

      {deleteTarget && (
        <ConfirmModal
          title="Delete player"
          body={`Permanently delete ${deleteTarget.fullName}? This also removes their guardian, team, and attendance records. This cannot be undone.`}
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
