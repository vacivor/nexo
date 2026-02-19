import { Button, Card, Input, Space, Tabs, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useState } from 'react'
import { login, register } from '../api/admin'
import type { LoginResponse } from '../api/types'

export function AuthPage() {
  const [loginResp, setLoginResp] = useState<LoginResponse | null>(null)

  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [registerUsername, setRegisterUsername] = useState('')
  const [registerPassword, setRegisterPassword] = useState('')

  return (
    <Space vertical align="start" style={{ width: '100%' }}>
      <Card title="Authentication" className="card-block" style={{ width: '100%' }}>
        <Tabs type="line">
          <Tabs.TabPane tab="Account Login" itemKey="identifier">
            <Space vertical style={{ width: '100%' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-start', gap: 12, width: '100%' }}>
                <Typography.Text strong style={{ width: 88, textAlign: 'left' }}>
                  Account
                </Typography.Text>
                <Input value={identifier} onChange={setIdentifier} placeholder="account" style={{ flex: 1 }} />
              </div>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-start', gap: 12, width: '100%' }}>
                <Typography.Text strong style={{ width: 88, textAlign: 'left' }}>
                  Password
                </Typography.Text>
                <Input mode="password" value={password} onChange={setPassword} placeholder="password" style={{ flex: 1 }} />
              </div>
              <Button
                theme="solid"
                type="primary"
                onClick={async () => {
                  try {
                    const resp = await login(identifier, password)
                    setLoginResp(resp ?? null)
                    Toast.success('Login success')
                  } catch (e) {
                    Toast.error((e as Error).message)
                  }
                }}
              >
                Login
              </Button>
            </Space>
          </Tabs.TabPane>
          <Tabs.TabPane tab="Register" itemKey="register">
            <Space vertical style={{ width: '100%' }}>
              <Typography.Text strong>Username</Typography.Text>
              <Input value={registerUsername} onChange={setRegisterUsername} placeholder="username" />
              <Typography.Text strong>Password</Typography.Text>
              <Input mode="password" value={registerPassword} onChange={setRegisterPassword} placeholder="password" />
              <Button
                theme="solid"
                type="primary"
                onClick={async () => {
                  try {
                    await register(registerUsername, registerPassword)
                    Toast.success('Register success')
                  } catch (e) {
                    Toast.error((e as Error).message)
                  }
                }}
              >
                Register
              </Button>
            </Space>
          </Tabs.TabPane>
          {/*
            <Tabs.TabPane tab="Social/OIDC/OAuth2" itemKey="federation">
              ...
            </Tabs.TabPane>
          */}
        </Tabs>
      </Card>
      {loginResp && (
        <Card title="Current Principal" className="card-block">
          <Typography.Text>{loginResp.principal}</Typography.Text>
        </Card>
      )}
    </Space>
  )
}
