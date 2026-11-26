import { useEffect, useState } from 'react'
import api from '../api'
import toast from 'react-hot-toast'
import { Pencil, Plus, X, AlertTriangle } from 'lucide-react'

const empty = { name: '', unit: 'kg', currentStock: '', reorderLevel: '', costPerUnit: '' }

export default function RawMaterials() {
  const [items, setItems] = useState([])
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(empty)
  const [editId, setEditId] = useState(null)

  const load = () => api.get('/raw-materials').then(r => setItems(r.data))
  useEffect(() => { load() }, [])

  const openAdd = () => { setForm(empty); setEditId(null); setModal(true) }
  const openEdit = r => { setForm({ ...r }); setEditId(r.id); setModal(true) }

  const save = async e => {
    e.preventDefault()
    try {
      if (editId) await api.put(`/raw-materials/${editId}`, form)
      else await api.post('/raw-materials', form)
      toast.success('Saved'); setModal(false); load()
    } catch { toast.error('Error saving') }
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">Raw Materials</h2>
        <button onClick={openAdd} className="flex items-center gap-1 bg-blue-600 text-white px-3 py-1.5 rounded-lg text-sm hover:bg-blue-700">
          <Plus size={16} /> Add
        </button>
      </div>
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>{['Name','Unit','Stock','Reorder Level','Cost/Unit','Status','Actions'].map(h => (
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            ))}</tr>
          </thead>
          <tbody className="divide-y">
            {items.map(r => {
              const low = parseFloat(r.currentStock) <= parseFloat(r.reorderLevel)
              return (
                <tr key={r.id} className={`hover:bg-gray-50 ${low ? 'bg-red-50' : ''}`}>
                  <td className="px-4 py-3 font-medium">{r.name}</td>
                  <td className="px-4 py-3">{r.unit}</td>
                  <td className="px-4 py-3 font-mono">{r.currentStock}</td>
                  <td className="px-4 py-3 font-mono">{r.reorderLevel}</td>
                  <td className="px-4 py-3">₹{r.costPerUnit}</td>
                  <td className="px-4 py-3">
                    {low ? (
                      <span className="flex items-center gap-1 text-red-600 text-xs font-medium">
                        <AlertTriangle size={13} /> Low Stock
                      </span>
                    ) : <span className="text-green-600 text-xs font-medium">OK</span>}
                  </td>
                  <td className="px-4 py-3">
                    <button onClick={() => openEdit(r)} className="text-blue-500 hover:text-blue-700"><Pencil size={15} /></button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>

      {modal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-bold text-lg">{editId ? 'Edit' : 'Add'} Raw Material</h3>
              <button onClick={() => setModal(false)}><X size={20} /></button>
            </div>
            <form onSubmit={save} className="space-y-3">
              <div>
                <label className="text-sm font-medium">Name</label>
                <input className="w-full border rounded px-3 py-1.5 mt-1 text-sm" value={form.name}
                  onChange={e => setForm({ ...form, name: e.target.value })} required />
              </div>
              <div>
                <label className="text-sm font-medium">Unit</label>
                <input className="w-full border rounded px-3 py-1.5 mt-1 text-sm" value={form.unit}
                  onChange={e => setForm({ ...form, unit: e.target.value })} required />
              </div>
              <div className="grid grid-cols-3 gap-2">
                {[['currentStock','Stock'],['reorderLevel','Reorder Lvl'],['costPerUnit','Cost/Unit']].map(([k,l]) => (
                  <div key={k}>
                    <label className="text-sm font-medium">{l}</label>
                    <input type="number" step="0.001" className="w-full border rounded px-3 py-1.5 mt-1 text-sm"
                      value={form[k]} onChange={e => setForm({ ...form, [k]: e.target.value })} required />
                  </div>
                ))}
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
