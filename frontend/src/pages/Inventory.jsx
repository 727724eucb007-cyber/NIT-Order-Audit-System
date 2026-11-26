import { useEffect, useState } from 'react'
import api from '../api'
import toast from 'react-hot-toast'
import { Plus, X } from 'lucide-react'

export default function Inventory() {
  const [tab, setTab] = useState('raw')

  // --- Raw Materials state (unchanged) ---
  const [ledger, setLedger] = useState([])
  const [rawMaterials, setRawMaterials] = useState([])
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState({ rawMaterialId: '', txnType: 'PURCHASE', qty: '', reference: '' })
  const [filterRm, setFilterRm] = useState('')

  // --- Finished Goods state ---
  const [fgLedger, setFgLedger] = useState([])
  const [fgProducts, setFgProducts] = useState([])
  const [filterFg, setFilterFg] = useState('')

  const loadRaw = () => {
    const url = filterRm ? `/inventory/ledger/${filterRm}` : '/inventory/ledger'
    api.get(url).then(r => setLedger(r.data))
  }

  const loadFg = () => {
    const url = filterFg ? `/finished-goods/ledger/${filterFg}` : '/finished-goods/ledger'
    api.get(url).then(r => setFgLedger(Array.isArray(r.data) ? r.data : []))
    api.get('/finished-goods/stock').then(r => setFgProducts(Array.isArray(r.data) ? r.data : []))
  }

  useEffect(() => {
    api.get('/raw-materials').then(r => setRawMaterials(r.data))
  }, [])

  useEffect(() => { loadRaw() }, [filterRm])
  useEffect(() => { loadFg() }, [filterFg])

  const save = async e => {
    e.preventDefault()
    try {
      const endpoint = form.txnType === 'PURCHASE' ? '/inventory/purchase' : '/inventory/adjustment'
      await api.post(endpoint, { ...form, qty: parseFloat(form.qty), rawMaterialId: parseInt(form.rawMaterialId) })
      toast.success('Transaction recorded'); setModal(false); loadRaw()
    } catch { toast.error('Error') }
  }

  const txnColor = t => t === 'PURCHASE' || t === 'OPENING' || t === 'PRODUCED' ? 'text-green-600'
    : t === 'CONSUMED' || t === 'DISPATCHED' ? 'text-red-600' : 'text-yellow-600'

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">Inventory</h2>
        {tab === 'raw' && (
          <button onClick={() => setModal(true)} className="flex items-center gap-1 bg-blue-600 text-white px-3 py-1.5 rounded-lg text-sm hover:bg-blue-700">
            <Plus size={16} /> Add Transaction
          </button>
        )}
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-4 border-b">
        <button onClick={() => setTab('raw')}
          className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${tab === 'raw' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}>
          Raw Materials
        </button>
        <button onClick={() => setTab('fg')}
          className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${tab === 'fg' ? 'border-purple-600 text-purple-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}>
          Finished Goods
        </button>
      </div>

      {/* Raw Materials Tab */}
      {tab === 'raw' && (
        <>
          <div className="mb-3">
            <select className="border rounded px-3 py-2 text-sm"
              value={filterRm} onChange={e => setFilterRm(e.target.value)}>
              <option value="">All Raw Materials</option>
              {rawMaterials.map(r => <option key={r.id} value={r.id}>{r.name}</option>)}
            </select>
          </div>
          <div className="bg-white rounded-xl shadow overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
                <tr>{['Date', 'Raw Material', 'Type', 'Qty', 'Balance After', 'Reference'].map(h => (
                  <th key={h} className="px-4 py-3 text-left">{h}</th>
                ))}</tr>
              </thead>
              <tbody className="divide-y">
                {ledger.map(l => (
                  <tr key={l.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-gray-500 text-xs">{new Date(l.txnDate).toLocaleString('en-IN')}</td>
                    <td className="px-4 py-3 font-medium">{l.rawMaterial?.name}</td>
                    <td className={`px-4 py-3 font-medium ${txnColor(l.txnType)}`}>{l.txnType}</td>
                    <td className={`px-4 py-3 font-mono ${parseFloat(l.qty) < 0 ? 'text-red-600' : 'text-green-600'}`}>
                      {parseFloat(l.qty) > 0 ? '+' : ''}{l.qty}
                    </td>
                    <td className="px-4 py-3 font-mono">{l.balanceAfter}</td>
                    <td className="px-4 py-3 text-gray-500">{l.reference}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {/* Finished Goods Tab */}
      {tab === 'fg' && (
        <>
          {/* Stock summary cards */}
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 mb-5">
            {fgProducts.map(p => (
              <div key={p.id} className="bg-white rounded-xl shadow p-4 border-l-4 border-purple-500">
                <p className="text-xs text-gray-500 truncate">{p.name}</p>
                <p className="text-2xl font-bold mt-1">{parseFloat(p.finishedStock ?? 0).toFixed(0)}</p>
                <p className="text-xs text-gray-400">{p.unit}</p>
              </div>
            ))}
          </div>

          {/* Ledger filter */}
          <div className="mb-3">
            <select className="border rounded px-3 py-2 text-sm"
              value={filterFg} onChange={e => setFilterFg(e.target.value)}>
              <option value="">All Products</option>
              {fgProducts.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
            </select>
          </div>

          <div className="bg-white rounded-xl shadow overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
                <tr>{['Date', 'Product', 'Type', 'Qty', 'Balance After', 'Reference'].map(h => (
                  <th key={h} className="px-4 py-3 text-left">{h}</th>
                ))}</tr>
              </thead>
              <tbody className="divide-y">
                {fgLedger.length === 0 && (
                  <tr><td colSpan={6} className="px-4 py-8 text-center text-gray-400 text-sm">No finished-goods transactions yet.</td></tr>
                )}
                {fgLedger.map(l => (
                  <tr key={l.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-gray-500 text-xs">{new Date(l.txnDate).toLocaleString('en-IN')}</td>
                    <td className="px-4 py-3 font-medium">{l.product?.name}</td>
                    <td className={`px-4 py-3 font-medium ${txnColor(l.txnType)}`}>{l.txnType}</td>
                    <td className={`px-4 py-3 font-mono ${parseFloat(l.qty) < 0 ? 'text-red-600' : 'text-green-600'}`}>
                      {parseFloat(l.qty) > 0 ? '+' : ''}{l.qty}
                    </td>
                    <td className="px-4 py-3 font-mono">{l.balanceAfter}</td>
                    <td className="px-4 py-3 text-gray-500">{l.reference}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {/* Add Raw Material Transaction Modal (unchanged) */}
      {modal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-bold text-lg">Add Transaction</h3>
              <button onClick={() => setModal(false)}><X size={20} /></button>
            </div>
            <form onSubmit={save} className="space-y-3">
              <div>
                <label className="text-sm font-medium">Raw Material</label>
                <select className="w-full border rounded px-3 py-2 mt-1 text-sm"
                  value={form.rawMaterialId} onChange={e => setForm({ ...form, rawMaterialId: e.target.value })} required>
                  <option value="">-- Select --</option>
                  {rawMaterials.map(r => <option key={r.id} value={r.id}>{r.name} (Stock: {r.currentStock} {r.unit})</option>)}
                </select>
              </div>
              <div>
                <label className="text-sm font-medium">Transaction Type</label>
                <select className="w-full border rounded px-3 py-2 mt-1 text-sm"
                  value={form.txnType} onChange={e => setForm({ ...form, txnType: e.target.value })}>
                  <option value="PURCHASE">Purchase (Add Stock)</option>
                  <option value="ADJUSTMENT">Adjustment</option>
                </select>
              </div>
              <div>
                <label className="text-sm font-medium">Quantity {form.txnType === 'ADJUSTMENT' ? '(use negative to reduce)' : ''}</label>
                <input type="number" step="0.001" className="w-full border rounded px-3 py-2 mt-1 text-sm"
                  value={form.qty} onChange={e => setForm({ ...form, qty: e.target.value })} required />
              </div>
              <div>
                <label className="text-sm font-medium">Reference</label>
                <input className="w-full border rounded px-3 py-2 mt-1 text-sm"
                  value={form.reference} onChange={e => setForm({ ...form, reference: e.target.value })}
                  placeholder="e.g. PO-2024-001" />
              </div>
              <div className="flex gap-2 pt-2">
                <button type="submit" className="flex-1 bg-blue-600 text-white py-2 rounded-lg text-sm hover:bg-blue-700">Save</button>
                <button type="button" onClick={() => setModal(false)} className="flex-1 border py-2 rounded-lg text-sm">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
