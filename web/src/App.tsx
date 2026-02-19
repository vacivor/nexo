import { Navigate, Route, Routes } from 'react-router-dom'
import { AdminLayout } from './layout/AdminLayout'
import { AuthLayout } from './layout/AuthLayout'
import { ApplicationCreatePage } from './pages/admin/ApplicationCreatePage'
import { ApplicationEditPage } from './pages/admin/ApplicationEditPage'
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
      <Route path="/platform" element={<AdminLayout basePath="/platform" title="Platform" />}>
        <Route index element={<Navigate to="/platform/users" replace />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="tenants" element={<TenantsPage />} />
        <Route path="applications" element={<ApplicationsPage />} />
        <Route path="applications/create" element={<ApplicationCreatePage />} />
        <Route path="applications/:uuid/edit" element={<ApplicationEditPage />} />
        <Route path="providers" element={<ProvidersPage />} />
      </Route>
      <Route path="/tenant" element={<AdminLayout basePath="/tenant" title="Tenant" />}>
        <Route index element={<Navigate to="/tenant/users" replace />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="applications" element={<ApplicationsPage />} />
        <Route path="applications/create" element={<ApplicationCreatePage />} />
        <Route path="applications/:uuid/edit" element={<ApplicationEditPage />} />
        <Route path="providers" element={<ProvidersPage />} />
      </Route>
      <Route path="/admin" element={<Navigate to="/platform" replace />} />
      <Route path="*" element={<Navigate to="/auth" replace />} />
    </Routes>
  )
}

export default App
