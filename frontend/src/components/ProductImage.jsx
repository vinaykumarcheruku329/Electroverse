import { useEffect, useMemo, useState } from 'react'
import { candidateImageUrls } from '../utils/productImages'

export function ProductImage({ product, url, alt, className, loading = 'lazy' }) {
  const candidates = useMemo(() => candidateImageUrls(product, url), [product, url])
  const [index, setIndex] = useState(0)

  useEffect(() => { setIndex(0) }, [candidates])

  const label = alt || product?.name || 'Product image'
  const exhausted = index >= candidates.length

  if (exhausted) {
    return (
      <div className="product-media-fallback" role="img" aria-label={label}>
        <span>{product?.name?.charAt(0) || '?'}</span>
      </div>
    )
  }

  return (
    <img
      src={candidates[index]}
      alt={label}
      className={className}
      loading={loading}
      onError={() => setIndex((i) => i + 1)}
    />
  )
}
