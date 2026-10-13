const BASE = '/images/products'

export const PLACEHOLDER_IMAGE = `${BASE}/placeholder.svg`

const CATEGORY_FALLBACK = {
  laptops: 'laptop.svg',
  smartphones: 'smartphone.svg',
  gpus: 'graphics-card.svg',
  cpus: 'processor.svg',
  monitors: 'monitor.svg',
  ram: 'ram.svg',
  storage: 'ssd.svg',
  chargers: 'charger.svg',
  accessories: 'keyboard.svg'
}

const KEYWORD_RULES = [
  [/gaming/, 'gaming-laptop.svg'],
  [/creator/, 'creator-laptop.svg'],
  [/ultrabook|\bswift\b/, 'ultrabook.svg'],
  [/business/, 'business-laptop.svg'],
  [/\bpsu\b|power supply/, 'psu.svg'],
  [/adapter/, 'laptop-adapter.svg'],
  [/charger|charging/, 'charger.svg'],
  [/keyboard/, 'keyboard.svg'],
  [/mouse/, 'mouse.svg'],
  [/graphics|\bgpu\b|geforce|radeon/, 'graphics-card.svg'],
  [/processor|\bcpu\b|ryzen/, 'processor.svg'],
  [/monitor|display/, 'monitor.svg'],
  [/\bddr\d?\b|\bram\b|memory/, 'ram.svg'],
  [/\bssd\b|nvme|sata|storage/, 'ssd.svg'],
  [/smartphone|\bphone\b|\b5g\b|galaxy|iphone|pixel/, 'smartphone.svg'],
  [/laptop|notebook|macbook/, 'laptop.svg']
]

const UNRELIABLE_HOST = /picsum\.photos|loremflickr|placehold\.co|placeholder\.com|via\.placeholder|dummyimage|lorempixel|source\.unsplash\.com|unsplash\.com\/featured/i

export function isUnreliableUrl(url) {
  if (!url || typeof url !== 'string') return true
  return UNRELIABLE_HOST.test(url)
}

function productSpecificLocal(product) {
  const name = (product?.name || '').toLowerCase()
  if (!name) return null
  const match = KEYWORD_RULES.find(([re]) => re.test(name))
  return match ? `${BASE}/${match[1]}` : null
}

function categoryLocal(product) {
  const slug = (product?.categorySlug || '').toLowerCase()
  return CATEGORY_FALLBACK[slug] ? `${BASE}/${CATEGORY_FALLBACK[slug]}` : null
}

export function localAssetUrl(product) {
  return productSpecificLocal(product) || categoryLocal(product) || PLACEHOLDER_IMAGE
}

export function candidateImageUrls(product, overrideUrl) {
  const urls = []
  const push = (u) => { if (u && !urls.includes(u)) urls.push(u) }

  const primary = overrideUrl || product?.primaryImageUrl
  if (!isUnreliableUrl(primary)) push(primary)
  push(productSpecificLocal(product))
  push(categoryLocal(product))
  push(PLACEHOLDER_IMAGE)
  return urls
}
