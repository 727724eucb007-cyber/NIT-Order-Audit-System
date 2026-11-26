import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import { AuthProvider, useAuth } from './AuthContext'
import ProtectedRoute from './ProtectedRoute'
import Layout from './Layout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Products from './pages/Products'
import Customers from './pages/Customers'
import RawMaterials from './pages/RawMaterials'
import BOM from './pages/BOM'
import Production from './pages/Production'
import SalesOrders from './pages/SalesOrders'
import Invoices from './pages/Invoices'
import Inventory from './pages/Inventory'
import Reports from './pages/Reports'
import Users from './pages/Users'

function AppRoutes() {
  const { user } = useAuth()
  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <Login />} />
      <Route path="/*" element={
        <ProtectedRoute>
          <Layout>
            <Routes>
              <Route path="/" element={<Dashboard />} />
              <Route path="/products" element={<ProtectedRoute roles={['ADMIN']}><Products /></ProtectedRoute>} />
              <Route path="/customers" element={<ProtectedRoute roles={['ADMIN','SALESMAN']}><Customers /></ProtectedRoute>} />
              <Route path="/raw-materials" element={<ProtectedRoute roles={['ADMIN','PRODUCTION']}><RawMaterials /></ProtectedRoute>} />
              <Route path="/bom" element={<ProtectedRoute roles={['ADMIN']}><BOM /></ProtectedRoute>} />
              <Route path="/production" element={<ProtectedRoute roles={['ADMIN','PRODUCTION']}><Production /></ProtectedRoute>} />
              <Route path="/sales-orders" element={<ProtectedRoute roles={['ADMIN','SALESMAN']}><SalesOrders /></ProtectedRoute>} />
              <Route path="/invoices" element={<ProtectedRoute roles={['ADMIN']}><Invoices /></ProtectedRoute>} />
              <Route path="/inventory" element={<ProtectedRoute roles={['ADMIN','PRODUCTION']}><Inventory /></ProtectedRoute>} />
              <Route path="/reports" element={<ProtectedRoute roles={['ADMIN']}><Reports /></ProtectedRoute>} />
              <Route path="/users" element={<ProtectedRoute roles={['ADMIN']}><Users /></ProtectedRoute>} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Layout>
        </ProtectedRoute>
      } />
    </Routes>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Toaster position="top-right" />
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  )
}
