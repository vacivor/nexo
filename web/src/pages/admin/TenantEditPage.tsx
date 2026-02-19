import { Button, Card, Input, Space, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { getTenant, updateTenant } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'

export function TenantEditPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const { uuid } = useParams<{ uuid: string }>()
  const [name, setName] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      if (!uuid) {
        navigate(`${basePath}/tenants`)
        return
      }
      try {
        const tenant = await getTenant(uuid)
        if (!tenant) {
          Toast.error('Tenant not found')
          navigate(`${basePath}/tenants`)
          return
        }
        setName(tenant.name ?? '')
      } catch (e) {
        Toast.error((e as Error).message)
        navigate(`${basePath}/tenants`)
      } finally {
        setLoading(false)
      }
    }
    load().catch(() => undefined)
  }, [uuid, navigate, basePath])

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Edit Tenant" className="card-block" loading={loading}>
        <Space vertical style={{ width: '100%' }}>
          <Typography.Text strong>UUID</Typography.Text>
          <Input value={uuid ?? ''} disabled />
          <Typography.Text strong>Name</Typography.Text>
          <Input value={name} onChange={setName} placeholder="tenant name" />
          <Space>
            <Button
              type="primary"
              onClick={async () => {
                if (!uuid) {
                  return
                }
                if (!name.trim()) {
                  Toast.error('Tenant name is required')
                  return
                }
                try {
                  await updateTenant(uuid, name.trim())
                  Toast.success('Tenant updated')
                  navigate(`${basePath}/tenants`)
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              Save
            </Button>
            <Button onClick={() => navigate(`${basePath}/tenants`)}>Cancel</Button>
          </Space>
        </Space>
      </Card>
    </Space>
  )
}
