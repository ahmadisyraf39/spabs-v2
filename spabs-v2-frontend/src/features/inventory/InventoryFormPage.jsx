import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createInventory, getInventory, updateInventory } from '../../lib/api/inventories'
import { INVENTORY_CATEGORIES } from './inventoryEnums'

export default function InventoryFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(isEdit)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const [name, setName] = useState('')
  const [category, setCategory] = useState(INVENTORY_CATEGORIES[0])
  const [description, setDescription] = useState('')
  const [initialQuantity, setInitialQuantity] = useState('0')
  const [currentQuantity, setCurrentQuantity] = useState(0)

  useEffect(() => {
    if (!isEdit) return
    let cancelled = false

    async function load() {
      try {
        const item = await getInventory(id)
        if (cancelled) return
        setName(item.name)
        setCategory(item.category)
        setDescription(item.description ?? '')
        setCurrentQuantity(item.currentQuantity)
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
  }, [id, isEdit])

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const payload = {
        name,
        category,
        description: description || null,
        currentQuantity: isEdit ? currentQuantity : Number(initialQuantity),
      }
      const item = isEdit ? await updateInventory(id, payload) : await createInventory(payload)
      navigate(`/inventory/${item.id}`)
    } catch (err) {
      setError(err.message ?? 'Unable to save inventory item.')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-lg">
      <h1 className="mb-4 text-2xl font-semibold">{isEdit ? 'Edit inventory item' : 'New inventory item'}</h1>

      <form className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        <label className="form-control">
          <span className="label-text mb-1">Name</span>
          <input
            type="text"
            className="input input-bordered w-full"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Category</span>
          <select
            className="select select-bordered w-full"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
          >
            {INVENTORY_CATEGORIES.map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Description</span>
          <textarea
            className="textarea textarea-bordered w-full"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={3}
          />
        </label>

        {isEdit ? (
          <div className="form-control">
            <span className="label-text mb-1">Current quantity</span>
            <p className="text-sm">
              {currentQuantity} — adjust via transactions on the item's Details page, not here.
            </p>
          </div>
        ) : (
          <label className="form-control">
            <span className="label-text mb-1">Initial quantity</span>
            <input
              type="number"
              className="input input-bordered w-full"
              value={initialQuantity}
              onChange={(e) => setInitialQuantity(e.target.value)}
              min="0"
              required
            />
          </label>
        )}

        <div className="mt-2 flex justify-end gap-3">
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/inventory')}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? <span className="loading loading-spinner loading-sm" /> : 'Save'}
          </button>
        </div>
      </form>
    </div>
  )
}
