import { useEffect, useState } from 'react'
import ConfirmModal from '../../components/ui/ConfirmModal'
import { createPlayerParent, deletePlayerParent } from '../../lib/api/playerParents'
import { getParentLinksForPlayer, getParentOptions } from '../../lib/playerRelations'
import { PARENT_RELATIONSHIPS } from './playerEnums'

export default function GuardianSection({ playerId }) {
  const [links, setLinks] = useState([])
  const [parentOptions, setParentOptions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [selectedParentId, setSelectedParentId] = useState('')
  const [selectedRelationship, setSelectedRelationship] = useState(PARENT_RELATIONSHIPS[0])
  const [adding, setAdding] = useState(false)
  const [removeTarget, setRemoveTarget] = useState(null)
  const [removing, setRemoving] = useState(false)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [linkList, options] = await Promise.all([
          getParentLinksForPlayer(playerId),
          getParentOptions(),
        ])
        if (cancelled) return
        setLinks(linkList)
        setParentOptions(options)
        setSelectedParentId((prev) => prev || String(options[0]?.parentId ?? ''))
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load guardians.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [playerId])

  function labelFor(parentId) {
    return parentOptions.find((o) => o.parentId === parentId)?.label ?? `Parent #${parentId}`
  }

  async function handleAdd() {
    if (!selectedParentId) return
    setAdding(true)
    setError(null)
    try {
      const link = await createPlayerParent({
        playerId: Number(playerId),
        parentId: Number(selectedParentId),
        relationship: selectedRelationship,
      })
      setLinks((prev) => [...prev, link])
    } catch (err) {
      setError(err.message ?? 'Unable to add guardian.')
    } finally {
      setAdding(false)
    }
  }

  async function handleRemove() {
    setRemoving(true)
    setError(null)
    try {
      await deletePlayerParent(removeTarget.id)
      setLinks((prev) => prev.filter((l) => l.id !== removeTarget.id))
      setRemoveTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to remove guardian.')
    } finally {
      setRemoving(false)
    }
  }

  return (
    <div className="card bg-base-100 shadow-md">
      <div className="card-body">
        <h2 className="card-title text-lg">Parents / Guardians</h2>

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
            {links.length === 0 && (
              <p className="text-base-content/60 text-sm">No guardians linked yet.</p>
            )}
            <ul className="flex flex-col gap-2">
              {links.map((link) => (
                <li key={link.id} className="bg-base-200 flex items-center justify-between rounded px-3 py-2">
                  <div className="flex items-center gap-2">
                    <span className="text-sm">{labelFor(link.parentId)}</span>
                    <span className="badge badge-ghost badge-sm">{link.relationship}</span>
                  </div>
                  <button
                    type="button"
                    className="btn btn-ghost btn-xs text-error"
                    onClick={() => setRemoveTarget(link)}
                  >
                    Remove
                  </button>
                </li>
              ))}
            </ul>

            <div className="mt-4 flex flex-wrap items-end gap-3">
              <label className="form-control">
                <span className="label-text mb-1">Parent</span>
                <select
                  className="select select-bordered select-sm"
                  value={selectedParentId}
                  onChange={(e) => setSelectedParentId(e.target.value)}
                  disabled={parentOptions.length === 0}
                >
                  {parentOptions.length === 0 && <option value="">No parent accounts found</option>}
                  {parentOptions.map((option) => (
                    <option key={option.parentId} value={option.parentId}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="form-control">
                <span className="label-text mb-1">Relationship</span>
                <select
                  className="select select-bordered select-sm"
                  value={selectedRelationship}
                  onChange={(e) => setSelectedRelationship(e.target.value)}
                >
                  {PARENT_RELATIONSHIPS.map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </select>
              </label>

              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={handleAdd}
                disabled={adding || !selectedParentId}
              >
                {adding ? <span className="loading loading-spinner loading-xs" /> : 'Add guardian'}
              </button>
            </div>
          </>
        )}
      </div>

      {removeTarget && (
        <ConfirmModal
          title="Remove guardian"
          body={`Remove ${labelFor(removeTarget.parentId)} (${removeTarget.relationship}) as a guardian of this player? This cannot be undone.`}
          confirmLabel={removing ? 'Removing…' : 'Remove'}
          onConfirm={handleRemove}
          onCancel={() => setRemoveTarget(null)}
        />
      )}
    </div>
  )
}
