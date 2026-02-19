import { Button, Card, Input, Space, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { createUser } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'

export function UserCreatePage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Create User" className="card-block">
        <Space vertical style={{ width: '100%' }}>
          <Typography.Text strong>Username</Typography.Text>
          <Input value={username} onChange={setUsername} placeholder="username" />
          <Typography.Text strong>Email</Typography.Text>
          <Input value={email} onChange={setEmail} placeholder="email" />
          <Typography.Text strong>Phone</Typography.Text>
          <Input value={phone} onChange={setPhone} placeholder="phone" />
          <Typography.Text strong>Password</Typography.Text>
          <Input mode="password" value={password} onChange={setPassword} placeholder="password" />
          <Space>
            <Button
              type="primary"
              onClick={async () => {
                if (!username.trim() || !password.trim()) {
                  Toast.error('Username and password are required')
                  return
                }
                try {
                  await createUser({
                    username: username.trim(),
                    email: email.trim() || undefined,
                    phone: phone.trim() || undefined,
                    password: password,
                  })
                  Toast.success('User created')
                  navigate(`${basePath}/users`)
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              Create
            </Button>
            <Button onClick={() => navigate(`${basePath}/users`)}>Cancel</Button>
          </Space>
        </Space>
      </Card>
    </Space>
  )
}
