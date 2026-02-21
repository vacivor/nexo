import { Banner, Button, Layout, Space, Typography } from '@douyinfe/semi-ui-19'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { SESSION_HEADER } from '../api/http'

export function AuthLayout() {
  const location = useLocation()
  const navigate = useNavigate()

  return (
    <Layout className="auth-shell">
      <Layout.Header className="app-header">
        <Typography.Title heading={3} style={{ margin: 0 }}>
          Nexo Authentication
        </Typography.Title>
      </Layout.Header>
      <Layout.Content className="auth-content">
        <Banner
          type="info"
          title="Session Transport"
          description={`Current ${SESSION_HEADER}: ${localStorage.getItem('nexoSessionId') ?? '(none)'}`}
          closeIcon={null}
        />
        <Space style={{ marginTop: 12 }}>
          <Button
            type={location.pathname === '/auth' ? 'primary' : 'tertiary'}
            onClick={() => navigate('/auth')}
          >
            Login
          </Button>
          <Button
            type={location.pathname === '/auth/consents' ? 'primary' : 'tertiary'}
            onClick={() => navigate('/auth/consents')}
          >
            My Consents
          </Button>
        </Space>
        <div style={{ marginTop: 16 }}>
          <Outlet />
        </div>
      </Layout.Content>
    </Layout>
  )
}
