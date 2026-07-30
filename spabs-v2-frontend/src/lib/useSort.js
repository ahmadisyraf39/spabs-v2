import { useMemo, useState } from 'react'

export function useSort(items, initialKey = null, initialDirection = 'asc') {
  const [sortKey, setSortKey] = useState(initialKey)
  const [sortDirection, setSortDirection] = useState(initialDirection)

  function requestSort(key) {
    if (key === sortKey) {
      setSortDirection((d) => (d === 'asc' ? 'desc' : 'asc'))
    } else {
      setSortKey(key)
      setSortDirection('asc')
    }
  }

  const sorted = useMemo(() => {
    if (!sortKey) return items
    const copy = [...items]
    copy.sort((a, b) => {
      const av = a[sortKey]
      const bv = b[sortKey]
      if (av == null && bv == null) return 0
      if (av == null) return 1
      if (bv == null) return -1
      if (typeof av === 'string') return av.localeCompare(bv)
      return av - bv
    })
    if (sortDirection === 'desc') copy.reverse()
    return copy
  }, [items, sortKey, sortDirection])

  return { sorted, sortKey, sortDirection, requestSort }
}
