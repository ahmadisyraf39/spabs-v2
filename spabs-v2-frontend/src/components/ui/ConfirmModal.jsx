export default function ConfirmModal({
  title,
  body,
  confirmLabel = 'Confirm',
  confirmClass = 'btn-error',
  onConfirm,
  onCancel,
}) {
  return (
    <div className="modal modal-open">
      <div className="modal-box">
        <h3 className="text-lg font-semibold">{title}</h3>
        <p className="text-base-content/70 mt-2 text-sm">{body}</p>
        <div className="modal-action">
          <button type="button" className="btn" onClick={onCancel}>
            Cancel
          </button>
          <button type="button" className={`btn ${confirmClass}`} onClick={onConfirm}>
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
