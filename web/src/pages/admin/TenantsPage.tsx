import { Button, Card, Space, Table } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { listTenants } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'
import type { Tenant } from '../../api/types'

export function TenantsPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const [tenants, setTenants] = useState<Tenant[]>([])

  const load = async () => {
    setTenants(await listTenants())
  }

  useEffect(() => {
    load().catch(() => undefined)
  }, [])

  const columns = [
    { title: 'ID', dataIndex: 'id' },
    { title: 'UUID', dataIndex: 'uuid' },
    { title: 'Name', dataIndex: 'name' },
    {
      title: 'Actions',
      render: (_text: unknown, record: Tenant) => (
        <Button size="small" onClick={() => navigate(`${basePath}/tenants/${record.uuid}/edit`)}>
          Edit
        </Button>
      ),
    },
  ]

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Tenant List" className="card-block">
        <Space style={{ marginBottom: 16 }}>
          <Button type="primary" onClick={() => navigate(`${basePath}/tenants/create`)}>
            Create Tenant
          </Button>
          <Button onClick={() => load().catch(() => undefined)}>Refresh</Button>
        </Space>
        <Table columns={columns} dataSource={tenants} rowKey="id" pagination />
      </Card>
    </Space>
  )
}
