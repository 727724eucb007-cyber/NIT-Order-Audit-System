import { useEffect, useState } from 'react'
import api from '../api'
import toast from 'react-hot-toast'
import { Plus, X, ChevronDown, ChevronUp, Trash2 } from 'lucide-react'
import { useAuth } from '../AuthContext'

const statusColors = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
  INVOICED: 'bg-blue-100 text-blue-700',
  DISPATCHED: 'bg-purple-100 text-purple-700',
}

export default function SalesOrders() {
  const { user } = useAuth()
  const [orders, setOrders] = useState([])
  const [customers, setCustomers] = useState([])
  const [products, setProducts] = useState([])
  const [modal, setModal] = useState(false)
  const [expanded, setExpanded] = useState(null)
  const [rejectModal, setRejectModal] = useState(null)
  const [rejectReason, setRejectReason] = useState('')
  const [form, setForm] = useState({ customerId: '', notes: '', items: [{ productId: '', qty: '', unitPrice: '', gstRate: '' }] })

  const [error, setError] = useState(null)
  const [shortageWarning, setShortageWarning] = useState(null) // { orderId, shortages[] }

  const load = () => {
    setError(null)
    api.get('/sales-orders')
      .then(r => setOrders(Array.isArray(r.data) ? r.data : []))
      .catch(() => { setOrders([]); setError('Failed to load orders. Please refresh.') })
  }
  useEffect(() => {
    load()
    api.get('/customers').then(r => setCustomers(r.data))
    api.get('/products').then(r => setProducts(r.data))
  }, [])

  const addItem = () => setForm({ ...form, items: [...form.items, { productId: '', qty: '', unitPrice: '', gstRate: '' }] })
  const removeItem = i => setForm({ ...form, items: form.items.filter((_, idx) => idx !== i) })

  const updateItem = (i, field, val) => {
    const items = [...form.items]
    items[i] = { ...items[i], [field]: val }
    if (field === 'productId') {
      const p = products.find(p => p.id === parseInt(val))
      if (p) { items[i].unitPrice = p.sellingPrice; items[i].gstRate = p.gstRate }
    }
    setForm({ ...form, items })
  }

  const calcTotal = () => form.items.reduce((sum, it) => {
    const base = (parseFloat(it.qty) || 0) * (parseFloat(it.unitPrice) || 0)
    const gst = base * (parseFloat(it.gstRate) || 0) / 100
    return sum + base + gst
  }, 0)

  const create = async e => {
    e.preventDefault()
    try {
      await api.post('/sales-orders', {
        customerId: parseInt(form.customerId),
        notes: form.notes,
        items: form.items.map(it => ({
          productId: parseInt(it.productId),
          qty: parseFloat(it.qty),
          unitPrice: parseFloat(it.unitPrice),
          gstRate: parseFloat(it.gstRate)
        }))
      })
      toast.success('Order created'); setModal(false); load()
    } catch { toast.error('Error creating order') }
  }

  const approve = async id => {
    setShortageWarning(null)
    try { await api.put(`/sales-orders/${id}/approve`); toast.success('Approved'); load() }
    catch (err) {
      const data = err.response?.data
      if (err.response?.status === 409 && data?.shortages) {
        setShortageWarning({ orderId: id, shortages: data.shortages })
        setExpanded(id)
      } else {
        toast.error('Error approving order')
      }
    }
  }

  const reject = async () => {
    try { await api.put(`/sales-orders/${rejectModal}/reject`, { reason: rejectReason }); toast.success('Rejected'); setRejectModal(null); load() }
    catch { toast.error('Error') }
  }

  const generateInvoice = async id => {
    try { await api.post(`/sales-orders/${id}/invoice`); toast.success('Invoice generated'); load() }
    catch (err) { toast.error(err.response?.data?.message || 'Error') }
  }

  const dispatch = async id => {
    try { await api.put(`/sales-orders/${id}/dispatch`); toast.success('Dispatched'); load() }
    catch (err) { toast.error(err.response?.data?.message || err.response?.data || 'Dispatch failed') }
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">Sales Orders</h2>
        {(user.role === 'SALESMAN' || user.role === 'ADMIN') && (
          <button onClick={() => setModal(true)} className="flex items-center gap-1 bg-blue-600 text-white px-3 py-1.5 rounded-lg text-sm hover:bg-blue-700">
            <Plus size={16} /> New Order
          </button>
        )}
      </div>

      <div className="space-y-3">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">{error}</div>
        )}
        {!error && orders.length === 0 && (
          <div className="bg-white rounded-xl shadow px-5 py-8 text-center text-gray-400 text-sm">No orders yet.</div>
        )}
        {orders.map(o => (
          <div key={o.id} className="bg-white rounded-xl shadow">
            <div className="flex items-center justify-between px-5 py-4 cursor-pointer"
              onClick={() => setExpanded(expanded === o.id ? null : o.id)}>
              <div className="flex items-center gap-3">
                <span className="font-mono text-xs text-gray-400">#{o.id}</span>
                <span className="font-medium">{o.customer?.name}</span>
                <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${statusColors[o.status]}`}>{o.status}</span>
              </div>
              <div className="flex items-center gap-4 text-sm text-gray-500">
                <span className="font-semibold text-gray-800">₹{parseFloat(o.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                <span>{new Date(o.createdAt).toLocaleDateString('en-IN')}</span>
                {expanded === o.id ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
              </div>
            </div>

            {expanded === o.id && (
              <div className="border-t px-5 py-4">
                <p className="text-sm text-gray-500 mb-3">Salesman: {o.salesman?.fullName} {o.notes && `| ${o.notes}`}</p>
                {shortageWarning?.orderId === o.id && (
                  <div className="mb-3 rounded-lg border border-orange-300 bg-orange-50 p-3">
                    <p className="text-sm font-semibold text-orange-700 mb-2">⚠ Insufficient stock — cannot approve</p>
                    <table className="w-full text-xs">
                      <thead>
                        <tr className="text-orange-600 uppercase">
                          <th className="text-left py-1 pr-4">Product</th>
                          <th className="text-right py-1 pr-4">Required</th>
                          <th className="text-right py-1 pr-4">Available</th>
                          <th className="text-right py-1">Shortage</th>
                        </tr>
                      </thead>
                      <tbody>
                        {shortageWarning.shortages.map((s, i) => (
                          <tr key={i} className="border-t border-orange-200">
                            <td className="py-1 pr-4 text-gray-700">{s.productName}</td>
                            <td className="py-1 pr-4 text-right text-gray-700">{s.required}</td>
                            <td className="py-1 pr-4 text-right text-gray-700">{s.available}</td>
                            <td className="py-1 text-right font-bold text-red-600">{s.shortage}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
                <table className="w-full text-sm mb-3">
                  <thead className="bg-gray-50 text-xs uppercase text-gray-500">
                    <tr>
                      <th className="px-3 py-2 text-left">Product</th>
                      <th className="px-3 py-2 text-right">Qty</th>
                      <th className="px-3 py-2 text-right">Unit Price</th>
                      <th className="px-3 py-2 text-right">GST%</th>
                      <th className="px-3 py-2 text-right">Line Total</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y">
                    {o.items?.map(it => {
                      const base = parseFloat(it.qty) * parseFloat(it.unitPrice)
                      const gst = base * parseFloat(it.gstRate) / 100
                      return (
                        <tr key={it.id}>
                          <td className="px-3 py-2">{it.product?.name}</td>
                          <td className="px-3 py-2 text-right">{it.qty}</td>
                          <td className="px-3 py-2 text-right">₹{it.unitPrice}</td>
                          <td className="px-3 py-2 text-right">{it.gstRate}%</td>
                          <td className="px-3 py-2 text-right font-medium">₹{(base + gst).toFixed(2)}</td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
                {user.role === 'ADMIN' && (
                  <div className="flex gap-2">
                    {o.status === 'PENDING' && <>
                      <button onClick={() => approve(o.id)} className="text-sm bg-green-600 text-white px-3 py-1.5 rounded-lg hover:bg-green-700">Approve</button>
                      <button onClick={() => { setRejectModal(o.id); setRejectReason('') }} className="text-sm bg-red-500 text-white px-3 py-1.5 rounded-lg hover:bg-red-600">Reject</button>
                    </>}
                    {o.status === 'APPROVED' && (
                      <button onClick={() => generateInvoice(o.id)} className="text-sm bg-blue-600 text-white px-3 py-1.5 rounded-lg hover:bg-blue-700">Generate Invoice</button>
                    )}
                    {o.status === 'INVOICED' && (
                      <button onClick={() => dispatch(o.id)} className="text-sm bg-purple-600 text-white px-3 py-1.5 rounded-lg hover:bg-purple-700">Dispatch</button>
                    )}
                  </div>
                )}
              </div>
            )}
          </div>
        ))}
      </div>

      {modal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-bold text-lg">New Sales Order</h3>
              <button onClick={() => setModal(false)}><X size={20} /></button>
            </div>
            <form onSubmit={create} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-sm font-medium">Customer</label>
                  <select className="w-full border rounded px-3 py-2 mt-1 text-sm"
                    value={form.customerId} onChange={e => setForm({ ...form, customerId: e.target.value })} required>
                    <option value="">-- Select --</option>
                    {customers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="text-sm font-medium">Notes</label>
                  <input className="w-full border rounded px-3 py-2 mt-1 text-sm"
                    value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} />
                </div>
              </div>

              <div>
                <div className="flex justify-between items-center mb-2">
                  <p className="text-sm font-medium">Order Items</p>
                  <button type="button" onClick={addItem} className="text-xs text-blue-600 hover:underline flex items-center gap-1">
                    <Plus size={13} /> Add Item
                  </button>
                </div>
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 text-xs uppercase text-gray-500">
                    <tr>
                      <th className="px-2 py-2 text-left">Product</th>
                      <th className="px-2 py-2">Qty</th>
                      <th className="px-2 py-2">Price</th>
                      <th className="px-2 py-2">GST%</th>
                      <th></th>
                    </tr>
                  </thead>
                  <tbody className="divide-y">
                    {form.items.map((it, i) => (
                      <tr key={i}>
                        <td className="px-2 py-2">
                          <select className="w-full border rounded px-2 py-1 text-sm"
                            value={it.productId} onChange={e => updateItem(i, 'productId', e.target.value)} required>
                            <option value="">--</option>
                            {products.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                          </select>
                        </td>
                        <td className="px-2 py-2">
                          <input type="number" step="0.001" className="w-20 border rounded px-2 py-1 text-sm"
                            value={it.qty} onChange={e => updateItem(i, 'qty', e.target.value)} required />
                        </td>
                        <td className="px-2 py-2">
                          <input type="number" step="0.01" className="w-24 border rounded px-2 py-1 text-sm"
                            value={it.unitPrice} onChange={e => updateItem(i, 'unitPrice', e.target.value)} required />
                        </td>
                        <td className="px-2 py-2">
                          <input type="number" step="0.01" className="w-16 border rounded px-2 py-1 text-sm"
                            value={it.gstRate} onChange={e => updateItem(i, 'gstRate', e.target.value)} required />
                        </td>
                        <td className="px-2 py-2">
                          <button type="button" onClick={() => removeItem(i)} className="text-red-500"><Trash2 size={14} /></button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <p className="text-right text-sm font-semibold mt-2">
                  Total (incl. GST): ₹{calcTotal().toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                </p>
              </div>

              <div className="flex gap-2 pt-2">
                <button type="submit" className="flex-1 bg-blue-600 text-white py-2 rounded-lg text-sm hover:bg-blue-700">Create Order</button>
                <button type="button" onClick={() => setModal(false)} className="flex-1 border py-2 rounded-lg text-sm">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {rejectModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-sm">
            <h3 className="font-bold text-lg mb-3">Reject Order #{rejectModal}</h3>
            <textarea className="w-full border rounded px-3 py-2 text-sm" rows={3}
              placeholder="Reason for rejection..." value={rejectReason}
              onChange={e => setRejectReason(e.target.value)} />
            <div className="flex gap-2 mt-3">
              <button onClick={reject} className="flex-1 bg-red-500 text-white py-2 rounded-lg text-sm hover:bg-red-600">Reject</button>
              <button onClick={() => setRejectModal(null)} className="flex-1 border py-2 rounded-lg text-sm">Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
