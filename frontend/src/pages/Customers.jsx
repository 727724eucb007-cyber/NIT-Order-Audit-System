import { useEffect, useState } from 'react'
import api from '../api'
import toast from 'react-hot-toast'
import { Pencil, Trash2, Plus, X } from 'lucide-react'

const empty = { name: '', gstin: '', address: '', phone: '', email: '' }

export default function Customers() {
  const [items, setItems] = useState([])
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(empty)
  const [editId, setEditId] = useState(null)

  const load = () => api.get('/customers').then(r => setItems(r.data))
  useEffect(() => { load() }, [])

  const openAdd = () => { setForm(empty); setEditId(null); setModal(true) }
  const openEdit = c => { setForm({ ...c }); setEditId(c.id); setModal(true) }

  const save = async e => {
    e.preventDefault()
    try {
      if (editId) await api.put(`/customers/${editId}`, form)
      else await api.post('/customers', form)
      toast.success('Saved'); setModal(false); load()
    } catch { toast.error('Error saving') }
  }

  const del = async id => {
    if (!confirm('Delete?')) return
    try { await api.delete(`/customers/${id}`); toast.success('Deleted'); load() }
    catch { toast.error('Cannot delete') }
  }

  const fields = [['name','Name *'],['gstin','GSTIN'],['address','Address'],['phone','Phone'],['email','Email']]

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">Customers</h2>
        <button onClick={openAdd} className="flex items-center gap-1 bg-blue-600 text-white px-3 py-1.5 rounded-lg text-sm hover:bg-blue-700">
          <Plus size={16} /> Add Customer
        </button>
      </div>
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>{['Name','GSTIN','Phone','Email','Actions'].map(h => (
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            ))}</tr>
          </thead>
          <tbody className="divide-y">
            {items.map(c => (
              <tr key={c.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{c.name}</td>
                <td className="px-4 py-3 text-gray-500 font-mono text-xs">{c.gstin}</td>
                <td className="px-4 py-3">{c.phone}</td>
                <td className="px-4 py-3">{c.email}</td>
                <td className="px-4 py-3 flex gap-2">
                  <button onClick={() => openEdit(c)} className="text-blue-500 hover:text-blue-700"><Pencil size={15} /></button>
                  <button onClick={() => del(c.id)} className="text-red-500 hover:text-red-700"><Trash2 size={15} /></button>
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
              <h3 className="font-bold text-lg">{editId ? 'Edit' : 'Add'} Customer</h3>
              <button onClick={() => setModal(false)}><X size={20} /></button>
            </div>
            <form onSubmit={save} className="space-y-3">
              {fields.map(([k, l]) => (
                <div key={k}>
                  <label className="text-sm font-medium">{l}</label>
                  <input className="w-full border rounded px-3 py-1.5 mt-1 text-sm"
                    value={form[k] || ''} onChange={e => setForm({ ...form, [k]: e.target.value })}
                    required={k === 'name'} />
                </div>
              ))}
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
