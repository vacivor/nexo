import { Banner, Layout, Typography } from '@douyinfe/semi-ui-19'
import { Outlet } from 'react-router-dom'
import { SESSION_HEADER } from '../api/http'

export function AuthLayout() {
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
        <div style={{ marginTop: 16 }}>
          <Outlet />
        </div>
      </Layout.Content>
    </Layout>
  )
}
