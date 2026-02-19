import { Button, Card, Input, Space, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { getUser, resetUserPassword, updateUser } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'

export function UserEditPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const { id } = useParams<{ id: string }>()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      if (!id) {
        navigate(`${basePath}/users`)
        return
      }
      try {
        const user = await getUser(id)
        if (!user) {
          Toast.error('User not found')
          navigate(`${basePath}/users`)
          return
        }
        setUsername(user.username ?? '')
        setEmail(user.email ?? '')
        setPhone(user.phone ?? '')
      } catch (e) {
        Toast.error((e as Error).message)
        navigate(`${basePath}/users`)
      } finally {
        setLoading(false)
      }
    }
    load().catch(() => undefined)
  }, [id, navigate, basePath])

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Edit User" className="card-block" loading={loading}>
        <Space vertical style={{ width: '100%' }}>
          <Typography.Text strong>User ID</Typography.Text>
          <Input value={id ?? ''} disabled />
          <Typography.Text strong>Username</Typography.Text>
          <Input value={username} onChange={setUsername} placeholder="username" />
          <Typography.Text strong>Email</Typography.Text>
          <Input value={email} onChange={setEmail} placeholder="email" />
          <Typography.Text strong>Phone</Typography.Text>
          <Input value={phone} onChange={setPhone} placeholder="phone" />
          <Space>
            <Button
              type="primary"
              onClick={async () => {
                if (!id) {
                  return
                }
                try {
                  await updateUser(id, {
                    username: username.trim(),
                    email: email.trim() || undefined,
                    phone: phone.trim() || undefined,
                  })
                  Toast.success('User updated')
                  navigate(`${basePath}/users`)
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              Save
            </Button>
            <Button onClick={() => navigate(`${basePath}/users`)}>Cancel</Button>
          </Space>
          <Typography.Text strong>Reset Password</Typography.Text>
          <Input mode="password" value={password} onChange={setPassword} placeholder="new password" />
          <Button
            type="warning"
            onClick={async () => {
              if (!id || !password.trim()) {
                Toast.error('Password is required')
                return
              }
              try {
                await resetUserPassword(id, password)
                setPassword('')
                Toast.success('Password reset')
              } catch (e) {
                Toast.error((e as Error).message)
              }
            }}
          >
            Reset Password
          </Button>
        </Space>
      </Card>
    </Space>
  )
}
