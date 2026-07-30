import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { deleteClubSponsorship, getClubSponsorships } from '../../lib/api/clubSponsorships'
import { getSponsors } from '../../lib/api/sponsors'
import { formatCurrency } from '../../lib/formatCurrency'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import {
  SPONSORSHIP_TYPES,
  sponsorshipStatus,
  sponsorshipStatusBadgeClass,
  sponsorshipTypeBadgeClass,
} from './sponsorshipEnums'

export default function ClubSponsorshipsListPage() {
  const [sponsors, setSponsors] = useState([])
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [sponsorFilter, setSponsorFilter] = useState('')
  const [typeFilter, setTypeFilter] = useState('')
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadRecords() {
    setLoading(true)
    setError(null)
    try {
      const [sponsorList, sponsorshipList] = await Promise.all([getSponsors(), getClubSponsorships()])
      setSponsors(sponsorList)
      setRecords(sponsorshipList)
    } catch (err) {
      setError(err.message ?? 'Unable to load sponsorships.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRecords()
  }, [])

  const filteredRecords = useMemo(() => {
    const term = search.trim().toLowerCase()
    return records.filter((record) => {
      const matchesTerm =
        !term ||
        record.sponsorName.toLowerCase().includes(term) ||
        (record.description ?? '').toLowerCase().includes(term)
      const matchesSponsor = !sponsorFilter || record.sponsorId === Number(sponsorFilter)
      const matchesType = !typeFilter || record.sponsorshipType === typeFilter
      return matchesTerm && matchesSponsor && matchesType
    })
  }, [records, search, sponsorFilter, typeFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredRecords, 'startDate', 'desc')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handleDelete() {
    setDeleting(true)
    try {
      await deleteClubSponsorship(deleteTarget.id)
      setRecords((prev) => prev.filter((r) => r.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete sponsorship.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Sponsorship</h1>
        <div className="flex gap-2">
          <Link to="/sponsors" className="btn btn-outline btn-sm">
            Manage sponsors
          </Link>
          <Link to="/sponsorships/new" className="btn btn-primary btn-sm">
            New sponsorship
          </Link>
        </div>
      </div>

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center w-full">
        <input
          type="text"
          placeholder="Search sponsor or description"
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
            value={sponsorFilter}
            onChange={(e) => {
              setSponsorFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All sponsors</option>
            {sponsors.map((sponsor) => (
              <option key={sponsor.id} value={sponsor.id}>
                {sponsor.name}
              </option>
            ))}
          </select>
          <select
            className="select select-bordered select-sm flex-1 sm:flex-initial"
            value={typeFilter}
            onChange={(e) => {
              setTypeFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All types</option>
            {SPONSORSHIP_TYPES.map((value) => (
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
                    label="Sponsor"
                    sortKey="sponsorName"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[18%]"
                  />
                  <th className="hidden sm:table-cell w-[12%]">Type</th>
                  <SortableTh
                    label="Amount"
                    sortKey="amount"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[12%]"
                  />
                  <SortableTh
                    label="Start"
                    sortKey="startDate"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[12%]"
                  />
                  <SortableTh
                    label="End"
                    sortKey="endDate"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="hidden sm:table-cell w-[12%]"
                  />
                  <th className="w-[10%]">Status</th>
                  <th className="w-[24%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((record) => {
                  const status = sponsorshipStatus(record)
                  return (
                    <tr key={record.id}>
                      <td className="truncate" title={record.sponsorName}>
                        {record.sponsorName}
                      </td>
                      <td className="hidden sm:table-cell">
                        <span className={`badge badge-sm ${sponsorshipTypeBadgeClass(record.sponsorshipType)}`}>
                          {record.sponsorshipType}
                        </span>
                      </td>
                      <td>{formatCurrency(record.amount)}</td>
                      <td>{record.startDate}</td>
                      <td className="hidden sm:table-cell">{record.endDate}</td>
                      <td>
                        <span className={`badge badge-sm ${sponsorshipStatusBadgeClass(status)}`}>{status}</span>
                      </td>
                      <td className="whitespace-nowrap">
                        <Link to={`/sponsorships/${record.id}/edit`} className="btn btn-ghost btn-xs">
                          Edit
                        </Link>
                        <button
                          type="button"
                          className="btn btn-ghost btn-xs text-error"
                          onClick={() => setDeleteTarget(record)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  )
                })}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={7} className="text-base-content/60 py-6 text-center">
                      No sponsorships found.
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
          title="Delete sponsorship"
          body={`Permanently delete this ${deleteTarget.sponsorshipType.toLowerCase()} sponsorship from ${deleteTarget.sponsorName}? The income transaction already logged for it has no link back and will be left orphaned — this cannot be undone.`}
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
