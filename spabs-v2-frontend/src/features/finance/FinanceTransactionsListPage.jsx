import { Fragment, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import {
  deleteFinanceTransaction,
  getFinanceSummary,
  getFinanceTransactions,
} from '../../lib/api/financeTransactions'
import { formatCurrency } from '../../lib/formatCurrency'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import {
  EXPENSE_CATEGORIES,
  FINANCE_CATEGORIES,
  FINANCE_TYPES,
  INCOME_CATEGORIES,
  financeReferenceLabel,
  financeTypeBadgeClass,
} from './financeLedgerEnums'

export default function FinanceTransactionsListPage() {
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  const [typeFilter, setTypeFilter] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [search, setSearch] = useState('')

  const [summary, setSummary] = useState(null)
  const [summaryLoading, setSummaryLoading] = useState(true)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadTransactions() {
    setLoading(true)
    setError(null)
    try {
      setTransactions(await getFinanceTransactions())
    } catch (err) {
      setError(err.message ?? 'Unable to load finance transactions.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadTransactions()
  }, [])

  useEffect(() => {
    let cancelled = false
    async function loadSummary() {
      setSummaryLoading(true)
      try {
        const s = await getFinanceSummary(startDate || undefined, endDate || undefined)
        if (!cancelled) setSummary(s)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load finance summary.')
      } finally {
        if (!cancelled) setSummaryLoading(false)
      }
    }
    loadSummary()
    return () => {
      cancelled = true
    }
  }, [startDate, endDate])

  const filteredTransactions = useMemo(() => {
    const term = search.trim().toLowerCase()
    return transactions.filter((t) => {
      const matchesStart = !startDate || t.transactionDate >= startDate
      const matchesEnd = !endDate || t.transactionDate <= endDate
      const matchesType = !typeFilter || t.financeType === typeFilter
      const matchesCategory = !categoryFilter || t.financeCategory === categoryFilter
      const matchesTerm = !term || (t.description ?? '').toLowerCase().includes(term)
      return matchesStart && matchesEnd && matchesType && matchesCategory && matchesTerm
    })
  }, [transactions, startDate, endDate, typeFilter, categoryFilter, search])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredTransactions, 'transactionDate', 'desc')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  const incomeBreakdown = useMemo(
    () => (summary ? INCOME_CATEGORIES.map((c) => [c, summary.byCategory?.[c] ?? 0]).filter(([, v]) => v) : []),
    [summary],
  )
  const expenseBreakdown = useMemo(
    () => (summary ? EXPENSE_CATEGORIES.map((c) => [c, summary.byCategory?.[c] ?? 0]).filter(([, v]) => v) : []),
    [summary],
  )

  async function handleDelete() {
    setDeleting(true)
    try {
      await deleteFinanceTransaction(deleteTarget.id)
      setTransactions((prev) => prev.filter((t) => t.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete transaction.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Ledger</h1>
        <Link to="/finance-transactions/new" className="btn btn-primary btn-sm">
          New transaction
        </Link>
      </div>

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}

      <div className="flex flex-wrap justify-end items-end gap-3">
        <label className="form-control">
          <span className="label-text mb-1 text-xs">From</span>
          <input
            type="date"
            className="input input-bordered input-sm"
            value={startDate}
            onChange={(e) => {
              setStartDate(e.target.value)
              resetToFirstPage()
            }}
          />
        </label>
        <label className="form-control">
          <span className="label-text mb-1 text-xs">To</span>
          <input
            type="date"
            className="input input-bordered input-sm"
            value={endDate}
            onChange={(e) => {
              setEndDate(e.target.value)
              resetToFirstPage()
            }}
          />
        </label>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="card-body">
          <h2 className="card-title text-lg">Summary{startDate || endDate ? ' (filtered range)' : ' (all time)'}</h2>
          {summaryLoading ? (
            <div className="flex justify-center py-6">
              <span className="loading loading-spinner text-primary" />
            </div>
          ) : (
            <div className="flex flex-col gap-4">
              <div className="stats stats-vertical sm:stats-horizontal bg-base-100 border">
                <div className="stat">
                  <div className="stat-title">Total income</div>
                  <div className="stat-value text-success text-2xl">{formatCurrency(summary.totalIncome)}</div>
                </div>
                <div className="stat">
                  <div className="stat-title">Total expense</div>
                  <div className="stat-value text-error text-2xl">{formatCurrency(summary.totalExpense)}</div>
                </div>
                <div className="stat">
                  <div className="stat-title">Net balance</div>
                  <div className="stat-value text-2xl">{formatCurrency(summary.netBalance)}</div>
                </div>
              </div>

              {(incomeBreakdown.length > 0 || expenseBreakdown.length > 0) && (
                <div className="grid grid-cols-1 gap-4 text-sm sm:grid-cols-2">
                  <div>
                    <h3 className="text-base-content/60 mb-1 font-medium">Income by category</h3>
                    <dl className="grid grid-cols-[1fr_auto] gap-x-4 gap-y-1">
                      {incomeBreakdown.map(([category, value]) => (
                        <Fragment key={category}>
                          <dt>{category}</dt>
                          <dd>{formatCurrency(value)}</dd>
                        </Fragment>
                      ))}
                      {incomeBreakdown.length === 0 && <dd className="text-base-content/60">No income recorded.</dd>}
                    </dl>
                  </div>
                  <div>
                    <h3 className="text-base-content/60 mb-1 font-medium">Expense by category</h3>
                    <dl className="grid grid-cols-[1fr_auto] gap-x-4 gap-y-1">
                      {expenseBreakdown.map(([category, value]) => (
                        <Fragment key={category}>
                          <dt>{category}</dt>
                          <dd>{formatCurrency(value)}</dd>
                        </Fragment>
                      ))}
                      {expenseBreakdown.length === 0 && (
                        <dd className="text-base-content/60">No expenses recorded.</dd>
                      )}
                    </dl>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center w-full">
        <input
          type="text"
          placeholder="Search description"
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
            {FINANCE_TYPES.map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
          <select
            className="select select-bordered select-sm flex-1 sm:flex-initial"
            value={categoryFilter}
            onChange={(e) => {
              setCategoryFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All categories</option>
            {FINANCE_CATEGORIES.map((value) => (
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
                  <th className="w-[10%]">Type</th>
                  <th className="hidden sm:table-cell w-[16%]">Category</th>
                  <SortableTh
                    label="Amount"
                    sortKey="amount"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[10%]"
                  />
                  <SortableTh
                    label="Date"
                    sortKey="transactionDate"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[12%]"
                  />
                  <th className="w-[24%]">Description</th>
                  <th className="hidden sm:table-cell w-[14%]">Reference</th>
                  <th className="w-[14%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((t) => (
                  <tr key={t.id}>
                    <td>
                      <span className={`badge badge-sm ${financeTypeBadgeClass(t.financeType)}`}>
                        {t.financeType}
                      </span>
                    </td>
                    <td className="hidden sm:table-cell truncate" title={t.financeCategory}>
                      {t.financeCategory}
                    </td>
                    <td>{formatCurrency(t.amount)}</td>
                    <td>{t.transactionDate}</td>
                    <td className="truncate" title={t.description}>
                      {t.description || '—'}
                    </td>
                    <td className="hidden sm:table-cell truncate">
                      {financeReferenceLabel(t.referenceType, t.referenceId) ?? (
                        <span className="text-base-content/40">—</span>
                      )}
                    </td>
                    <td className="whitespace-nowrap">
                      <Link to={`/finance-transactions/${t.id}/edit`} className="btn btn-ghost btn-xs">
                        Edit
                      </Link>
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
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={7} className="text-base-content/60 py-6 text-center">
                      No transactions found.
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
          title="Delete transaction"
          body={
            deleteTarget.referenceType
              ? `Permanently delete this ${deleteTarget.financeType.toLowerCase()} transaction? It's linked to a ${financeReferenceLabel(deleteTarget.referenceType, deleteTarget.referenceId)} — deleting it here won't affect that record, but the two will no longer be in sync. This cannot be undone.`
              : `Permanently delete this ${deleteTarget.financeType.toLowerCase()} transaction? This cannot be undone.`
          }
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
