import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import { getUser } from '../../lib/api/users'
import { ROLES } from '../../lib/roles'
import { findProfileByUserId } from '../../lib/userProfiles'
import { getAge } from '../../lib/ageUtils'
import { getPlayersForParent } from '../../lib/playerRelations'
import { getCoachTeamHistory } from '../../lib/teamRelations'
import { usePagination } from '../../lib/usePagination'

export default function UserDetailsPage() {
  const { id } = useParams()
  const [user, setUser] = useState(null)
  const [profile, setProfile] = useState(null)
  const [players, setPlayers] = useState([])
  const [teams, setTeams] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const u = await getUser(id)
        if (cancelled) return
        setUser(u)

        const p = await findProfileByUserId(u.role, id)
        if (cancelled) return
        setProfile(p)

        if (u.role === ROLES.PARENT && p) {
          const linkedPlayers = await getPlayersForParent(p.id)
          if (cancelled) return
          setPlayers(linkedPlayers)
        }

        if (u.role === ROLES.COACH && p) {
          const linkedTeams = await getCoachTeamHistory(p.id)
          if (cancelled) return
          setTeams(linkedTeams)
        }
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load user.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [id])

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

  return <UserDetailsBody id={id} user={user} profile={profile} players={players} teams={teams} />
}

function UserDetailsBody({ id, user, profile, players, teams }) {
  const isParent = user.role === ROLES.PARENT
  const isCoach = user.role === ROLES.COACH
  const hasDetailsCard = (isCoach || isParent) && profile
  const hasListCard = isParent || isCoach
  const rosterItems = isParent ? players : teams

  const { pageItems, page, setPage, totalPages } = usePagination(rosterItems, 10)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">{user.fullName}</h1>
        <div className="flex gap-2">
          <Link to="/users" className="btn btn-ghost btn-sm">
            Back
          </Link>
          <Link to={`/users/${id}/edit`} className="btn btn-primary btn-sm">
            Edit
          </Link>
        </div>
      </div>

      <div className={hasDetailsCard ? 'grid grid-cols-1 gap-6 lg:grid-cols-3' : 'lg:max-w-lg'}>
        <div className="card bg-base-100 shadow-md">
          <div className="card-body">
            <h2 className="card-title text-lg">User info</h2>
            <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-3 text-sm">
              <dt className="text-base-content/60">Full name</dt>
              <dd>{user.fullName}</dd>

              <dt className="text-base-content/60">Email</dt>
              <dd>{user.email}</dd>

              <dt className="text-base-content/60">Phone</dt>
              <dd>{user.phoneNumber || '—'}</dd>

              <dt className="text-base-content/60">Role</dt>
              <dd>
                <span className="badge badge-ghost">{user.role}</span>
              </dd>

              <dt className="text-base-content/60">Status</dt>
              <dd>
                <span className={`badge badge-soft ${user.active ? 'badge-success' : 'badge-neutral'}`}>
                  {user.active ? 'Active' : 'Inactive'}
                </span>
                {user.mustChangePassword && (
                  <span className="badge badge-soft badge-warning ml-1">Must change password</span>
                )}
              </dd>
            </dl>
          </div>
        </div>

        {isCoach && profile && (
          <div className="card bg-base-100 shadow-md lg:col-span-2">
            <div className="card-body">
              <h2 className="card-title text-lg">Coach details</h2>
              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-3 text-sm">
                <dt className="text-base-content/60">Specialization</dt>
                <dd>{profile.specialization}</dd>
                <dt className="text-base-content/60">Certification</dt>
                <dd>{profile.certification}</dd>
              </dl>
            </div>
          </div>
        )}

        {isParent && profile && (
          <div className="card bg-base-100 shadow-md lg:col-span-2">
            <div className="card-body">
              <h2 className="card-title text-lg">Parent details</h2>
              <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-3 text-sm">
                <dt className="text-base-content/60">Emergency contact</dt>
                <dd>{profile.emergencyContact || '—'}</dd>
                <dt className="text-base-content/60">Address</dt>
                <dd>{profile.address || '—'}</dd>
              </dl>
            </div>
          </div>
        )}
      </div>

      {hasListCard && (
        <div className="card bg-base-100 shadow-md">
          <div className="card-body">
            <h2 className="card-title text-lg">{isParent ? 'Players' : 'Coaching assignments'}</h2>
            {rosterItems.length === 0 ? (
              <p className="text-base-content/60 text-sm">
                {isParent ? 'No players linked.' : 'No teams linked.'}
              </p>
            ) : isParent ? (
              <div className="overflow-x-auto">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Player</th>
                      <th>Age</th>
                      <th>Gender</th>
                      <th>Relationship</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pageItems.map((player) => (
                      <tr key={player.id}>
                        <td>
                          <Link to={`/players/${player.id}`} className="link link-primary text-sm">
                            {player.fullName}
                          </Link>
                        </td>
                        <td>{getAge(player.dateOfBirth)}</td>
                        <td>{player.gender}</td>
                        <td>
                          <span className="badge badge-ghost badge-sm">{player.relationship}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Team</th>
                      <th className="hidden sm:table-cell">Category</th>
                      <th className="hidden sm:table-cell">Age group</th>
                      <th>Role</th>
                      <th>Status</th>
                      <th className="hidden sm:table-cell">Joined</th>
                      <th className="hidden sm:table-cell">Left</th>
                    </tr>
                  </thead>
                  <tbody>
                    {pageItems.map((team) => (
                      <tr key={team.linkId}>
                        <td>
                          <Link to={`/teams/${team.id}`} className="link link-primary text-sm">
                            {team.name}
                          </Link>
                        </td>
                        <td className="hidden sm:table-cell">
                          <span className="badge badge-ghost badge-sm">{team.category}</span>
                        </td>
                        <td className="hidden sm:table-cell">
                          <span className="badge badge-ghost badge-sm">{team.ageGroup}</span>
                        </td>
                        <td>
                          <span className="badge badge-ghost badge-sm">{team.role}</span>
                        </td>
                        <td>
                          <span className={`badge badge-soft badge-sm ${team.status === 'ACTIVE' ? 'badge-success' : 'badge-neutral'}`}>
                            {team.status}
                          </span>
                        </td>
                        <td className="hidden sm:table-cell">{team.joinedAt}</td>
                        <td className="hidden sm:table-cell">{team.leftAt ?? '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  )
}
