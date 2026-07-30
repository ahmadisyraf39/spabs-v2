import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { deleteInventory, getInventories } from '../../lib/api/inventories'
import { getInventoryTransactionsByInventory } from '../../lib/api/inventoryTransactions'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { INVENTORY_CATEGORIES } from './inventoryEnums'

export default function InventoryListPage() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleteBlockedCount, setDeleteBlockedCount] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadItems() {
    setLoading(true)
    setError(null)
    try {
      setItems(await getInventories())
    } catch (err) {
      setError(err.message ?? 'Unable to load inventory.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadItems()
  }, [])

  const filteredItems = useMemo(() => {
    const term = search.trim().toLowerCase()
    return items.filter((item) => {
      const matchesTerm = !term || item.name.toLowerCase().includes(term)
      const matchesCategory = !categoryFilter || item.category === categoryFilter
      return matchesTerm && matchesCategory
    })
  }, [items, search, categoryFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredItems, 'name')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handleDeleteClick(item) {
    setDeleteTarget(item)
    setDeleteBlockedCount(null)
    try {
      const transactions = await getInventoryTransactionsByInventory(item.id)
      setDeleteBlockedCount(transactions.length)
    } catch (err) {
      setError(err.message ?? 'Unable to check this item for transactions.')
      setDeleteTarget(null)
    }
  }

  async function handleConfirmDelete() {
    setDeleting(true)
    try {
      await deleteInventory(deleteTarget.id)
      setItems((prev) => prev.filter((item) => item.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete inventory item.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Inventory</h1>
        <Link to="/inventory/new" className="btn btn-primary btn-sm">
          New item
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
            value={categoryFilter}
            onChange={(e) => {
              setCategoryFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All categories</option>
            {INVENTORY_CATEGORIES.map((value) => (
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
                    label="Category"
                    sortKey="category"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="hidden sm:table-cell w-[18%]"
                  />
                  <SortableTh
                    label="Current quantity"
                    sortKey="currentQuantity"
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
                      <span className="badge badge-ghost badge-sm">{item.category}</span>
                    </td>
                    <td>{item.currentQuantity}</td>
                    <td className="whitespace-nowrap">
                      <Link to={`/inventory/${item.id}`} className="btn btn-ghost btn-xs text-info">
                        Details
                      </Link>
                      <Link to={`/inventory/${item.id}/edit`} className="btn btn-ghost btn-xs">
                        Edit
                      </Link>
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => handleDeleteClick(item)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={4} className="text-base-content/60 py-6 text-center">
                      No inventory items found.
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
            <h3 className="text-lg font-semibold">Cannot delete item</h3>
            <p className="text-base-content/70 mt-2 text-sm">
              "{deleteTarget.name}" still has {deleteBlockedCount} transaction
              {deleteBlockedCount === 1 ? '' : 's'} recorded. Delete{' '}
              {deleteBlockedCount === 1 ? 'it' : 'them'} first from the item's Details page.
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
          title="Delete inventory item"
          body={`Permanently delete "${deleteTarget.name}"? This cannot be undone.`}
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleConfirmDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
