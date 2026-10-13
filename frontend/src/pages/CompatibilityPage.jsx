import { useEffect, useState } from 'react'
import { compatibilityApi } from '../api/endpoints'
import { ErrorMessage } from '../components/StateViews'
import { useAuth } from '../context/AuthContext'

const DEFAULT_TYPES = ['CHARGER', 'CONNECTION', 'CPU', 'GPU', 'LAPTOP', 'MONITOR', 'MOTHERBOARD', 'PSU', 'RAM']

function AttributeRows({ attributes, setAttributes }) {
  function update(idx, field, value) {
    const next = attributes.map((a, i) => (i === idx ? { ...a, [field]: value } : a))
    setAttributes(next)
  }
  function add() { setAttributes([...attributes, { key: '', value: '' }]) }
  function remove(idx) { setAttributes(attributes.filter((_, i) => i !== idx)) }

  return (
    <div>
      {attributes.map((a, i) => (
        <div key={i} className="form-row" style={{ gridTemplateColumns: '1fr 1fr auto', marginBottom: 8, alignItems: 'center' }}>
          <input className="form-control" placeholder="attribute (e.g. socket)" value={a.key} onChange={(e) => update(i, 'key', e.target.value)} />
          <input className="form-control" placeholder="value (e.g. AM5)" value={a.value} onChange={(e) => update(i, 'value', e.target.value)} />
          <button type="button" className="btn btn-outline btn-sm" onClick={() => remove(i)} aria-label="Remove attribute">✕</button>
        </div>
      ))}
      <button type="button" className="btn btn-ghost btn-sm" onClick={add}>+ Add attribute</button>
    </div>
  )
}

export default function CompatibilityPage() {
  const { isAuthenticated } = useAuth()
  const [types, setTypes] = useState(DEFAULT_TYPES)
  const [sourceType, setSourceType] = useState('CPU')
  const [targetType, setTargetType] = useState('MOTHERBOARD')
  const [sourceAttributes, setSourceAttributes] = useState([{ key: 'Socket', value: 'AM5' }])
  const [targetAttributes, setTargetAttributes] = useState([{ key: 'Socket', value: 'AM5' }])
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (isAuthenticated) {
      compatibilityApi.types().then((t) => { if (t?.length) setTypes(t) }).catch(() => {})
    }
  }, [isAuthenticated])

  function toMap(rows) {
    return rows.reduce((acc, r) => {
      if (r.key.trim()) acc[r.key.trim()] = r.value
      return acc
    }, {})
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    setResult(null)
    try {
      const data = await compatibilityApi.check({
        sourceType,
        sourceAttributes: toMap(sourceAttributes),
        targetType,
        targetAttributes: toMap(targetAttributes)
      })
      setResult(data)
    } catch (err) { setError(err) } finally { setBusy(false) }
  }

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <h1>Compatibility Lab</h1>
        <p>Check whether two components satisfy our registered compatibility rules.</p>
      </div>

      <div className="cart-layout" style={{ gridTemplateColumns: '1.2fr 1fr', alignItems: 'start' }}>
        <form className="tool-card" onSubmit={handleSubmit}>
          <div className="tool-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
            <div className="form-group">
              <label>Source type</label>
              <input className="form-control" list="compat-types" value={sourceType} onChange={(e) => setSourceType(e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Target type</label>
              <input className="form-control" list="compat-types" value={targetType} onChange={(e) => setTargetType(e.target.value)} required />
            </div>
          </div>
          <datalist id="compat-types">
            {types.map((t) => <option key={t} value={t} />)}
          </datalist>

          <div className="tool-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
            <div>
              <h4>Source attributes</h4>
              <AttributeRows attributes={sourceAttributes} setAttributes={setSourceAttributes} />
            </div>
            <div>
              <h4>Target attributes</h4>
              <AttributeRows attributes={targetAttributes} setAttributes={setTargetAttributes} />
            </div>
          </div>

          <button type="submit" className="btn btn-primary btn-lg" style={{ marginTop: 16 }} disabled={busy}>
            {busy ? 'Checking…' : 'Check compatibility'}
          </button>
        </form>

        <div>
          {error ? <ErrorMessage error={error} /> : null}
          {result ? (
            <div className="card tool-card">
              <h3>Result</h3>
              <div className={`compat-result compat-${result.result}`}>{result.result}</div>
              {result.reason ? <p>{result.reason}</p> : null}
              <div className="kv">
                <div className="kv-item"><span>Rule ID</span><strong>{result.ruleId ?? '—'}</strong></div>
                <div className="kv-item"><span>Rule type</span><strong>{result.ruleType || '—'}</strong></div>
                <div className="kv-item"><span>Verified</span><strong>{result.verified ? 'Yes' : 'No'}</strong></div>
                <div className="kv-item"><span>Pair</span><strong>{result.sourceType} → {result.targetType}</strong></div>
              </div>
            </div>
          ) : (
            <div className="card tool-card">
              <h3>How it works</h3>
              <p className="muted">Enter the component types and their key attributes. ElectroVerse evaluates registered rules and returns COMPATIBLE, INCOMPATIBLE, CONDITIONAL, or UNKNOWN.</p>
              <p className="muted small">Example: a CPU with attribute <em>Socket</em> = AM5 against a motherboard with attribute <em>Socket</em> = AM5 returns COMPATIBLE.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}