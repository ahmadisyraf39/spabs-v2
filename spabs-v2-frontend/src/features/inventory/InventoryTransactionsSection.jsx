import { useEffect, useState } from 'react'
import ConfirmModal from '../../components/ui/ConfirmModal'
import { getInventory } from '../../lib/api/inventories'
import {
  createInventoryTransaction,
  deleteInventoryTransaction,
  getInventoryTransactionsByInventory,
} from '../../lib/api/inventoryTransactions'
import { formatCurrency } from '../../lib/formatCurrency'
import { INVENTORY_TRANSACTION_TYPES, quantityEffectLabel, transactionTypeBadgeClass } from './inventoryEnums'

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

export default function InventoryTransactionsSection({ inventoryId, onQuantityChange }) {
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [transactionType, setTransactionType] = useState(INVENTORY_TRANSACTION_TYPES[0])
  const [quantity, setQuantity] = useState('')
  const [transactionDate, setTransactionDate] = useState(todayIso())
  const [price, setPrice] = useState('')
  const [remarks, setRemarks] = useState('')
  const [adding, setAdding] = useState(false)

  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadTransactions() {
    setLoading(true)
    setError(null)
    try {
      setTransactions(await getInventoryTransactionsByInventory(inventoryId))
    } catch (err) {
      setError(err.message ?? 'Unable to load transactions.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadTransactions()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [inventoryId])

  async function refreshQuantity() {
    const inventory = await getInventory(inventoryId)
    onQuantityChange(inventory.currentQuantity)
  }

  async function handleAdd() {
    if (!quantity) return
    setAdding(true)
    setError(null)
    try {
      const created = await createInventoryTransaction({
        inventoryId: Number(inventoryId),
        transactionType,
        quantity: Number(quantity),
        transactionDate,
        price: price === '' ? null : Number(price),
        remarks: remarks || null,
      })
      setTransactions((prev) => [created, ...prev])
      await refreshQuantity()
      setQuantity('')
      setPrice('')
      setRemarks('')
    } catch (err) {
      setError(err.message ?? 'Unable to add transaction.')
    } finally {
      setAdding(false)
    }
  }

  async function handleDelete() {
    setDeleting(true)
    try {
      await deleteInventoryTransaction(deleteTarget.id)
      setTransactions((prev) => prev.filter((t) => t.id !== deleteTarget.id))
      await refreshQuantity()
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete transaction.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="card bg-base-100 shadow-md">
      <div className="card-body">
        <h2 className="card-title text-lg">Transactions</h2>

        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        {loading ? (
          <div className="flex justify-center py-6">
            <span className="loading loading-spinner text-primary" />
          </div>
        ) : (
          <>
            <div className="mb-4 flex flex-wrap items-end gap-3">
              <label className="form-control">
                <span className="label-text mb-1">Type</span>
                <select
                  className="select select-bordered select-sm"
                  value={transactionType}
                  onChange={(e) => setTransactionType(e.target.value)}
                >
                  {INVENTORY_TRANSACTION_TYPES.map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </select>
              </label>

              <label className="form-control">
                <span className="label-text mb-1">
                  {transactionType === 'ADJUSTMENT' ? 'New quantity' : 'Quantity'}
                </span>
                <input
                  type="number"
                  className="input input-bordered input-sm w-28"
                  value={quantity}
                  onChange={(e) => setQuantity(e.target.value)}
                  min="0"
                />
              </label>

              <label className="form-control">
                <span className="label-text mb-1">Date</span>
                <input
                  type="date"
                  className="input input-bordered input-sm"
                  value={transactionDate}
                  onChange={(e) => setTransactionDate(e.target.value)}
                />
              </label>

              <label className="form-control">
                <span className="label-text mb-1">Price (RM)</span>
                <input
                  type="number"
                  className="input input-bordered input-sm w-28"
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                  min="0"
                  step="0.01"
                  placeholder="Optional"
                />
              </label>

              <label className="form-control">
                <span className="label-text mb-1">Remarks</span>
                <input
                  type="text"
                  className="input input-bordered input-sm"
                  value={remarks}
                  onChange={(e) => setRemarks(e.target.value)}
                  placeholder="Optional"
                />
              </label>

              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={handleAdd}
                disabled={adding || !quantity}
              >
                {adding ? <span className="loading loading-spinner loading-xs" /> : 'Add transaction'}
              </button>
            </div>

            {transactions.length === 0 ? (
              <p className="text-base-content/60 text-sm">No transactions recorded yet.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Type</th>
                      <th>Quantity</th>
                      <th>Date</th>
                      <th>Price</th>
                      <th>Remarks</th>
                      <th className="text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.map((t) => (
                      <tr key={t.id}>
                        <td>
                          <span className={`badge badge-sm ${transactionTypeBadgeClass(t.transactionType)}`}>
                            {t.transactionType}
                          </span>
                        </td>
                        <td>{quantityEffectLabel(t.transactionType, t.quantity)}</td>
                        <td>{t.transactionDate}</td>
                        <td>{t.price != null ? formatCurrency(t.price) : '—'}</td>
                        <td className="max-w-xs truncate" title={t.remarks ?? ''}>
                          {t.remarks || '—'}
                        </td>
                        <td className="text-right whitespace-nowrap">
                          <button
                            type="button"
                            className="btn btn-ghost btn-xs text-error"
                            onClick={() => setDeleteTarget(t)}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </div>

      {deleteTarget && (
        <ConfirmModal
          title="Delete transaction"
          body="Permanently delete this transaction? Its effect on the current quantity will be re-derived from the item's own record afterward — this cannot be undone."
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
