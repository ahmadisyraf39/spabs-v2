import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getInventory } from '../../lib/api/inventories'
import InventoryTransactionsSection from './InventoryTransactionsSection'

export default function InventoryDetailsPage() {
  const { id } = useParams()
  const [item, setItem] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const i = await getInventory(id)
        if (cancelled) return
        setItem(i)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load inventory item.')
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

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">{item.name}</h1>
        <div className="flex gap-2">
          <Link to="/inventory" className="btn btn-ghost btn-sm">
            Back
          </Link>
          <Link to={`/inventory/${id}/edit`} className="btn btn-primary btn-sm">
            Edit
          </Link>
        </div>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="card-body">
          <h2 className="card-title text-lg">Item info</h2>
          <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-3 text-sm">
            <dt className="text-base-content/60">Name</dt>
            <dd>{item.name}</dd>

            <dt className="text-base-content/60">Category</dt>
            <dd>
              <span className="badge badge-ghost">{item.category}</span>
            </dd>

            <dt className="text-base-content/60">Description</dt>
            <dd>{item.description || '—'}</dd>

            <dt className="text-base-content/60">Current quantity</dt>
            <dd className="text-lg font-semibold">{item.currentQuantity}</dd>
          </dl>
        </div>
      </div>

      <InventoryTransactionsSection
        inventoryId={id}
        onQuantityChange={(newQuantity) =>
          setItem((prev) => ({ ...prev, currentQuantity: newQuantity }))
        }
      />
    </div>
  )
}
