import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { getClubSponsorshipsBySponsor } from '../../lib/api/clubSponsorships'
import { deleteSponsor, getSponsors } from '../../lib/api/sponsors'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'

export default function SponsorsListPage() {
  const [sponsors, setSponsors] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleteBlockedCount, setDeleteBlockedCount] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadSponsors() {
    setLoading(true)
    setError(null)
    try {
      setSponsors(await getSponsors())
    } catch (err) {
      setError(err.message ?? 'Unable to load sponsors.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadSponsors()
  }, [])

  const filteredSponsors = useMemo(() => {
    const term = search.trim().toLowerCase()
    if (!term) return sponsors
    return sponsors.filter((sponsor) => sponsor.name.toLowerCase().includes(term))
  }, [sponsors, search])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredSponsors, 'name')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handleDeleteClick(sponsor) {
    setDeleteTarget(sponsor)
    setDeleteBlockedCount(null)
    try {
      const sponsorships = await getClubSponsorshipsBySponsor(sponsor.id)
      setDeleteBlockedCount(sponsorships.length)
    } catch (err) {
      setError(err.message ?? 'Unable to check this sponsor for sponsorships.')
      setDeleteTarget(null)
    }
  }

  async function handleConfirmDelete() {
    setDeleting(true)
    try {
      await deleteSponsor(deleteTarget.id)
      setSponsors((prev) => prev.filter((sponsor) => sponsor.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete sponsor.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Sponsors</h1>
        <Link to="/sponsors/new" className="btn btn-primary btn-sm">
          New sponsor
        </Link>
      </div>

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}

      <div className="flex flex-wrap gap-3">
        <input
          type="text"
          placeholder="Search name"
          className="input input-bordered input-sm w-full sm:w-64"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value)
            resetToFirstPage()
          }}
        />
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
                    sortKey="name"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[26%]"
                  />
                  <SortableTh
                    label="Contact person"
                    sortKey="contactPerson"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[20%]"
                  />
                  <th className="hidden sm:table-cell w-[18%]">Phone</th>
                  <th className="w-[18%]">Email</th>
                  <th className="w-[18%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((sponsor) => (
                  <tr key={sponsor.id}>
                    <td className="truncate" title={sponsor.name}>
                      {sponsor.name}
                    </td>
                    <td className="truncate">{sponsor.contactPerson || '—'}</td>
                    <td className="hidden sm:table-cell truncate">{sponsor.phoneNumber || '—'}</td>
                    <td className="truncate">{sponsor.email || '—'}</td>
                    <td className="whitespace-nowrap">
                      <Link to={`/sponsors/${sponsor.id}/edit`} className="btn btn-ghost btn-xs">
                        Edit
                      </Link>
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => handleDeleteClick(sponsor)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-base-content/60 py-6 text-center">
                      No sponsors found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>

      {deleteTarget && deleteBlockedCount === null && (
        <div className="modal modal-open">
          <div className="modal-box flex justify-center">
            <span className="loading loading-spinner text-primary" />
          </div>
        </div>
      )}

      {deleteTarget && deleteBlockedCount !== null && deleteBlockedCount > 0 && (
        <div className="modal modal-open">
          <div className="modal-box">
            <h3 className="text-lg font-semibold">Cannot delete sponsor</h3>
            <p className="text-base-content/70 mt-2 text-sm">
              "{deleteTarget.name}" still has {deleteBlockedCount} sponsorship
              {deleteBlockedCount === 1 ? '' : 's'} recorded. Delete{' '}
              {deleteBlockedCount === 1 ? 'it' : 'them'} first from the Sponsorship page.
            </p>
            <div className="modal-action">
              <button type="button" className="btn" onClick={() => setDeleteTarget(null)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {deleteTarget && deleteBlockedCount === 0 && (
        <ConfirmModal
          title="Delete sponsor"
          body={`Permanently delete "${deleteTarget.name}"? This cannot be undone.`}
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleConfirmDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
