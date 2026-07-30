import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import { getMyParentProfile } from '../../lib/api/profiles'
import { getAge } from '../../lib/ageUtils'
import { getPlayersForParent } from '../../lib/playerRelations'
import { usePagination } from '../../lib/usePagination'

export default function ParentPlayersListPage() {
  const [children, setChildren] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const parent = await getMyParentProfile()
        const kids = await getPlayersForParent(parent.id)
        if (cancelled) return
        setChildren(kids.map((p) => ({ ...p, age: getAge(p.dateOfBirth) })))
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load your children.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  const { pageItems, page, setPage, totalPages } = usePagination(children, 10)

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

  if (children.length === 1) {
    return <Navigate to={`/dashboard/parent/children/${children[0].id}`} replace />
  }

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-2xl font-semibold">Players</h1>

      <div className="card bg-base-100 shadow-md">
        <div className="overflow-x-auto">
          <table className="table table-fixed">
            <thead>
              <tr>
                <th className="w-[34%]">Name</th>
                <th className="hidden sm:table-cell w-[24%]">Date of birth</th>
                <th className="w-[14%]">Age</th>
                <th className="w-[16%]">Gender</th>
                <th className="w-[12%]">Actions</th>
              </tr>
            </thead>
            <tbody>
              {pageItems.map((child) => (
                <tr key={child.id}>
                  <td className="truncate" title={child.fullName}>
                    {child.fullName}
                  </td>
                  <td className="hidden sm:table-cell">{child.dateOfBirth}</td>
                  <td>{child.age}</td>
                  <td>
                    <span className="badge badge-ghost">{child.gender}</span>
                  </td>
                  <td className="whitespace-nowrap">
                    <Link
                      to={`/dashboard/parent/children/${child.id}`}
                      className="btn btn-ghost btn-xs text-info"
                    >
                      Details
                    </Link>
                  </td>
                </tr>
              ))}
              {pageItems.length === 0 && (
                <tr>
                  <td colSpan={5} className="text-base-content/60 py-6 text-center">
                    No children linked to your account yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>
    </div>
  )
}
