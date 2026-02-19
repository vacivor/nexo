import { Button, Card, Space, Table, Toast } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { deleteUser, listUsers } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'
import type { User } from '../../api/types'

export function UsersPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const [users, setUsers] = useState<User[]>([])

  const load = async () => {
    setUsers(await listUsers())
  }

  useEffect(() => {
    load().catch(() => undefined)
  }, [])

  const columns = [
    { title: 'ID', dataIndex: 'id' },
    { title: 'Username', dataIndex: 'username' },
    { title: 'Email', dataIndex: 'email' },
    { title: 'Phone', dataIndex: 'phone' },
    {
      title: 'Actions',
      render: (_text: unknown, record: User) => (
        <Space>
          <Button size="small" onClick={() => navigate(`${basePath}/users/${record.id}/edit`)}>
            Edit
          </Button>
          <Button
            size="small"
            type="danger"
            onClick={async () => {
              try {
                await deleteUser(record.id)
                await load()
                Toast.success('User deleted')
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
      <Card title="User List" className="card-block">
        <Space style={{ marginBottom: 16 }}>
          <Button type="primary" onClick={() => navigate(`${basePath}/users/create`)}>
            Create User
          </Button>
          <Button onClick={() => load().catch(() => undefined)}>Refresh</Button>
        </Space>
        <Table columns={columns} dataSource={users} rowKey="id" pagination />
      </Card>
    </Space>
  )
}
