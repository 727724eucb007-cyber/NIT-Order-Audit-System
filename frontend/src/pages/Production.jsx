import { useEffect, useState } from 'react'
import api from '../api'
import toast from 'react-hot-toast'
import { Plus, X, ChevronDown, ChevronUp } from 'lucide-react'

const statusColors = {
  PLANNED: 'bg-gray-100 text-gray-700',
  IN_PROGRESS: 'bg-yellow-100 text-yellow-700',
  COMPLETED: 'bg-green-100 text-green-700',
}

export default function Production() {
  const [orders, setOrders] = useState([])
  const [products, setProducts] = useState([])
  const [modal, setModal] = useState(false)
  const [completeModal, setCompleteModal] = useState(null)
  const [form, setForm] = useState({ productId: '', plannedQty: '', notes: '' })
  const [completeForm, setCompleteForm] = useState({ actualQty: '', consumptions: [] })
  const [expanded, setExpanded] = useState(null)

  const load = () => api.get('/production')
    .then(r => setOrders(Array.isArray(r.data) ? r.data : []))
    .catch(() => setOrders([]))
  useEffect(() => {
    load()
    api.get('/products').then(r => setProducts(r.data))
  }, [])

  const create = async e => {
    e.preventDefault()
    try {
      await api.post('/production', form)
      toast.success('Production order created'); setModal(false); load()
    } catch { toast.error('Error creating order') }
  }

  const start = async id => {
    try { await api.put(`/production/${id}/start`); toast.success('Started'); load() }
    catch (err) { toast.error(err.response?.data?.message || 'Error') }
  }

  const openComplete = async order => {
    const r = await api.get(`/production/${order.id}`)
    const cons = r.data.consumptions.map(c => ({
      rawMaterialId: c.rawMaterial.id,
      name: c.rawMaterial.name,
      unit: c.rawMaterial.unit,
      plannedQty: c.plannedQty,
      actualQty: c.plannedQty,
    }))
    setCompleteForm({ actualQty: order.plannedQty, consumptions: cons })
    setCompleteModal(order)
  }

  const complete = async e => {
    e.preventDefault()
    try {
      await api.put(`/production/${completeModal.id}/complete`, {
        actualQty: parseFloat(completeForm.actualQty),
        consumptions: completeForm.consumptions.map(c => ({
          rawMaterialId: c.rawMaterialId,
          actualQty: parseFloat(c.actualQty)
        }))
      })
      toast.success('Production completed'); setCompleteModal(null); load()
    } catch (err) { toast.error(err.response?.data?.message || 'Error completing') }
  }

  const updateCons = (i, val) => {
    const updated = [...completeForm.consumptions]
    updated[i] = { ...updated[i], actualQty: val }
    setCompleteForm({ ...completeForm, consumptions: updated })
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">Production Orders</h2>
        <button onClick={() => setModal(true)} className="flex items-center gap-1 bg-blue-600 text-white px-3 py-1.5 rounded-lg text-sm hover:bg-blue-700">
          <Plus size={16} /> New Order
        </button>
      </div>

      <div className="space-y-3">
        {orders.map(o => (
          <div key={o.id} className="bg-white rounded-xl shadow">
            <div className="flex items-center justify-between px-5 py-4 cursor-pointer"
              onClick={() => setExpanded(expanded === o.id ? null : o.id)}>
              <div className="flex items-center gap-3">
                <span className="font-mono text-xs text-gray-400">#{o.id}</span>
                <span className="font-medium">{o.product?.name}</span>
                <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusColors[o.status]}`}>{o.status}</span>
              </div>
              <div className="flex items-center gap-4 text-sm text-gray-500">
                <span>Planned: <b>{o.plannedQty}</b></span>
                {o.actualQty && <span>Actual: <b>{o.actualQty}</b></span>}
                {expanded === o.id ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
              </div>
            </div>

            {expanded === o.id && (
              <div className="border-t px-5 py-4">
                <div className="text-sm text-gray-500 mb-3">
                  Created by: {o.createdBy?.fullName} | {new Date(o.createdAt).toLocaleDateString('en-IN')}
                  {o.notes && <span> | {o.notes}</span>}
                </div>
                {o.consumptions?.length > 0 && (
                  <table className="w-full text-sm mb-3">
                    <thead className="bg-gray-50 text-xs uppercase text-gray-500">
                      <tr>
                        <th className="px-3 py-2 text-left">Raw Material</th>
                        <th className="px-3 py-2 text-right">Planned</th>
                        <th className="px-3 py-2 text-right">Actual</th>
                        <th className="px-3 py-2 text-right">Variance</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y">
                      {o.consumptions.map(c => {
                        const variance = c.actualQty ? (parseFloat(c.actualQty) - parseFloat(c.plannedQty)).toFixed(4) : '-'
                        return (
                          <tr key={c.id}>
                            <td className="px-3 py-2">{c.rawMaterial?.name}</td>
                            <td className="px-3 py-2 text-right font-mono">{c.plannedQty}</td>
                            <td className="px-3 py-2 text-right font-mono">{c.actualQty || '-'}</td>
                            <td className={`px-3 py-2 text-right font-mono ${parseFloat(variance) > 0 ? 'text-red-600' : 'text-green-600'}`}>{variance}</td>
                          </tr>
                        )
                      })}
                    </tbody>
                  </table>
                )}
                <div className="flex gap-2">
                  {o.status === 'PLANNED' && (
                    <button onClick={() => start(o.id)} className="text-sm bg-yellow-500 text-white px-3 py-1.5 rounded-lg hover:bg-yellow-600">
                      Start Production
                    </button>
                  )}
                  {(o.status === 'PLANNED' || o.status === 'IN_PROGRESS') && (
                    <button onClick={() => openComplete(o)} className="text-sm bg-green-600 text-white px-3 py-1.5 rounded-lg hover:bg-green-700">
                      Complete & Record
                    </button>
                  )}
                </div>
              </div>
            )}
          </div>
        ))}
      </div>

      {modal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-bold text-lg">New Production Order</h3>
              <button onClick={() => setModal(false)}><X size={20} /></button>
            </div>
            <form onSubmit={create} className="space-y-3">
              <div>
                <label className="text-sm font-medium">Product</label>
                <select className="w-full border rounded px-3 py-2 mt-1 text-sm"
                  value={form.productId} onChange={e => setForm({ ...form, productId: e.target.value })} required>
                  <option value="">-- Select --</option>
                  {products.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
              </div>
              <div>
                <label className="text-sm font-medium">Planned Quantity</label>
                <input type="number" step="0.001" className="w-full border rounded px-3 py-2 mt-1 text-sm"
                  value={form.plannedQty} onChange={e => setForm({ ...form, plannedQty: e.target.value })} required />
              </div>
              <div>
                <label className="text-sm font-medium">Notes</label>
                <input className="w-full border rounded px-3 py-2 mt-1 text-sm"
                  value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} />
              </div>
              <div className="flex gap-2 pt-2">
                <button type="submit" className="flex-1 bg-blue-600 text-white py-2 rounded-lg text-sm hover:bg-blue-700">Create</button>
                <button type="button" onClick={() => setModal(false)} className="flex-1 border py-2 rounded-lg text-sm">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {completeModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-lg max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-bold text-lg">Complete Production #{completeModal.id}</h3>
              <button onClick={() => setCompleteModal(null)}><X size={20} /></button>
            </div>
            <form onSubmit={complete} className="space-y-4">
              <div>
                <label className="text-sm font-medium">Actual Quantity Produced</label>
                <input type="number" step="0.001" className="w-full border rounded px-3 py-2 mt-1 text-sm"
                  value={completeForm.actualQty} onChange={e => setCompleteForm({ ...completeForm, actualQty: e.target.value })} required />
              </div>
              <div>
                <p className="text-sm font-medium mb-2">Actual Raw Material Consumption</p>
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 text-xs uppercase text-gray-500">
                    <tr>
                      <th className="px-3 py-2 text-left">Material</th>
                      <th className="px-3 py-2 text-right">Planned</th>
                      <th className="px-3 py-2 text-right">Actual</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y">
                    {completeForm.consumptions.map((c, i) => (
                      <tr key={i}>
                        <td className="px-3 py-2">{c.name} ({c.unit})</td>
                        <td className="px-3 py-2 text-right font-mono text-gray-500">{c.plannedQty}</td>
                        <td className="px-3 py-2">
                          <input type="number" step="0.0001" className="w-full border rounded px-2 py-1 text-sm text-right"
                            value={c.actualQty} onChange={e => updateCons(i, e.target.value)} required />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="flex gap-2 pt-2">
                <button type="submit" className="flex-1 bg-green-600 text-white py-2 rounded-lg text-sm hover:bg-green-700">Complete & Deduct Stock</button>
                <button type="button" onClick={() => setCompleteModal(null)} className="flex-1 border py-2 rounded-lg text-sm">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
