import { useEffect, useState } from 'react'
import api from '../api'
import toast from 'react-hot-toast'
import { Plus, X, ToggleLeft, ToggleRight } from 'lucide-react'

const empty = { username: '', password: '', role: 'SALESMAN', fullName: '' }

export default function Users() {
  const [users, setUsers] = useState([])
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState(empty)

  const load = () => api.get('/users').then(r => setUsers(r.data))
  useEffect(() => { load() }, [])

  const create = async e => {
    e.preventDefault()
    try { await api.post('/users', form); toast.success('User created'); setModal(false); load() }
    catch { toast.error('Error creating user') }
  }

  const toggle = async id => {
    try { await api.put(`/users/${id}/toggle`); load() }
    catch { toast.error('Error') }
  }

  const roleColor = r => r === 'ADMIN' ? 'bg-purple-100 text-purple-700' : r === 'SALESMAN' ? 'bg-blue-100 text-blue-700' : 'bg-orange-100 text-orange-700'

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">User Management</h2>
        <button onClick={() => { setForm(empty); setModal(true) }}
          className="flex items-center gap-1 bg-blue-600 text-white px-3 py-1.5 rounded-lg text-sm hover:bg-blue-700">
          <Plus size={16} /> Add User
        </button>
      </div>
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>{['Full Name','Username','Role','Status','Actions'].map(h => (
              <th key={h} className="px-4 py-3 text-left">{h}</th>
            ))}</tr>
          </thead>
          <tbody className="divide-y">
            {users.map(u => (
              <tr key={u.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 font-medium">{u.fullName}</td>
                <td className="px-4 py-3 font-mono text-gray-500">{u.username}</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${roleColor(u.role)}`}>{u.role}</span>
                </td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${u.active ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                    {u.active ? 'Active' : 'Inactive'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <button onClick={() => toggle(u.id)} className="text-gray-500 hover:text-blue-600" title="Toggle active">
                    {u.active ? <ToggleRight size={20} className="text-green-500" /> : <ToggleLeft size={20} />}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {modal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-sm">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-bold text-lg">Add User</h3>
              <button onClick={() => setModal(false)}><X size={20} /></button>
            </div>
            <form onSubmit={create} className="space-y-3">
              {[['fullName','Full Name'],['username','Username'],['password','Password']].map(([k,l]) => (
                <div key={k}>
                  <label className="text-sm font-medium">{l}</label>
                  <input type={k === 'password' ? 'password' : 'text'}
                    className="w-full border rounded px-3 py-1.5 mt-1 text-sm"
                    value={form[k]} onChange={e => setForm({ ...form, [k]: e.target.value })} required />
                </div>
              ))}
              <div>
                <label className="text-sm font-medium">Role</label>
                <select className="w-full border rounded px-3 py-2 mt-1 text-sm"
                  value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}>
                  <option value="ADMIN">Admin</option>
                  <option value="SALESMAN">Salesman</option>
                  <option value="PRODUCTION">Production</option>
                </select>
              </div>
              <div className="flex gap-2 pt-2">
                <button type="submit" className="flex-1 bg-blue-600 text-white py-2 rounded-lg text-sm hover:bg-blue-700">Create</button>
                <button type="button" onClick={() => setModal(false)} className="flex-1 border py-2 rounded-lg text-sm">Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
