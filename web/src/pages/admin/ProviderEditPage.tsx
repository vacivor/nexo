import { Button, Card, Input, Select, Space, TextArea, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { getProvider, updateProvider } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'
import type { IdentityProviderProtocol } from '../../api/types'

export function ProviderEditPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const { uuid } = useParams<{ uuid: string }>()
  const [protocol, setProtocol] = useState<IdentityProviderProtocol>('SOCIAL')
  const [provider, setProvider] = useState('github')
  const [displayName, setDisplayName] = useState('')
  const [enabled, setEnabled] = useState(true)
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
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      if (!uuid) {
        navigate(`${basePath}/providers`)
        return
      }
      try {
        const data = await getProvider(uuid)
        if (!data) {
          Toast.error('Provider not found')
          navigate(`${basePath}/providers`)
          return
        }
        setProtocol(data.protocol)
        setProvider(data.provider)
        setDisplayName(data.displayName ?? '')
        setEnabled(data.enabled)
        setClientId(data.clientId ?? '')
        setClientSecret('')
        setAuthorizationUri(data.authorizationUri ?? '')
        setTokenUri(data.tokenUri ?? '')
        setUserInfoUri(data.userInfoUri ?? '')
        setIssuer(data.issuer ?? '')
        setJwksUri(data.jwksUri ?? '')
        setRedirectUri(data.redirectUri ?? '')
        setScopes(data.scopes ?? '')
        setExtraConfig(data.extraConfig ?? '')
      } catch (e) {
        Toast.error((e as Error).message)
        navigate(`${basePath}/providers`)
      } finally {
        setLoading(false)
      }
    }
    load().catch(() => undefined)
  }, [uuid, navigate, basePath])

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Edit Provider" className="card-block" loading={loading}>
        <Space vertical style={{ width: '100%' }}>
          <Typography.Text strong>UUID</Typography.Text>
          <Input value={uuid ?? ''} disabled />
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
          <Typography.Text strong>Enabled</Typography.Text>
          <Select
            value={enabled ? 'true' : 'false'}
            onChange={(v) => setEnabled(String(v) === 'true')}
            optionList={[
              { value: 'true', label: 'true' },
              { value: 'false', label: 'false' },
            ]}
            style={{ width: 220 }}
          />
          <Typography.Text strong>Client ID</Typography.Text>
          <Input value={clientId} onChange={setClientId} placeholder="clientId" />
          <Typography.Text strong>Client Secret</Typography.Text>
          <Input value={clientSecret} onChange={setClientSecret} placeholder="leave empty to keep unchanged" />
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
                if (!uuid) {
                  return
                }
                try {
                  await updateProvider(uuid, {
                    protocol,
                    provider,
                    displayName: displayName || undefined,
                    enabled,
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
                  Toast.success('Provider updated')
                  navigate(`${basePath}/providers`)
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              Save
            </Button>
            <Button onClick={() => navigate(`${basePath}/providers`)}>Cancel</Button>
          </Space>
        </Space>
      </Card>
    </Space>
  )
}
