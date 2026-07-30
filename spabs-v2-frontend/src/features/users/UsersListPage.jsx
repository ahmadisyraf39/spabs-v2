import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { deleteUser, getUsers, updateUser } from '../../lib/api/users'
import { ROLES } from '../../lib/roles'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { deleteAllCoachPaymentsForCoach, deleteAllTeamLinksForCoachUser } from '../../lib/teamRelations'
import { deleteProfileForUser } from '../../lib/userProfiles'
import ResetPasswordModal from './ResetPasswordModal'

const ROLE_OPTIONS = [ROLES.SUPER_ADMIN, ROLES.ADMIN, ROLES.COACH, ROLES.PARENT]

export default function UsersListPage() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [roleFilter, setRoleFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [resetTarget, setResetTarget] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadUsers() {
    setLoading(true)
    setError(null)
    try {
      setUsers(await getUsers())
    } catch (err) {
      setError(err.message ?? 'Unable to load users.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadUsers()
  }, [])

  const filteredUsers = useMemo(() => {
    const term = search.trim().toLowerCase()
    return users.filter((u) => {
      const matchesTerm =
        !term || u.fullName.toLowerCase().includes(term) || u.email.toLowerCase().includes(term)
      const matchesRole = !roleFilter || u.role === roleFilter
      const matchesStatus =
        !statusFilter || (statusFilter === 'ACTIVE' ? u.active : !u.active)
      return matchesTerm && matchesRole && matchesStatus
    })
  }, [users, search, roleFilter, statusFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredUsers, 'fullName')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handleToggleActive(user) {
    const updated = await updateUser(user.id, {
      email: user.email,
      fullName: user.fullName,
      phoneNumber: user.phoneNumber,
      role: user.role,
      active: !user.active,
    })
    setUsers((prev) => prev.map((u) => (u.id === updated.id ? updated : u)))
  }

  async function handleDelete() {
    setDeleting(true)
    try {
      if (deleteTarget.role === ROLES.COACH) {
        await deleteAllTeamLinksForCoachUser(deleteTarget.id)
        await deleteAllCoachPaymentsForCoach(deleteTarget.id)
      }
      await deleteProfileForUser(deleteTarget.role, deleteTarget.id)
      await deleteUser(deleteTarget.id)
      setUsers((prev) => prev.filter((u) => u.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete user.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Users</h1>
        <Link to="/users/new" className="btn btn-primary btn-sm">
          New user
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
          placeholder="Search name or email"
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
            value={roleFilter}
            onChange={(e) => {
              setRoleFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All roles</option>
            {ROLE_OPTIONS.map((role) => (
              <option key={role} value={role}>
                {role}
              </option>
            ))}
          </select>
          <select
            className="select select-bordered select-sm flex-1 sm:flex-initial"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
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
                    className="w-[20%]"
                  />
                  <SortableTh
                    label="Email"
                    sortKey="email"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="hidden sm:table-cell w-[28%]"
                  />
                  <SortableTh
                    label="Role"
                    sortKey="role"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[10%]"
                  />
                  <SortableTh
                    label="Status"
                    sortKey="active"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[14%]"
                  />
                  <th className="w-[28%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((user) => (
                  <tr key={user.id}>
                    <td className="truncate" title={user.fullName}>
                      {user.fullName}
                    </td>
                    <td className="hidden sm:table-cell truncate" title={user.email}>
                      {user.email}
                    </td>
                    <td>
                      <span className="badge badge-ghost">{user.role}</span>
                    </td>
                    <td>
                      <span className={`badge badge-soft ${user.active ? 'badge-success' : 'badge-neutral'}`}>
                        {user.active ? 'Active' : 'Inactive'}
                      </span>
                      {user.mustChangePassword && (
                        <span className="badge badge-soft badge-warning ml-1">Must change password</span>
                      )}
                    </td>
                    <td className="whitespace-nowrap">
                      <Link to={`/users/${user.id}`} className="btn btn-ghost btn-xs text-info">
                        Details
                      </Link>
                      <Link to={`/users/${user.id}/edit`} className="btn btn-ghost btn-xs">
                        Edit
                      </Link>
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs"
                        onClick={() => setResetTarget(user)}
                      >
                        Reset password
                      </button>
                      <button type="button" className="btn btn-ghost btn-xs" onClick={() => handleToggleActive(user)}>
                        {user.active ? 'Deactivate' : 'Activate'}
                      </button>
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => setDeleteTarget(user)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-base-content/60 text-center py-6">
                      No users found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>

      {resetTarget && <ResetPasswordModal user={resetTarget} onClose={() => setResetTarget(null)} />}

      {deleteTarget && (
        <ConfirmModal
          title="Delete user"
          body={`Permanently delete ${deleteTarget.fullName}? This cannot be undone.`}
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
