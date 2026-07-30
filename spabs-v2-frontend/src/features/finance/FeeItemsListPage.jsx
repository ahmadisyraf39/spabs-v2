import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { deleteFeeItem, getFeeItems } from '../../lib/api/feeItems'
import { formatCurrency } from '../../lib/formatCurrency'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { FEE_TYPES } from './feeEnums'

export default function FeeItemsListPage() {
  const [feeItems, setFeeItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [typeFilter, setTypeFilter] = useState('')
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadFeeItems() {
    setLoading(true)
    setError(null)
    try {
      setFeeItems(await getFeeItems())
    } catch (err) {
      setError(err.message ?? 'Unable to load fee items.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadFeeItems()
  }, [])

  const filteredFeeItems = useMemo(() => {
    const term = search.trim().toLowerCase()
    return feeItems.filter((item) => {
      const matchesTerm = !term || item.name.toLowerCase().includes(term)
      const matchesType = !typeFilter || item.feeType === typeFilter
      return matchesTerm && matchesType
    })
  }, [feeItems, search, typeFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredFeeItems, 'name')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handleDelete() {
    setDeleting(true)
    try {
      await deleteFeeItem(deleteTarget.id)
      setFeeItems((prev) => prev.filter((item) => item.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete fee item.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Fee items</h1>
        <Link to="/fee-items/new" className="btn btn-primary btn-sm">
          New fee item
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
            value={typeFilter}
            onChange={(e) => {
              setTypeFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All types</option>
            {FEE_TYPES.map((value) => (
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
                    sortKey="name"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[36%]"
                  />
                  <SortableTh
                    label="Type"
                    sortKey="feeType"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="hidden sm:table-cell w-[18%]"
                  />
                  <SortableTh
                    label="Amount"
                    sortKey="amount"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[18%]"
                  />
                  <th className="w-[28%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((item) => (
                  <tr key={item.id}>
                    <td className="truncate" title={item.name}>
                      {item.name}
                    </td>
                    <td className="hidden sm:table-cell">
                      <span className="badge badge-ghost badge-sm">{item.feeType}</span>
                    </td>
                    <td>{formatCurrency(item.amount)}</td>
                    <td className="whitespace-nowrap">
                      <Link to={`/fee-items/${item.id}/edit`} className="btn btn-ghost btn-xs">
                        Edit
                      </Link>
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => setDeleteTarget(item)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={4} className="text-base-content/60 py-6 text-center">
                      No fee items found.
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
          title="Delete fee item"
          body={`Permanently delete "${deleteTarget.name}"? This will fail if any fee record still references it — there's no way to check that ahead of time. This cannot be undone.`}
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
