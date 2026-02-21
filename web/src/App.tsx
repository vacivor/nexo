import { Navigate, Route, Routes } from 'react-router-dom'
import { AdminLayout } from './layout/AdminLayout'
import { AuthLayout } from './layout/AuthLayout'
import { ApplicationEditPage } from './pages/admin/ApplicationEditPage'
import { ApplicationsPage } from './pages/admin/ApplicationsPage'
import { ProviderCreatePage } from './pages/admin/ProviderCreatePage'
import { ProviderEditPage } from './pages/admin/ProviderEditPage'
import { ProvidersPage } from './pages/admin/ProvidersPage'
import { SessionsPage } from './pages/admin/SessionsPage'
import { UserCreatePage } from './pages/admin/UserCreatePage'
import { UserEditPage } from './pages/admin/UserEditPage'
import { UsersPage } from './pages/admin/UsersPage'
import { AuthPage } from './pages/AuthPage'
import { ConsentPage } from './pages/ConsentPage'
import { ConsentManagementPage } from './pages/ConsentManagementPage'
import './App.scss'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/auth" replace />} />
      <Route path="/auth" element={<AuthLayout />}>
        <Route index element={<AuthPage />} />
        <Route path="consents" element={<ConsentManagementPage />} />
      </Route>
      <Route path="/consent" element={<AuthLayout />}>
        <Route path="oauth" element={<ConsentPage protocol="oauth" />} />
        <Route path="oidc" element={<ConsentPage protocol="oidc" />} />
      </Route>
      <Route path="/platform" element={<AdminLayout basePath="/platform" title="Platform" />}>
        <Route index element={<Navigate to="/platform/users" replace />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="users/create" element={<UserCreatePage />} />
        <Route path="users/:id/edit" element={<UserEditPage />} />
        <Route path="applications" element={<ApplicationsPage />} />
        <Route path="applications/:uuid/edit" element={<ApplicationEditPage />} />
        <Route path="providers" element={<ProvidersPage />} />
        <Route path="providers/create" element={<ProviderCreatePage />} />
        <Route path="providers/:uuid/edit" element={<ProviderEditPage />} />
        <Route path="sessions" element={<SessionsPage />} />
      </Route>
      <Route path="/admin" element={<Navigate to="/platform" replace />} />
      <Route path="*" element={<Navigate to="/auth" replace />} />
    </Routes>
  )
}

export default App
