import request from '@/utils/request'

const PERSON_CACHE_TTL = 30 * 60 * 1000
const personCache = new Map()

function cacheKey(type, id) {
  return `${String(type).toLowerCase()}:${id}`
}

export function getPersonDetail(type, id, options = {}) {
  const key = cacheKey(type, id)
  const now = Date.now()
  const cached = personCache.get(key)
  if (!options.force && cached?.response && cached.expiresAt > now) {
    return Promise.resolve(cached.response)
  }
  if (!options.force && cached?.pending) {
    return cached.pending
  }

  const pending = request({
    url: `/people/${type}/${id}`,
    method: 'get'
  })
    .then((response) => {
      personCache.set(key, { response, expiresAt: Date.now() + PERSON_CACHE_TTL })
      return response
    })
    .catch((error) => {
      personCache.delete(key)
      throw error
    })

  personCache.set(key, { pending, expiresAt: now + PERSON_CACHE_TTL })
  return pending
}

export function prefetchPersonDetail(type, id) {
  return getPersonDetail(type, id).catch(() => null)
}

export function clearPersonDetailCache(type, id) {
  if (type && id) personCache.delete(cacheKey(type, id))
  else personCache.clear()
}