import { useEffect, useState } from 'react'
import { getPlayerTeamProgress, saveBulkPlayerModuleProgress } from '../../lib/api/playerProgress'
import {
  MODULE_PROGRESS_STATUSES,
  moduleProgressColor,
  moduleStatusBadgeClass,
  moduleStatusSelectClass,
  percentageForStatus,
} from '../progress/progressEnums'

export default function PlayerProgressGrid({ playerId, teamId, editable = true }) {
  const [skills, setSkills] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      const progress = await getPlayerTeamProgress(playerId, teamId)
      setSkills(progress.skills)
    } catch (err) {
      setError(err.message ?? 'Unable to load progress.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [playerId, teamId])

  function handleStatusChange(skillId, moduleId, status) {
    setSuccess(null)
    setSkills((prev) =>
      prev.map((skill) =>
        skill.skillId !== skillId
          ? skill
          : {
              ...skill,
              modules: skill.modules.map((m) => (m.moduleId === moduleId ? { ...m, status } : m)),
            },
      ),
    )
  }

  async function handleSave() {
    setSaving(true)
    setError(null)
    setSuccess(null)
    try {
      await Promise.all(
        skills
          .filter((skill) => skill.modules.length > 0)
          .map((skill) =>
            saveBulkPlayerModuleProgress({
              skillId: skill.skillId,
              playerId: Number(playerId),
              teamId: Number(teamId),
              entries: skill.modules.map((m) => ({ moduleId: m.moduleId, status: m.status })),
            }),
          ),
      )
      setSuccess('Progress saved.')
      await load()
    } catch (err) {
      setError(err.message ?? 'Unable to save progress.')
    } finally {
      setSaving(false)
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
    <div className="flex flex-col gap-6">
      {editable && (
        <div className="flex justify-end">
          <button type="button" className="btn btn-primary btn-sm" onClick={handleSave} disabled={saving}>
            {saving ? <span className="loading loading-spinner loading-xs" /> : 'Save'}
          </button>
        </div>
      )}

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}
      {success && (
        <div role="alert" className="alert alert-success text-sm">
          <span>{success}</span>
        </div>
      )}

      {skills.length === 0 ? (
        <p className="text-base-content/60 text-sm">
          No skills defined yet for this team's age group and category.
        </p>
      ) : (
        skills.map((skill) => (
          <div key={skill.skillId} className="card bg-base-100 shadow-md">
            <div className="card-body">
              <div className="flex items-center justify-between">
                <h2 className="card-title text-lg">{skill.skillName}</h2>
                <div className="flex items-center gap-2">
                  <progress
                    className={`progress ${moduleProgressColor(skill.skillPercentage)} w-24`}
                    value={skill.skillPercentage}
                    max="100"
                  />
                  <span className="text-xs whitespace-nowrap">{skill.skillPercentage}%</span>
                </div>
              </div>

              {skill.modules.length === 0 ? (
                <p className="text-base-content/60 text-sm">No modules on this skill yet.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Module</th>
                        <th>Status</th>
                        <th>Percentage</th>
                      </tr>
                    </thead>
                    <tbody>
                      {skill.modules.map((module) => (
                        <tr key={module.moduleId}>
                          <td>{module.moduleName}</td>
                          <td>
                            {editable ? (
                              <select
                                className={`select select-bordered select-sm ${moduleStatusSelectClass(module.status)}`}
                                value={module.status}
                                onChange={(e) =>
                                  handleStatusChange(skill.skillId, module.moduleId, e.target.value)
                                }
                              >
                                {MODULE_PROGRESS_STATUSES.map((value) => (
                                  <option key={value} value={value}>
                                    {value}
                                  </option>
                                ))}
                              </select>
                            ) : (
                              <span className={`badge badge-sm ${moduleStatusBadgeClass(module.status)}`}>
                                {module.status}
                              </span>
                            )}
                          </td>
                          <td>{percentageForStatus(module.status)}%</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        ))
      )}
    </div>
  )
}
