import { Download } from 'lucide-react'

const reports = [
  { label: 'Sales Orders Report', url: '/api/reports/sales-orders/csv', desc: 'All sales orders with status and amounts' },
  { label: 'Inventory Ledger Report', url: '/api/reports/inventory/csv', desc: 'Full inventory transaction history' },
  { label: 'Production Variance Report', url: '/api/reports/production/variance/csv', desc: 'Planned vs actual consumption & yield variance' },
]

export default function Reports() {
  const download = url => {
    const token = localStorage.getItem('token')
    fetch(`http://localhost:8080${url}`, { headers: { Authorization: `Bearer ${token}` } })
      .then(r => r.blob())
      .then(blob => {
        const a = document.createElement('a')
        a.href = URL.createObjectURL(blob)
        a.download = url.split('/').pop() + '.csv'
        a.click()
      })
  }

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">CSV Reports</h2>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {reports.map(r => (
          <div key={r.url} className="bg-white rounded-xl shadow p-5">
            <h3 className="font-semibold mb-1">{r.label}</h3>
            <p className="text-sm text-gray-500 mb-4">{r.desc}</p>
            <button onClick={() => download(r.url)}
              className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-700">
              <Download size={15} /> Download CSV
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}
