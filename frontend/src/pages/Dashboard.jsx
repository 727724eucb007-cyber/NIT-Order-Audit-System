import { useEffect, useState } from 'react'
import { useAuth } from '../AuthContext'
import api from '../api'
import { ShoppingCart, Factory, FileText, TrendingUp } from 'lucide-react'

function StatCard({ label, value, icon: Icon, color }) {
  return (
    <div className={`bg-white rounded-xl shadow p-5 flex items-center gap-4 border-l-4 ${color}`}>
      <Icon size={32} className="text-gray-400" />
      <div>
        <p className="text-sm text-gray-500">{label}</p>
        <p className="text-2xl font-bold">{value ?? '—'}</p>
      </div>
    </div>
  )
}

export default function Dashboard() {
  const { user } = useAuth()
  const [stats, setStats] = useState(null)

  useEffect(() => {
    if (user?.role === 'ADMIN') {
      api.get('/reports/dashboard').then(r => setStats(r.data)).catch(() => {})
    }
  }, [user])

  if (user?.role !== 'ADMIN') {
    return (
      <div>
        <h2 className="text-xl font-bold mb-2">Welcome, {user?.fullName}</h2>
        <p className="text-gray-500">Role: <span className="font-medium text-blue-600">{user?.role}</span></p>
        <p className="mt-4 text-gray-600">Use the navigation menu to access your modules.</p>
      </div>
    )
  }

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">Admin Dashboard</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard label="Total Sales Orders" value={stats?.totalSalesOrders} icon={ShoppingCart} color="border-blue-500" />
        <StatCard label="Pending Approvals" value={stats?.pendingOrders} icon={FileText} color="border-yellow-500" />
        <StatCard label="Approved Orders" value={stats?.approvedOrders} icon={FileText} color="border-green-500" />
        <StatCard label="Production Orders" value={stats?.totalProductionOrders} icon={Factory} color="border-purple-500" />
        <StatCard label="Completed Production" value={stats?.completedProduction} icon={Factory} color="border-indigo-500" />
        <StatCard label="Total Revenue (₹)" value={stats?.totalRevenue != null ? `₹${Number(stats.totalRevenue).toLocaleString('en-IN')}` : '—'} icon={TrendingUp} color="border-emerald-500" />
      </div>
    </div>
  )
}
