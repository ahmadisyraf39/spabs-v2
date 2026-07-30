import { useEffect, useMemo, useState } from 'react'
import Pagination from '../../components/ui/Pagination'
import { getInventories } from '../../lib/api/inventories'
import { usePagination } from '../../lib/usePagination'
import { INVENTORY_CATEGORIES } from '../inventory/inventoryEnums'

export default function CoachInventoryPage() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const list = await getInventories()
        if (cancelled) return
        setItems(list)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load inventory.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  const filteredItems = useMemo(() => {
    const term = search.trim().toLowerCase()
    return items
      .filter((item) => {
        const matchesTerm = !term || item.name.toLowerCase().includes(term)
        const matchesCategory = !categoryFilter || item.category === categoryFilter
        return matchesTerm && matchesCategory
      })
      .sort((a, b) => a.name.localeCompare(b.name))
  }, [items, search, categoryFilter])

  const { pageItems, page, setPage, totalPages } = usePagination(filteredItems, 10)

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-2xl font-semibold">Inventory</h1>

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
            setPage(1)
          }}
        />
        <div className="flex gap-3 w-full sm:contents">
          <select
            className="select select-bordered select-sm flex-1 sm:flex-initial sm:ml-auto"
            value={categoryFilter}
            onChange={(e) => {
              setCategoryFilter(e.target.value)
              setPage(1)
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
          <table className="table table-fixed">
            <thead>
              <tr>
                <th className="w-[46%]">Name</th>
                <th className="w-[28%]">Category</th>
                <th className="w-[26%]">Current quantity</th>
              </tr>
            </thead>
            <tbody>
              {pageItems.map((item) => (
                <tr key={item.id}>
                  <td className="truncate">{item.name}</td>
                  <td>
                    <span className="badge badge-ghost badge-sm">{item.category}</span>
                  </td>
                  <td>{item.currentQuantity}</td>
                </tr>
              ))}
              {pageItems.length === 0 && (
                <tr>
                  <td colSpan={3} className="text-base-content/60 py-6 text-center">
                    No inventory items found.
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
