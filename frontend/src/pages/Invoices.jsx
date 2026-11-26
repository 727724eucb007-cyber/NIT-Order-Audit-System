import { useEffect, useState } from 'react'
import api from '../api'
import { FileText, Printer } from 'lucide-react'

export default function Invoices() {
  const [invoices, setInvoices] = useState([])
  const [selected, setSelected] = useState(null)

  useEffect(() => { api.get('/sales-orders/invoices').then(r => setInvoices(r.data)) }, [])

  const print = () => window.print()

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">GST Invoices</h2>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div className="lg:col-span-1 bg-white rounded-xl shadow overflow-y-auto max-h-[70vh]">
          {invoices.map(inv => (
            <div key={inv.id} onClick={() => setSelected(inv)}
              className={`px-4 py-3 border-b cursor-pointer hover:bg-blue-50 ${selected?.id === inv.id ? 'bg-blue-50 border-l-4 border-l-blue-500' : ''}`}>
              <p className="font-medium text-sm">{inv.invoiceNumber}</p>
              <p className="text-xs text-gray-500">{inv.salesOrder?.customer?.name}</p>
              <p className="text-xs text-gray-400">{inv.invoiceDate}</p>
              <p className="text-sm font-semibold text-blue-700">₹{parseFloat(inv.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</p>
            </div>
          ))}
          {invoices.length === 0 && <p className="p-4 text-gray-400 text-sm">No invoices yet</p>}
        </div>

        <div className="lg:col-span-2">
          {selected ? (
            <div className="bg-white rounded-xl shadow p-6 print:shadow-none" id="invoice-print">
              <div className="flex justify-between items-start mb-6">
                <div>
                  <h1 className="text-2xl font-bold text-blue-700">TAX INVOICE</h1>
                  <p className="text-xs text-gray-500 mt-1">NIT Trichy Food Products Pvt Ltd</p>
                  <p className="text-xs text-gray-500">GSTIN: 33AABCN1234A1Z5</p>
                  <p className="text-xs text-gray-500">NIT Campus, Tiruchirappalli - 620015</p>
                </div>
                <div className="text-right">
                  <p className="font-bold text-lg">{selected.invoiceNumber}</p>
                  <p className="text-sm text-gray-500">Date: {selected.invoiceDate}</p>
                  <button onClick={print} className="mt-2 flex items-center gap-1 text-xs text-blue-600 hover:underline print:hidden">
                    <Printer size={13} /> Print
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 mb-6 p-4 bg-gray-50 rounded-lg">
                <div>
                  <p className="text-xs font-semibold text-gray-500 uppercase mb-1">Bill To</p>
                  <p className="font-medium">{selected.salesOrder?.customer?.name}</p>
                  <p className="text-sm text-gray-600">{selected.salesOrder?.customer?.address}</p>
                  <p className="text-sm text-gray-600">GSTIN: {selected.salesOrder?.customer?.gstin}</p>
                  <p className="text-sm text-gray-600">{selected.salesOrder?.customer?.phone}</p>
                </div>
                <div>
                  <p className="text-xs font-semibold text-gray-500 uppercase mb-1">Order Details</p>
                  <p className="text-sm">Order #: {selected.salesOrder?.id}</p>
                  <p className="text-sm">Salesman: {selected.salesOrder?.salesman?.fullName}</p>
                </div>
              </div>

              <table className="w-full text-sm mb-4">
                <thead className="bg-gray-100 text-xs uppercase text-gray-600">
                  <tr>
                    <th className="px-3 py-2 text-left">#</th>
                    <th className="px-3 py-2 text-left">Product</th>
                    <th className="px-3 py-2 text-right">Qty</th>
                    <th className="px-3 py-2 text-right">Unit Price</th>
                    <th className="px-3 py-2 text-right">GST%</th>
                    <th className="px-3 py-2 text-right">GST Amt</th>
                    <th className="px-3 py-2 text-right">Total</th>
                  </tr>
                </thead>
                <tbody className="divide-y">
                  {selected.salesOrder?.items?.map((it, i) => {
                    const base = parseFloat(it.qty) * parseFloat(it.unitPrice)
                    const gst = base * parseFloat(it.gstRate) / 100
                    return (
                      <tr key={it.id}>
                        <td className="px-3 py-2 text-gray-400">{i + 1}</td>
                        <td className="px-3 py-2">{it.product?.name}</td>
                        <td className="px-3 py-2 text-right">{it.qty} {it.product?.unit}</td>
                        <td className="px-3 py-2 text-right">₹{it.unitPrice}</td>
                        <td className="px-3 py-2 text-right">{it.gstRate}%</td>
                        <td className="px-3 py-2 text-right">₹{gst.toFixed(2)}</td>
                        <td className="px-3 py-2 text-right font-medium">₹{(base + gst).toFixed(2)}</td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>

              <div className="flex justify-end">
                <div className="w-64 space-y-1 text-sm">
                  <div className="flex justify-between"><span className="text-gray-500">Subtotal</span><span>₹{parseFloat(selected.subtotal).toFixed(2)}</span></div>
                  <div className="flex justify-between"><span className="text-gray-500">GST Amount</span><span>₹{parseFloat(selected.gstAmount).toFixed(2)}</span></div>
                  <div className="flex justify-between font-bold text-base border-t pt-1">
                    <span>Total Amount</span><span className="text-blue-700">₹{parseFloat(selected.totalAmount).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                  </div>
                </div>
              </div>

              <div className="mt-6 pt-4 border-t text-xs text-gray-400 text-center">
                This is a computer-generated invoice. No signature required.
              </div>
            </div>
          ) : (
            <div className="bg-white rounded-xl shadow p-8 flex flex-col items-center justify-center text-gray-400 h-64">
              <FileText size={40} className="mb-2" />
              <p>Select an invoice to view</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
