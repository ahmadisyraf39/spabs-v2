import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createModule, getModule, updateModule } from '../../lib/api/modules'
import { getSkill } from '../../lib/api/skills'

const CRITERIA_FIELDS = [
  { key: 'criteria25', label: 'Criteria — 25%' },
  { key: 'criteria50', label: 'Criteria — 50%' },
  { key: 'criteria75', label: 'Criteria — 75%' },
  { key: 'criteria100', label: 'Criteria — 100%' },
]

export default function ModuleFormPage() {
  const { skillId: skillIdParam, id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const [skillId, setSkillId] = useState(skillIdParam ?? null)
  const [skillLabel, setSkillLabel] = useState(null)
  const [name, setName] = useState('')
  const [criteria, setCriteria] = useState({
    criteria25: '',
    criteria50: '',
    criteria75: '',
    criteria100: '',
  })

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        if (isEdit) {
          const module = await getModule(id)
          if (cancelled) return
          setSkillId(module.skillId)
          setName(module.name)
          setCriteria({
            criteria25: module.criteria25 ?? '',
            criteria50: module.criteria50 ?? '',
            criteria75: module.criteria75 ?? '',
            criteria100: module.criteria100 ?? '',
          })
          const skill = await getSkill(module.skillId)
          if (cancelled) return
          setSkillLabel(`${skill.name} (${skill.ageGroup}, ${skill.category})`)
        } else {
          const skill = await getSkill(skillIdParam)
          if (cancelled) return
          setSkillLabel(`${skill.name} (${skill.ageGroup}, ${skill.category})`)
        }
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load module.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [id, isEdit, skillIdParam])

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const payload = {
        skillId: Number(skillId),
        name,
        criteria25: criteria.criteria25 || null,
        criteria50: criteria.criteria50 || null,
        criteria75: criteria.criteria75 || null,
        criteria100: criteria.criteria100 || null,
      }
      const module = isEdit ? await updateModule(id, payload) : await createModule(payload)
      navigate(`/skills/${module.skillId}`)
    } catch (err) {
      setError(err.message ?? 'Unable to save module.')
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
      <h1 className="mb-4 text-2xl font-semibold">{isEdit ? 'Edit module' : 'New module'}</h1>

      <form className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        <div className="form-control">
          <span className="label-text mb-1">Skill</span>
          <p className="text-sm">{skillLabel}</p>
        </div>

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

        {CRITERIA_FIELDS.map(({ key, label }) => (
          <label key={key} className="form-control">
            <span className="label-text mb-1">{label}</span>
            <textarea
              className="textarea textarea-bordered w-full"
              value={criteria[key]}
              onChange={(e) => setCriteria((prev) => ({ ...prev, [key]: e.target.value }))}
              rows={2}
              placeholder="Optional"
            />
          </label>
        ))}

        <div className="mt-2 flex justify-end gap-3">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => navigate(skillId ? `/skills/${skillId}` : '/skills')}
          >
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
