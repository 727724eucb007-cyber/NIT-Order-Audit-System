import { useEffect, useState } from 'react'
import api from '../api'
import toast from 'react-hot-toast'
import { Pencil, Trash2, Plus, X } from 'lucide-react'

const empty = { name: '', sku: '', unit: 'Bottle', sellingPrice: '', gstRate: '12', active: true }

export default function Products() {
  const [items, setItems] = useState([])
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(empty)
  const [editId, setEditId] = useState(null)

  const load = () => api.get('/products').then(r => setItems(r.data))
  useEffect(() => { load() }, [])

  const openAdd = () => { setForm(empty); setEditId(null); setModal(true) }
  const openEdit = p => { setForm({ ...p }); setEditId(p.id); setModal(true) }

  const save = async e => {
    e.preventDefault()
    try {
      if (editId) await api.put(`/products/${editId}`, form)
      else await api.post('/products', form)
      toast.success('Saved')
      setModal(false)
      load()
    } catch { toast.error('Error saving product') }
  }

  const del = async id => {
    if (!confirm('Delete this product?')) return
    try { await api.delete(`/products/${id}`); toast.success('Deleted'); load() }
    catch { toast.error('Cannot delete') }
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">Products</h2>
        <button onClick={openAdd} className="flex items-center gap-1 bg-blue-600 text-white px-3 py-1.5 rounded-lg text-sm hover:bg-blue-700">
          <Plus size={16} /> Add Product
        </button>
      </div>
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>{['Name','SKU','Unit','Price (₹)','GST %','Status','Actions'].map(h => (
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            ))}</tr>
          </thead>
          <tbody className="divide-y">
            {items.map(p => (
              <tr key={p.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{p.name}</td>
                <td className="px-4 py-3 text-gray-500">{p.sku}</td>
                <td className="px-4 py-3">{p.unit}</td>
                <td className="px-4 py-3">₹{p.sellingPrice}</td>
                <td className="px-4 py-3">{p.gstRate}%</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${p.active ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                    {p.active ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="px-4 py-3 flex gap-2">
                  <button onClick={() => openEdit(p)} className="text-blue-500 hover:text-blue-700"><Pencil size={15} /></button>
                  <button onClick={() => del(p.id)} className="text-red-500 hover:text-red-700"><Trash2 size={15} /></button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {modal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-bold text-lg">{editId ? 'Edit' : 'Add'} Product</h3>
              <button onClick={() => setModal(false)}><X size={20} /></button>
            </div>
            <form onSubmit={save} className="space-y-3">
              {[['name','Name'],['sku','SKU'],['unit','Unit']].map(([k,l]) => (
                <div key={k}>
                  <label className="text-sm font-medium">{l}</label>
                  <input className="w-full border rounded px-3 py-1.5 mt-1 text-sm" value={form[k]}
                    onChange={e => setForm({...form,[k]:e.target.value})} required />
                </div>
              ))}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="text-sm font-medium">Selling Price (₹)</label>
                  <input type="number" step="0.01" className="w-full border rounded px-3 py-1.5 mt-1 text-sm"
                    value={form.sellingPrice} onChange={e => setForm({...form,sellingPrice:e.target.value})} required />
                </div>
                <div>
                  <label className="text-sm font-medium">GST Rate (%)</label>
                  <select className="w-full border rounded px-3 py-1.5 mt-1 text-sm"
                    value={form.gstRate} onChange={e => setForm({...form,gstRate:e.target.value})}>
                    {['0','5','12','18','28'].map(r => <option key={r}>{r}</option>)}
                  </select>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <input type="checkbox" id="active" checked={form.active}
                  onChange={e => setForm({...form,active:e.target.checked})} />
                <label htmlFor="active" className="text-sm">Active</label>
              </div>
              <div className="flex gap-2 pt-2">
                <button type="submit" className="flex-1 bg-blue-600 text-white py-2 rounded-lg text-sm hover:bg-blue-700">Save</button>
                <button type="button" onClick={() => setModal(false)} className="flex-1 border py-2 rounded-lg text-sm hover:bg-gray-50">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
