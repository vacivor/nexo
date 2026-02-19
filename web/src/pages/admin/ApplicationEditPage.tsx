import { Button, Card, Input, Space, TextArea, Toast } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { useLocation, useNavigate, useParams } from 'react-router-dom'
import { getApplication, updateApplication } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'

export function ApplicationEditPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const { uuid } = useParams<{ uuid: string }>()

  const [tenantId, setTenantId] = useState('')
  const [clientType, setClientType] = useState('')
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [idTokenExpiration, setIdTokenExpiration] = useState('')
  const [refreshTokenExpiration, setRefreshTokenExpiration] = useState('')
  const [redirectUris, setRedirectUris] = useState<string[]>([''])
  const [loading, setLoading] = useState(true)

  const normalizeRedirectUris = (values: string[]): string[] =>
    values.map((s) => s.trim()).filter((s) => s.length > 0)
  const parseOptionalPositiveInt = (value: string): number | undefined => {
    const trimmed = value.trim()
    if (!trimmed) {
      return undefined
    }
    const parsed = Number.parseInt(trimmed, 10)
    if (!Number.isFinite(parsed) || parsed <= 0) {
      return undefined
    }
    return parsed
  }

  useEffect(() => {
    const load = async () => {
      if (!uuid) {
        Toast.error('Invalid application uuid')
        navigate(`${basePath}/applications`)
        return
      }
      try {
        const app = await getApplication(uuid)
        if (!app) {
          Toast.error('Application not found')
          navigate(`${basePath}/applications`)
          return
        }
        setTenantId(app.tenantId ?? '')
        setClientType(app.clientType ?? '')
        setName(app.name ?? '')
        setDescription(app.description ?? '')
        setIdTokenExpiration(app.idTokenExpiration ? String(app.idTokenExpiration) : '')
        setRefreshTokenExpiration(app.refreshTokenExpiration ? String(app.refreshTokenExpiration) : '')
        setRedirectUris((app.redirectUris ?? []).length > 0 ? (app.redirectUris ?? []) : [''])
      } catch (e) {
        Toast.error((e as Error).message)
        navigate(`${basePath}/applications`)
      } finally {
        setLoading(false)
      }
    }
    load().catch(() => undefined)
  }, [uuid, navigate, basePath])

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Edit OAuth Application" className="card-block" loading={loading}>
        <Space vertical style={{ width: '100%' }}>
          <Input value={uuid ?? ''} disabled />
          <Input value={tenantId} onChange={setTenantId} placeholder="tenant uuid" />
          <Input value={clientType} onChange={setClientType} placeholder="client type" />
          <Input value={name} onChange={setName} placeholder="application name" />
          <TextArea
            value={description}
            onChange={setDescription}
            placeholder="application description"
            rows={4}
          />
          <Input
            value={idTokenExpiration}
            onChange={setIdTokenExpiration}
            placeholder="id token expiration (seconds)"
          />
          <Input
            value={refreshTokenExpiration}
            onChange={setRefreshTokenExpiration}
            placeholder="refresh token expiration (seconds)"
          />
          <Space vertical style={{ width: '100%' }}>
            {redirectUris.map((uri, index) => (
              <Space key={index} style={{ width: '100%' }}>
                <Input
                  value={uri}
                  onChange={(value) =>
                    setRedirectUris((prev) => prev.map((item, i) => (i === index ? value : item)))
                  }
                  placeholder="redirect uri"
                />
                <Button
                  type="danger"
                  disabled={redirectUris.length <= 1}
                  onClick={() => setRedirectUris((prev) => prev.filter((_, i) => i !== index))}
                >
                  -
                </Button>
              </Space>
            ))}
            <Button onClick={() => setRedirectUris((prev) => [...prev, ''])}>+ Add Redirect URI</Button>
          </Space>
          <Space>
            <Button
              type="primary"
              onClick={async () => {
                if (!uuid) {
                  Toast.error('Invalid application uuid')
                  return
                }
                try {
                  await updateApplication(uuid, {
                    tenantId: tenantId.trim() || undefined,
                    clientType: clientType.trim() || undefined,
                    name: name || undefined,
                    description: description.trim() || undefined,
                    idTokenExpiration: parseOptionalPositiveInt(idTokenExpiration),
                    refreshTokenExpiration: parseOptionalPositiveInt(refreshTokenExpiration),
                    redirectUris: normalizeRedirectUris(redirectUris),
                  })
                  Toast.success('Application updated')
                  navigate(`${basePath}/applications`)
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              Save
            </Button>
            <Button onClick={() => navigate(`${basePath}/applications`)}>Cancel</Button>
          </Space>
        </Space>
      </Card>
    </Space>
  )
}
