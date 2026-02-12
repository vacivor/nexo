import { Button, Card, Input, Select, Space, Table, Tag, TextArea, Toast } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { createProvider, deleteProvider, listProviders, setProviderEnabled } from '../../api/admin'
import type { IdentityProvider, IdentityProviderProtocol } from '../../api/types'

export function ProvidersPage() {
  const [providers, setProviders] = useState<IdentityProvider[]>([])
  const [providerProtocol, setProviderProtocol] = useState<IdentityProviderProtocol>('SOCIAL')
  const [providerName, setProviderName] = useState('github')
  const [providerDisplayName, setProviderDisplayName] = useState('')
  const [providerClientId, setProviderClientId] = useState('')
  const [providerClientSecret, setProviderClientSecret] = useState('')
  const [providerAuthorizationUri, setProviderAuthorizationUri] = useState('')
  const [providerTokenUri, setProviderTokenUri] = useState('')
  const [providerUserInfoUri, setProviderUserInfoUri] = useState('')
  const [providerIssuer, setProviderIssuer] = useState('')
  const [providerJwksUri, setProviderJwksUri] = useState('')
  const [providerRedirectUri, setProviderRedirectUri] = useState('')
  const [providerScopes, setProviderScopes] = useState('')
  const [providerExtraConfig, setProviderExtraConfig] = useState('')

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
      <Card title="Create Provider" className="card-block">
        <Space vertical style={{ width: '100%' }}>
          <Space wrap>
            <Select
              value={providerProtocol}
              onChange={(v) => setProviderProtocol(v as IdentityProviderProtocol)}
              optionList={[
                { value: 'SOCIAL', label: 'SOCIAL' },
                { value: 'OIDC', label: 'OIDC' },
                { value: 'OAUTH2', label: 'OAUTH2' },
              ]}
              style={{ width: 180 }}
            />
            <Input value={providerName} onChange={setProviderName} placeholder="provider name" style={{ width: 180 }} />
            <Input value={providerDisplayName} onChange={setProviderDisplayName} placeholder="display name" style={{ width: 220 }} />
            <Input value={providerClientId} onChange={setProviderClientId} placeholder="clientId" />
            <Input value={providerClientSecret} onChange={setProviderClientSecret} placeholder="clientSecret" />
          </Space>
          <Space wrap>
            <Input value={providerAuthorizationUri} onChange={setProviderAuthorizationUri} placeholder="authorizationUri" />
            <Input value={providerTokenUri} onChange={setProviderTokenUri} placeholder="tokenUri" />
            <Input value={providerUserInfoUri} onChange={setProviderUserInfoUri} placeholder="userInfoUri" />
            <Input value={providerIssuer} onChange={setProviderIssuer} placeholder="issuer" />
          </Space>
          <Space wrap>
            <Input value={providerJwksUri} onChange={setProviderJwksUri} placeholder="jwksUri" />
            <Input value={providerRedirectUri} onChange={setProviderRedirectUri} placeholder="redirectUri" />
            <Input value={providerScopes} onChange={setProviderScopes} placeholder="scopes (space/comma)" />
          </Space>
          <TextArea
            value={providerExtraConfig}
            onChange={setProviderExtraConfig}
            placeholder="extraConfig (json string)"
            rows={4}
          />
          <Space>
            <Button
              type="primary"
              onClick={async () => {
                try {
                  await createProvider({
                    protocol: providerProtocol,
                    provider: providerName,
                    displayName: providerDisplayName || undefined,
                    enabled: true,
                    clientId: providerClientId || undefined,
                    clientSecret: providerClientSecret || undefined,
                    authorizationUri: providerAuthorizationUri || undefined,
                    tokenUri: providerTokenUri || undefined,
                    userInfoUri: providerUserInfoUri || undefined,
                    issuer: providerIssuer || undefined,
                    jwksUri: providerJwksUri || undefined,
                    redirectUri: providerRedirectUri || undefined,
                    scopes: providerScopes || undefined,
                    extraConfig: providerExtraConfig || undefined,
                  })
                  await load()
                  Toast.success('Provider created')
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              Create Provider
            </Button>
            <Button onClick={() => load().catch(() => undefined)}>Refresh</Button>
          </Space>
        </Space>
      </Card>
      <Card title="Provider List" className="card-block">
        <Table columns={columns} dataSource={providers} rowKey="uuid" pagination />
      </Card>
    </Space>
  )
}
