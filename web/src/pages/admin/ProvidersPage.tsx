import { Button, Card, Space, Table, Tag, Toast } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { deleteProvider, listProviders, setProviderEnabled } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'
import type { IdentityProvider } from '../../api/types'

export function ProvidersPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const [providers, setProviders] = useState<IdentityProvider[]>([])

  const load = async () => {
    setProviders(await listProviders())
  }

  useEffect(() => {
    load().catch(() => undefined)
  }, [])

  const columns = [
    { title: 'UUID', dataIndex: 'uuid' },
    { title: 'Protocol', dataIndex: 'protocol' },
    { title: 'Provider', dataIndex: 'provider' },
    {
      title: 'Enabled',
      render: (_text: unknown, record: IdentityProvider) => (
        <Tag color={record.enabled ? 'green' : 'red'}>{record.enabled ? 'true' : 'false'}</Tag>
      ),
    },
    { title: 'Display Name', dataIndex: 'displayName' },
    {
      title: 'Actions',
      render: (_text: unknown, record: IdentityProvider) => (
        <Space>
          <Button size="small" onClick={() => navigate(`${basePath}/providers/${record.uuid}/edit`)}>
            Edit
          </Button>
          <Button
            size="small"
            onClick={async () => {
              try {
                await setProviderEnabled(record.uuid, !record.enabled)
                await load()
                Toast.success('Provider updated')
              } catch (e) {
                Toast.error((e as Error).message)
              }
            }}
          >
            {record.enabled ? 'Disable' : 'Enable'}
          </Button>
          <Button
            size="small"
            type="danger"
            onClick={async () => {
              try {
                await deleteProvider(record.uuid)
                await load()
                Toast.success('Provider deleted')
              } catch (e) {
                Toast.error((e as Error).message)
              }
            }}
          >
            Delete
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Provider List" className="card-block">
        <Space style={{ marginBottom: 16 }}>
          <Button type="primary" onClick={() => navigate(`${basePath}/providers/create`)}>
            Create Provider
          </Button>
          <Button onClick={() => load().catch(() => undefined)}>Refresh</Button>
        </Space>
        <Table columns={columns} dataSource={providers} rowKey="uuid" pagination />
      </Card>
    </Space>
  )
}
