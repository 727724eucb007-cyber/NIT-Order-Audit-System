import { useEffect, useState } from 'react'
import api from '../api'
import toast from 'react-hot-toast'
import { Plus, Trash2, Save } from 'lucide-react'

export default function BOM() {
  const [products, setProducts] = useState([])
  const [rawMaterials, setRawMaterials] = useState([])
  const [selectedProduct, setSelectedProduct] = useState('')
  const [bom, setBom] = useState([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api.get('/products').then(r => setProducts(r.data))
    api.get('/raw-materials').then(r => setRawMaterials(r.data))
  }, [])

  const loadBom = async pid => {
    setSelectedProduct(pid)
    if (!pid) { setBom([]); return }
    const r = await api.get(`/bom/product/${pid}`)
    setBom(r.data.map(b => ({ rawMaterialId: b.rawMaterial.id, qtyRequired: b.qtyRequired, name: b.rawMaterial.name })))
  }

  const addRow = () => setBom([...bom, { rawMaterialId: '', qtyRequired: '' }])
  const removeRow = i => setBom(bom.filter((_, idx) => idx !== i))

  const updateRow = (i, field, val) => {
    const updated = [...bom]
    updated[i] = { ...updated[i], [field]: val }
    if (field === 'rawMaterialId') {
      const rm = rawMaterials.find(r => r.id === parseInt(val))
      updated[i].name = rm?.name || ''
    }
    setBom(updated)
  }

  const save = async () => {
    if (!selectedProduct) { toast.error('Select a product'); return }
    const invalid = bom.some(b => !b.rawMaterialId || !b.qtyRequired || parseFloat(b.qtyRequired) <= 0)
    if (invalid) { toast.error('All rows must have a raw material and quantity > 0'); return }
    setLoading(true)
    try {
      await api.post(`/bom/product/${selectedProduct}`, bom.map(b => ({
        rawMaterialId: parseInt(b.rawMaterialId),
        qtyRequired: parseFloat(b.qtyRequired)
      })))
      toast.success('BOM saved')
      // Reload from DB to confirm persistence
      await loadBom(selectedProduct)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Error saving BOM')
    } finally { setLoading(false) }
  }

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">Bill of Materials (BOM)</h2>
      <div className="bg-white rounded-xl shadow p-5">
        <div className="mb-4">
          <label className="text-sm font-medium">Select Product</label>
          <select className="w-full border rounded px-3 py-2 mt-1 text-sm"
            value={selectedProduct} onChange={e => loadBom(e.target.value)}>
            <option value="">-- Select Product --</option>
            {products.map(p => <option key={p.id} value={p.id}>{p.name} ({p.sku})</option>)}
          </select>
        </div>

        {selectedProduct && (
          <>
            <table className="w-full text-sm mb-3">
              <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
                <tr>
                  <th className="px-3 py-2 text-left">Raw Material</th>
                  <th className="px-3 py-2 text-left">Qty Required (per unit)</th>
                  <th className="px-3 py-2"></th>
                </tr>
              </thead>
              <tbody className="divide-y">
                {bom.map((row, i) => (
                  <tr key={i}>
                    <td className="px-3 py-2">
                      <select className="w-full border rounded px-2 py-1 text-sm"
                        value={row.rawMaterialId} onChange={e => updateRow(i, 'rawMaterialId', e.target.value)}>
                        <option value="">-- Select --</option>
                        {rawMaterials.map(r => <option key={r.id} value={r.id}>{r.name} ({r.unit})</option>)}
                      </select>
                    </td>
                    <td className="px-3 py-2">
                      <input type="number" step="0.0001" className="w-full border rounded px-2 py-1 text-sm"
                        value={row.qtyRequired} onChange={e => updateRow(i, 'qtyRequired', e.target.value)} />
                    </td>
                    <td className="px-3 py-2">
                      <button onClick={() => removeRow(i)} className="text-red-500 hover:text-red-700">
                        <Trash2 size={15} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <div className="flex gap-2">
              <button onClick={addRow} className="flex items-center gap-1 text-sm border px-3 py-1.5 rounded-lg hover:bg-gray-50">
                <Plus size={15} /> Add Row
              </button>
              <button onClick={save} disabled={loading}
                className="flex items-center gap-1 text-sm bg-blue-600 text-white px-3 py-1.5 rounded-lg hover:bg-blue-700 disabled:opacity-50">
                <Save size={15} /> Save BOM
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
