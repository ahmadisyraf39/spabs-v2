import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import { deleteModule, getModulesBySkill } from '../../lib/api/modules'

export default function ModulesSection({ skillId }) {
  const [modules, setModules] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadModules() {
    setLoading(true)
    setError(null)
    try {
      setModules(await getModulesBySkill(skillId))
    } catch (err) {
      setError(err.message ?? 'Unable to load modules.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadModules()
  }, [skillId])

  async function handleDelete() {
    setDeleting(true)
    try {
      await deleteModule(deleteTarget.id)
      setModules((prev) => prev.filter((m) => m.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete module.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="card bg-base-100 shadow-md">
      <div className="card-body">
        <div className="flex items-center justify-between">
          <h2 className="card-title text-lg">Modules</h2>
          <Link to={`/skills/${skillId}/modules/new`} className="btn btn-primary btn-sm">
            Add module
          </Link>
        </div>

        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        {loading ? (
          <div className="flex justify-center py-6">
            <span className="loading loading-spinner text-primary" />
          </div>
        ) : modules.length === 0 ? (
          <p className="text-base-content/60 text-sm">No modules on this skill yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th className="text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {modules.map((module) => (
                  <tr key={module.id}>
                    <td>{module.name}</td>
                    <td className="text-right whitespace-nowrap">
                      <Link to={`/modules/${module.id}/edit`} className="btn btn-ghost btn-xs">
                        Edit
                      </Link>
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => setDeleteTarget(module)}
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
      </div>

      {deleteTarget && (
        <ConfirmModal
          title="Delete module"
          body={`Permanently delete "${deleteTarget.name}"? This will fail if any player already has progress recorded on it — remove that progress first if so. This cannot be undone.`}
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
