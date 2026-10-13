import { describe, it, expect } from 'vitest'
import { candidateImageUrls, isUnreliableUrl, localAssetUrl, PLACEHOLDER_IMAGE } from './productImages'

const product = (name, categorySlug) => ({ name, categorySlug })

describe('product image mapping', () => {
  it('maps seeded products to device-specific local assets', () => {
    expect(localAssetUrl(product('Nova Gaming 17 RTX Laptop', 'laptops'))).toBe('/images/products/gaming-laptop.svg')
    expect(localAssetUrl(product('Vertex Zen 14 Business Laptop', 'laptops'))).toBe('/images/products/business-laptop.svg')
    expect(localAssetUrl(product('Aurora Swift 14 Ultrabook', 'laptops'))).toBe('/images/products/ultrabook.svg')
    expect(localAssetUrl(product('Aurora X16 Creator Laptop', 'laptops'))).toBe('/images/products/creator-laptop.svg')
    expect(localAssetUrl(product('Pulse Vantage GPU 16GB', 'gpus'))).toBe('/images/products/graphics-card.svg')
    expect(localAssetUrl(product('Core Titan CPU 16-Core', 'cpus'))).toBe('/images/products/processor.svg')
    expect(localAssetUrl(product('Visual 27 4K Display', 'monitors'))).toBe('/images/products/monitor.svg')
    expect(localAssetUrl(product('Vertex DDR5 32GB Kit', 'ram'))).toBe('/images/products/ram.svg')
    expect(localAssetUrl(product('Flash NVMe 2TB SSD', 'storage'))).toBe('/images/products/ssd.svg')
    expect(localAssetUrl(product('Flux 100W USB-C Charger', 'chargers'))).toBe('/images/products/charger.svg')
    expect(localAssetUrl(product('Flux 65W Laptop Adapter', 'chargers'))).toBe('/images/products/laptop-adapter.svg')
    expect(localAssetUrl(product('Flux 750W Gold PSU', 'chargers'))).toBe('/images/products/psu.svg')
    expect(localAssetUrl(product('Nova Mechanical Keyboard', 'accessories'))).toBe('/images/products/keyboard.svg')
    expect(localAssetUrl(product('Nova Precision Mouse', 'accessories'))).toBe('/images/products/mouse.svg')
    expect(localAssetUrl(product('Helio Air 5G', 'smartphones'))).toBe('/images/products/smartphone.svg')
  })

  it('falls back to a category asset when no keyword matches', () => {
    expect(localAssetUrl(product('Vision 32 Curved 165Hz', 'monitors'))).toBe('/images/products/monitor.svg')
    expect(localAssetUrl(product('Mystery Device', 'laptops'))).toBe('/images/products/laptop.svg')
  })

  it('falls back to the branded placeholder for unknown categories', () => {
    expect(localAssetUrl(product('Mystery Device', 'unknown'))).toBe(PLACEHOLDER_IMAGE)
  })

  it('flags random/placeholder remote URLs as unreliable', () => {
    expect(isUnreliableUrl('https://picsum.photos/seed/x/800/600')).toBe(true)
    expect(isUnreliableUrl('https://placehold.co/800x600')).toBe(true)
    expect(isUnreliableUrl('/images/products/keyboard.svg')).toBe(false)
    expect(isUnreliableUrl(null)).toBe(true)
  })

  it('skips unreliable primary URLs and ends the chain with the placeholder', () => {
    const urls = candidateImageUrls({ ...product('Nova Precision Mouse', 'accessories'), primaryImageUrl: 'https://picsum.photos/seed/x/800/600' })
    expect(urls).not.toContain('https://picsum.photos/seed/x/800/600')
    expect(urls[0]).toBe('/images/products/mouse.svg')
    expect(urls[urls.length - 1]).toBe(PLACEHOLDER_IMAGE)
  })

  it('keeps a valid stored URL as the first candidate', () => {
    const urls = candidateImageUrls({ ...product('Nova Precision Mouse', 'accessories'), primaryImageUrl: '/images/products/mouse.svg' })
    expect(urls[0]).toBe('/images/products/mouse.svg')
    expect(new Set(urls).size).toBe(urls.length)
  })
})
