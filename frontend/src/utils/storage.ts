export function readSessionJson<T>(key: string): T | null {
  const rawValue = window.sessionStorage.getItem(key)

  if (!rawValue) {
    return null
  }

  try {
    return JSON.parse(rawValue) as T
  } catch {
    window.sessionStorage.removeItem(key)
    return null
  }
}
