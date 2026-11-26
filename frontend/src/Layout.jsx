import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'
import { LogOut, Menu, X } from 'lucide-react'
import { useState } from 'react'

const navLinks = {
  ADMIN: [
    { to: '/', label: 'Dashboard' },
    { to: '/products', label: 'Products' },
    { to: '/customers', label: 'Customers' },
    { to: '/raw-materials', label: 'Raw Materials' },
    { to: '/bom', label: 'BOM' },
    { to: '/production', label: 'Production' },
    { to: '/sales-orders', label: 'Sales Orders' },
    { to: '/invoices', label: 'Invoices' },
    { to: '/inventory', label: 'Inventory' },
    { to: '/reports', label: 'Reports' },
    { to: '/users', label: 'Users' },
  ],
  SALESMAN: [
    { to: '/', label: 'Dashboard' },
    { to: '/customers', label: 'Customers' },
    { to: '/sales-orders', label: 'My Orders' },
  ],
  PRODUCTION: [
    { to: '/', label: 'Dashboard' },
    { to: '/raw-materials', label: 'Raw Materials' },
    { to: '/production', label: 'Production' },
    { to: '/inventory', label: 'Inventory' },
  ],
}

export default function Layout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [open, setOpen] = useState(false)
  const links = navLinks[user?.role] || []

  const handleLogout = () => { logout(); navigate('/login') }

  return (
    <div className="min-h-screen flex flex-col">
      <nav className="bg-blue-700 text-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 flex items-center justify-between h-14">
          <div className="flex items-center gap-3">
            <button className="md:hidden" onClick={() => setOpen(!open)}>
              {open ? <X size={20} /> : <Menu size={20} />}
            </button>
            <span className="font-bold text-lg tracking-tight">NIT Audit System</span>
          </div>
          <div className="hidden md:flex items-center gap-1">
            {links.map(l => (
              <Link key={l.to} to={l.to}
                className={`px-3 py-1.5 rounded text-sm font-medium transition-colors
                  ${location.pathname === l.to ? 'bg-blue-900' : 'hover:bg-blue-600'}`}>
                {l.label}
              </Link>
            ))}
          </div>
          <div className="flex items-center gap-2 text-sm">
            <span className="hidden sm:block opacity-80">{user?.fullName} ({user?.role})</span>
            <button onClick={handleLogout} className="flex items-center gap-1 hover:text-red-300 transition-colors">
              <LogOut size={16} /> Logout
            </button>
          </div>
        </div>
        {open && (
          <div className="md:hidden bg-blue-800 px-4 pb-3 flex flex-col gap-1">
            {links.map(l => (
              <Link key={l.to} to={l.to} onClick={() => setOpen(false)}
                className={`px-3 py-2 rounded text-sm ${location.pathname === l.to ? 'bg-blue-900' : 'hover:bg-blue-700'}`}>
                {l.label}
              </Link>
            ))}
          </div>
        )}
      </nav>
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 py-6">{children}</main>
    </div>
  )
}
