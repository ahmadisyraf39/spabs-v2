import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { getModulesBySkill } from '../../lib/api/modules'
import { deleteSkill, getSkills } from '../../lib/api/skills'
import { AGE_GROUPS } from '../../lib/ageUtils'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { TEAM_CATEGORIES } from '../teams/teamEnums'

export default function SkillsListPage() {
  const [skills, setSkills] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [ageGroupFilter, setAgeGroupFilter] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleteBlockedCount, setDeleteBlockedCount] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadSkills() {
    setLoading(true)
    setError(null)
    try {
      setSkills(await getSkills())
    } catch (err) {
      setError(err.message ?? 'Unable to load skills.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadSkills()
  }, [])

  const filteredSkills = useMemo(() => {
    const term = search.trim().toLowerCase()
    return skills.filter((s) => {
      const matchesTerm = !term || s.name.toLowerCase().includes(term)
      const matchesAgeGroup = !ageGroupFilter || s.ageGroup === ageGroupFilter
      const matchesCategory = !categoryFilter || s.category === categoryFilter
      return matchesTerm && matchesAgeGroup && matchesCategory
    })
  }, [skills, search, ageGroupFilter, categoryFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredSkills, 'name')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handleDeleteClick(skill) {
    setDeleteTarget(skill)
    setDeleteBlockedCount(null)
    try {
      const modules = await getModulesBySkill(skill.id)
      setDeleteBlockedCount(modules.length)
    } catch (err) {
      setError(err.message ?? 'Unable to check this skill for modules.')
      setDeleteTarget(null)
    }
  }

  async function handleConfirmDelete() {
    setDeleting(true)
    try {
      await deleteSkill(deleteTarget.id)
      setSkills((prev) => prev.filter((s) => s.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete skill.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Skills</h1>
        <Link to="/skills/new" className="btn btn-primary btn-sm">
          New skill
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
            value={ageGroupFilter}
            onChange={(e) => {
              setAgeGroupFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All age groups</option>
            {AGE_GROUPS.map((value) => (
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
            {TEAM_CATEGORIES.map((value) => (
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
                    label="Age group"
                    sortKey="ageGroup"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="hidden sm:table-cell w-[18%]"
                  />
                  <SortableTh
                    label="Category"
                    sortKey="category"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[18%]"
                  />
                  <th className="w-[28%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((skill) => (
                  <tr key={skill.id}>
                    <td className="truncate" title={skill.name}>
                      {skill.name}
                    </td>
                    <td className="hidden sm:table-cell">
                      <span className="badge badge-ghost badge-sm">{skill.ageGroup}</span>
                    </td>
                    <td>
                      <span className="badge badge-ghost badge-sm">{skill.category}</span>
                    </td>
                    <td className="whitespace-nowrap">
                      <Link to={`/skills/${skill.id}`} className="btn btn-ghost btn-xs text-info">
                        Details
                      </Link>
                      <Link to={`/skills/${skill.id}/edit`} className="btn btn-ghost btn-xs">
                        Edit
                      </Link>
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => handleDeleteClick(skill)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={4} className="text-base-content/60 py-6 text-center">
                      No skills found.
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
            <h3 className="text-lg font-semibold">Cannot delete skill</h3>
            <p className="text-base-content/70 mt-2 text-sm">
              "{deleteTarget.name}" still has {deleteBlockedCount} module
              {deleteBlockedCount === 1 ? '' : 's'} attached. Delete{' '}
              {deleteBlockedCount === 1 ? 'it' : 'them'} first from the skill's Details page.
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
          title="Delete skill"
          body={`Permanently delete "${deleteTarget.name}"? This cannot be undone.`}
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleConfirmDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
