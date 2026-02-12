import { Navigate, Route, Routes } from 'react-router-dom'
import { AdminLayout } from './layout/AdminLayout'
import { AuthLayout } from './layout/AuthLayout'
import { ApplicationsPage } from './pages/admin/ApplicationsPage'
import { ProvidersPage } from './pages/admin/ProvidersPage'
import { TenantsPage } from './pages/admin/TenantsPage'
import { UsersPage } from './pages/admin/UsersPage'
import { AuthPage } from './pages/AuthPage'
import './App.scss'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/auth" replace />} />
      <Route path="/auth" element={<AuthLayout />}>
        <Route index element={<AuthPage />} />
      </Route>
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<Navigate to="/admin/users" replace />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="tenants" element={<TenantsPage />} />
        <Route path="applications" element={<ApplicationsPage />} />
        <Route path="providers" element={<ProvidersPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/auth" replace />} />
    </Routes>
  )
}

export default App
