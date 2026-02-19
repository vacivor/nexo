import { Button, Card, Input, Select, Space, TextArea, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { createProvider } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'
import type { IdentityProviderProtocol } from '../../api/types'

export function ProviderCreatePage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const [protocol, setProtocol] = useState<IdentityProviderProtocol>('SOCIAL')
  const [provider, setProvider] = useState('github')
  const [displayName, setDisplayName] = useState('')
  const [clientId, setClientId] = useState('')
  const [clientSecret, setClientSecret] = useState('')
  const [authorizationUri, setAuthorizationUri] = useState('')
  const [tokenUri, setTokenUri] = useState('')
  const [userInfoUri, setUserInfoUri] = useState('')
  const [issuer, setIssuer] = useState('')
  const [jwksUri, setJwksUri] = useState('')
  const [redirectUri, setRedirectUri] = useState('')
  const [scopes, setScopes] = useState('')
  const [extraConfig, setExtraConfig] = useState('')

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Create Provider" className="card-block">
        <Space vertical style={{ width: '100%' }}>
          <Typography.Text strong>Protocol</Typography.Text>
          <Select
            value={protocol}
            onChange={(v) => setProtocol(v as IdentityProviderProtocol)}
            optionList={[
              { value: 'SOCIAL', label: 'SOCIAL' },
              { value: 'OIDC', label: 'OIDC' },
              { value: 'OAUTH2', label: 'OAUTH2' },
            ]}
            style={{ width: 220 }}
          />
          <Typography.Text strong>Provider</Typography.Text>
          <Input value={provider} onChange={setProvider} placeholder="provider name" />
          <Typography.Text strong>Display Name</Typography.Text>
          <Input value={displayName} onChange={setDisplayName} placeholder="display name" />
          <Typography.Text strong>Client ID</Typography.Text>
          <Input value={clientId} onChange={setClientId} placeholder="clientId" />
          <Typography.Text strong>Client Secret</Typography.Text>
          <Input value={clientSecret} onChange={setClientSecret} placeholder="clientSecret" />
          <Typography.Text strong>Authorization URI</Typography.Text>
          <Input value={authorizationUri} onChange={setAuthorizationUri} placeholder="authorizationUri" />
          <Typography.Text strong>Token URI</Typography.Text>
          <Input value={tokenUri} onChange={setTokenUri} placeholder="tokenUri" />
          <Typography.Text strong>UserInfo URI</Typography.Text>
          <Input value={userInfoUri} onChange={setUserInfoUri} placeholder="userInfoUri" />
          <Typography.Text strong>Issuer</Typography.Text>
          <Input value={issuer} onChange={setIssuer} placeholder="issuer" />
          <Typography.Text strong>JWKS URI</Typography.Text>
          <Input value={jwksUri} onChange={setJwksUri} placeholder="jwksUri" />
          <Typography.Text strong>Redirect URI</Typography.Text>
          <Input value={redirectUri} onChange={setRedirectUri} placeholder="redirectUri" />
          <Typography.Text strong>Scopes</Typography.Text>
          <Input value={scopes} onChange={setScopes} placeholder="scopes (space/comma)" />
          <Typography.Text strong>Extra Config</Typography.Text>
          <TextArea value={extraConfig} onChange={setExtraConfig} placeholder="extraConfig (json string)" rows={4} />
          <Space>
            <Button
              type="primary"
              onClick={async () => {
                try {
                  await createProvider({
                    protocol,
                    provider,
                    displayName: displayName || undefined,
                    enabled: true,
                    clientId: clientId || undefined,
                    clientSecret: clientSecret || undefined,
                    authorizationUri: authorizationUri || undefined,
                    tokenUri: tokenUri || undefined,
                    userInfoUri: userInfoUri || undefined,
                    issuer: issuer || undefined,
                    jwksUri: jwksUri || undefined,
                    redirectUri: redirectUri || undefined,
                    scopes: scopes || undefined,
                    extraConfig: extraConfig || undefined,
                  })
                  Toast.success('Provider created')
                  navigate(`${basePath}/providers`)
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              Create
            </Button>
            <Button onClick={() => navigate(`${basePath}/providers`)}>Cancel</Button>
          </Space>
        </Space>
      </Card>
    </Space>
  )
}
