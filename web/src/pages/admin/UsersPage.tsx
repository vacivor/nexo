import { Button, Card, Input, Space, Table, Toast } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { createUser, deleteUser, listUsers, resetUserPassword, updateUser } from '../../api/admin'
import type { User } from '../../api/types'

export function UsersPage() {
  const [users, setUsers] = useState<User[]>([])
  const [createUserUsername, setCreateUserUsername] = useState('')
  const [createUserEmail, setCreateUserEmail] = useState('')
  const [createUserPhone, setCreateUserPhone] = useState('')
  const [createUserPassword, setCreateUserPassword] = useState('')
  const [editUserId, setEditUserId] = useState('')
  const [editUserUsername, setEditUserUsername] = useState('')
  const [editUserEmail, setEditUserEmail] = useState('')
  const [editUserPhone, setEditUserPhone] = useState('')
  const [resetPasswordUserId, setResetPasswordUserId] = useState('')
  const [resetPasswordValue, setResetPasswordValue] = useState('')

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
          <Button
            size="small"
            onClick={() => {
              setEditUserId(String(record.id))
              setEditUserUsername(record.username ?? '')
              setEditUserEmail(record.email ?? '')
              setEditUserPhone(record.phone ?? '')
            }}
          >
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
      <Card title="Create User" className="card-block">
        <Space wrap>
          <Input value={createUserUsername} onChange={setCreateUserUsername} placeholder="username" />
          <Input value={createUserEmail} onChange={setCreateUserEmail} placeholder="email" />
          <Input value={createUserPhone} onChange={setCreateUserPhone} placeholder="phone" />
          <Input mode="password" value={createUserPassword} onChange={setCreateUserPassword} placeholder="password" />
          <Button
            type="primary"
            onClick={async () => {
              try {
                await createUser({
                  username: createUserUsername,
                  email: createUserEmail || undefined,
                  phone: createUserPhone || undefined,
                  password: createUserPassword,
                })
                setCreateUserUsername('')
                setCreateUserEmail('')
                setCreateUserPhone('')
                setCreateUserPassword('')
                await load()
                Toast.success('User created')
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
      <Card title="Update User" className="card-block">
        <Space wrap>
          <Input value={editUserId} onChange={setEditUserId} placeholder="userId" />
          <Input value={editUserUsername} onChange={setEditUserUsername} placeholder="username" />
          <Input value={editUserEmail} onChange={setEditUserEmail} placeholder="email" />
          <Input value={editUserPhone} onChange={setEditUserPhone} placeholder="phone" />
          <Button
            type="primary"
            onClick={async () => {
              try {
                await updateUser(editUserId, {
                  username: editUserUsername,
                  email: editUserEmail || undefined,
                  phone: editUserPhone || undefined,
                })
                await load()
                Toast.success('User updated')
              } catch (e) {
                Toast.error((e as Error).message)
              }
            }}
          >
            Update
          </Button>
        </Space>
      </Card>
      <Card title="Reset Password" className="card-block">
        <Space wrap>
          <Input value={resetPasswordUserId} onChange={setResetPasswordUserId} placeholder="userId" />
          <Input mode="password" value={resetPasswordValue} onChange={setResetPasswordValue} placeholder="new password" />
          <Button
            type="warning"
            onClick={async () => {
              try {
                await resetUserPassword(resetPasswordUserId, resetPasswordValue)
                setResetPasswordValue('')
                Toast.success('Password reset')
              } catch (e) {
                Toast.error((e as Error).message)
              }
            }}
          >
            Reset
          </Button>
        </Space>
      </Card>
      <Card title="User List" className="card-block">
        <Table columns={columns} dataSource={users} rowKey="id" pagination />
      </Card>
    </Space>
  )
}
