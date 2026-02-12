import { Button, Card, Input, Space, Table, Toast } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { createTenant, listTenants } from '../../api/admin'
import type { Tenant } from '../../api/types'

export function TenantsPage() {
  const [tenants, setTenants] = useState<Tenant[]>([])
  const [newTenantName, setNewTenantName] = useState('')

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
  ]

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Create Tenant" className="card-block">
        <Space>
          <Input value={newTenantName} onChange={setNewTenantName} placeholder="tenant name" />
          <Button
            type="primary"
            onClick={async () => {
              try {
                await createTenant(newTenantName)
                setNewTenantName('')
                await load()
                Toast.success('Tenant created')
              } catch (e) {
                Toast.error((e as Error).message)
              }
            }}
          >
            Create
          </Button>
          <Button onClick={() => load().catch(() => undefined)}>Refresh</Button>
        </Space>
      </Card>
      <Card title="Tenant List" className="card-block">
        <Table columns={columns} dataSource={tenants} rowKey="id" pagination />
      </Card>
    </Space>
  )
}
