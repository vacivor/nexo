import { Button, Card, Input, Space, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { createTenant } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'

export function TenantCreatePage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const [name, setName] = useState('')

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Create Tenant" className="card-block">
        <Space vertical style={{ width: '100%' }}>
          <Typography.Text strong>Name</Typography.Text>
          <Input value={name} onChange={setName} placeholder="tenant name" />
          <Space>
            <Button
              type="primary"
              onClick={async () => {
                if (!name.trim()) {
                  Toast.error('Tenant name is required')
                  return
                }
                try {
                  await createTenant(name.trim())
                  Toast.success('Tenant created')
                  navigate(`${basePath}/tenants`)
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              Create
            </Button>
            <Button onClick={() => navigate(`${basePath}/tenants`)}>Cancel</Button>
          </Space>
        </Space>
      </Card>
    </Space>
  )
}
