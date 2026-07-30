import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { createRecurringActivities } from "../../lib/api/activities";
import { getTeams } from "../../lib/api/teams";
import { ACTIVITY_TYPES, DAYS_OF_WEEK } from "./activityEnums";

export default function RecurringActivityFormPage() {
  const navigate = useNavigate();

  const [teams, setTeams] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [createdCount, setCreatedCount] = useState(null);

  const [applyToAllTeams, setApplyToAllTeams] = useState(false);
  const [teamIds, setTeamIds] = useState([]);
  const [type, setType] = useState("TRAINING");
  const [title, setTitle] = useState("");
  const [location, setLocation] = useState("");
  const [description, setDescription] = useState("");
  const [dayOfWeek, setDayOfWeek] = useState(DAYS_OF_WEEK[0]);
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const teamList = await getTeams();
        if (cancelled) return;
        setTeams(teamList);
      } catch (err) {
        if (!cancelled) setError(err.message ?? "Unable to load teams.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  function handleApplyToAllChange(checked) {
    setApplyToAllTeams(checked);
    if (checked) setTeamIds([]);
  }

  function handleTeamIdsChange(selectedOptions) {
    setTeamIds(Array.from(selectedOptions).map((option) => option.value));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError(null);

    if (!applyToAllTeams && teamIds.length === 0) {
      setError('Select at least one team, or check "Apply to all teams".');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        teamIds: applyToAllTeams ? [] : teamIds.map(Number),
        applyToAllTeams,
        type,
        title: title || null,
        location: location || null,
        description: description || null,
        dayOfWeek,
        startTime,
        endTime: endTime || null,
        startDate,
        endDate,
      };
      const created = await createRecurringActivities(payload);
      setCreatedCount(created.length);
    } catch (err) {
      setError(err.message ?? "Unable to generate recurring activities.");
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    );
  }

  if (createdCount !== null) {
    return (
      <div className="mx-auto max-w-lg">
        <h1 className="mb-4 text-2xl font-semibold">
          Generate recurring training
        </h1>
        <div className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md">
          <div role="alert" className="alert alert-success text-sm">
            <span>
              Created {createdCount} training session
              {createdCount === 1 ? "" : "s"}.
            </span>
          </div>
          <div className="flex gap-3">
            <Link to="/activities" className="btn btn-primary">
              Go to Attendance
            </Link>
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => setCreatedCount(null)}
            >
              Generate another batch
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold">Generate recurring training</h1>

      <form className="flex flex-col gap-6" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <div className="card bg-base-100 shadow-md">
            <div className="card-body">
              <h2 className="card-title text-lg">Teams</h2>

              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  className="checkbox checkbox-sm"
                  checked={applyToAllTeams}
                  onChange={(e) => handleApplyToAllChange(e.target.checked)}
                />
                <span className="label-text">Apply to all teams</span>
              </label>

              <label className="form-control">
                <span className="label-text mb-1">Teams</span>
                <select
                  multiple
                  className="select select-bordered h-auto w-full"
                  size={8}
                  value={teamIds}
                  onChange={(e) =>
                    handleTeamIdsChange(e.target.selectedOptions)
                  }
                  disabled={applyToAllTeams}
                >
                  {teams.map((team) => (
                    <option key={team.id} value={team.id}>
                      {team.name}
                    </option>
                  ))}
                </select>
                <span className="label-text-alt text-base-content/60 mt-1">
                  Ctrl/Cmd+click to select multiple teams.
                </span>
              </label>
            </div>
          </div>

          <div className="card bg-base-100 shadow-md lg:col-span-2">
            <div className="card-body">
              <h2 className="card-title text-lg">Session details</h2>

              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <label className="form-control">
                  <span className="label-text mb-1">Type</span>
                  <select
                    className="select select-bordered w-full"
                    value={type}
                    onChange={(e) => setType(e.target.value)}
                  >
                    {ACTIVITY_TYPES.map((value) => (
                      <option key={value} value={value}>
                        {value}
                      </option>
                    ))}
                  </select>
                </label>

                <label className="form-control">
                  <span className="label-text mb-1">Title</span>
                  <input
                    type="text"
                    className="input input-bordered w-full"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="Optional — falls back to the type if left blank"
                  />
                </label>

                <label className="form-control">
                  <span className="label-text mb-1">Location</span>
                  <input
                    type="text"
                    className="input input-bordered w-full"
                    value={location}
                    onChange={(e) => setLocation(e.target.value)}
                  />
                </label>
              </div>

              <label className="form-control mt-4">
                <span className="label-text mb-1">Description</span>
                <textarea
                  className="textarea textarea-bordered w-full"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  rows={3}
                />
              </label>
            </div>
          </div>
        </div>

        <div className="card bg-base-100 shadow-md">
          <div className="card-body">
            <h2 className="card-title text-lg">Schedule</h2>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
              <label className="form-control">
                <span className="label-text mb-1">Day of week</span>
                <select
                  className="select select-bordered w-full"
                  value={dayOfWeek}
                  onChange={(e) => setDayOfWeek(e.target.value)}
                >
                  {DAYS_OF_WEEK.map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </select>
              </label>

              <label className="form-control">
                <span className="label-text mb-1">Start time</span>
                <input
                  type="time"
                  className="input input-bordered w-full"
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  required
                />
              </label>

              <label className="form-control">
                <span className="label-text mb-1">End time</span>
                <input
                  type="time"
                  className="input input-bordered w-full"
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                />
              </label>

              <label className="form-control">
                <span className="label-text mb-1">Start date</span>
                <input
                  type="date"
                  className="input input-bordered w-full"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  required
                />
              </label>

              <label className="form-control">
                <span className="label-text mb-1">End date</span>
                <input
                  type="date"
                  className="input input-bordered w-full"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  required
                />
              </label>
            </div>
          </div>
        </div>

        <div className="flex justify-end gap-3">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => navigate("/activities")}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={submitting}
          >
            {submitting ? (
              <span className="loading loading-spinner loading-sm" />
            ) : (
              "Generate"
            )}
          </button>
        </div>
      </form>
    </div>
  );
}
