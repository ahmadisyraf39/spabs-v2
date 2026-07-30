export function getAge(dateOfBirth) {
  if (!dateOfBirth) return null
  const birthYear = Number(dateOfBirth.slice(0, 4))
  if (Number.isNaN(birthYear)) return null
  return new Date().getFullYear() - birthYear
}

export const AGE_GROUPS = ['U8', 'U10', 'U12', 'U14', 'U16', 'U18', 'OPEN']

const AGE_GROUP_MAX_AGE = { U8: 8, U10: 10, U12: 12, U14: 14, U16: 16, U18: 18 }

export function ageGroupForAge(age) {
  if (age == null) return null
  for (const group of AGE_GROUPS) {
    const max = AGE_GROUP_MAX_AGE[group]
    if (max !== undefined && age < max) return group
  }
  return 'OPEN'
}
