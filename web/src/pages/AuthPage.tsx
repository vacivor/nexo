import { Button, Card, Divider, Input, Select, Space, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useState } from 'react'
import { login, loginOauth2, loginOidc, loginSocial, register } from '../api/admin'
import type { LoginResponse } from '../api/types'

export function AuthPage() {
  const [loginResp, setLoginResp] = useState<LoginResponse | null>(null)

  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [registerUsername, setRegisterUsername] = useState('')
  const [registerPassword, setRegisterPassword] = useState('')
  const [socialProvider, setSocialProvider] = useState('github')
  const [socialAccessToken, setSocialAccessToken] = useState('')
  const [oidcProvider, setOidcProvider] = useState('default')
  const [oidcIdToken, setOidcIdToken] = useState('')
  const [oauth2Provider, setOauth2Provider] = useState('github')
  const [oauth2Code, setOauth2Code] = useState('')
  const [oauth2RedirectUri, setOauth2RedirectUri] = useState('')

  return (
    <Space vertical align="start" style={{ width: '100%' }}>
      <Card title="Identifier Login" className="card-block">
        <Space vertical style={{ width: '100%' }}>
          <Input value={identifier} onChange={setIdentifier} placeholder="identifier: username/phone/email" />
          <Input mode="password" value={password} onChange={setPassword} placeholder="password" />
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
      </Card>
      <Card title="Register" className="card-block">
        <Space vertical style={{ width: '100%' }}>
          <Input value={registerUsername} onChange={setRegisterUsername} placeholder="username" />
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
      </Card>
      <Card title="Social/OIDC/OAuth2 Login" className="card-block">
        <Space vertical style={{ width: '100%' }}>
          <Divider margin="12px" align="left">Social</Divider>
          <Space>
            <Select
              value={socialProvider}
              onChange={(v) => setSocialProvider(String(v))}
              optionList={[
                { value: 'qq', label: 'qq' },
                { value: 'dingtalk', label: 'dingtalk' },
                { value: 'wechat', label: 'wechat' },
                { value: 'github', label: 'github' },
              ]}
              style={{ width: 180 }}
            />
            <Input value={socialAccessToken} onChange={setSocialAccessToken} placeholder="accessToken" />
            <Button
              onClick={async () => {
                try {
                  const resp = await loginSocial(socialProvider, socialAccessToken)
                  setLoginResp(resp ?? null)
                  Toast.success('Social login success')
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              /login/social
            </Button>
          </Space>
          <Divider margin="12px" align="left">OIDC</Divider>
          <Space>
            <Input value={oidcProvider} onChange={setOidcProvider} placeholder="provider" style={{ width: 180 }} />
            <Input value={oidcIdToken} onChange={setOidcIdToken} placeholder="idToken" />
            <Button
              onClick={async () => {
                try {
                  const resp = await loginOidc(oidcProvider, oidcIdToken)
                  setLoginResp(resp ?? null)
                  Toast.success('OIDC login success')
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              /login/oidc
            </Button>
          </Space>
          <Divider margin="12px" align="left">OAuth2 Code</Divider>
          <Space>
            <Input value={oauth2Provider} onChange={setOauth2Provider} placeholder="provider" style={{ width: 180 }} />
            <Input value={oauth2Code} onChange={setOauth2Code} placeholder="code" />
            <Input value={oauth2RedirectUri} onChange={setOauth2RedirectUri} placeholder="redirectUri" />
            <Button
              onClick={async () => {
                try {
                  const resp = await loginOauth2(oauth2Provider, oauth2Code, oauth2RedirectUri)
                  setLoginResp(resp ?? null)
                  Toast.success('OAuth2 login success')
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              /login/oauth2
            </Button>
          </Space>
        </Space>
      </Card>
      {loginResp && (
        <Card title="Current Principal" className="card-block">
          <Typography.Text>{loginResp.principal}</Typography.Text>
        </Card>
      )}
    </Space>
  )
}
