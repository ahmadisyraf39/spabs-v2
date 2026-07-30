export default function SortableTh({ label, sortKey, currentKey, direction, onSort, className }) {
  const active = sortKey === currentKey
  return (
    <th
      className={`hover:bg-base-200 cursor-pointer select-none ${className ?? ''}`}
      onClick={() => onSort(sortKey)}
    >
      <span className="inline-flex items-center gap-1">
        {label}
        {active && <span className="text-xs">{direction === 'asc' ? '▲' : '▼'}</span>}
      </span>
    </th>
  )
}
