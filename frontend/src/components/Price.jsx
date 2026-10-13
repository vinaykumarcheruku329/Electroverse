import { formatCurrency, formatINR } from '../utils/format'

export function Price({ value, compareAt, currency = 'INR', large = false }) {
  const onSale = compareAt != null && Number(compareAt) > Number(value)
  const isInr = !currency || currency.toUpperCase() === 'INR'
  const format = isInr ? formatINR : (amount) => formatCurrency(amount, currency)
  return (
    <span className={large ? 'price price-lg' : 'price'}>
      <span className="price-current">{format(value)}</span>
      {onSale ? <span className="price-compare">{format(compareAt)}</span> : null}
    </span>
  )
}
