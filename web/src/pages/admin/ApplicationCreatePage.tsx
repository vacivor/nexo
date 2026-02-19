import { Button, Card, Input, Space, TextArea, Toast } from '@douyinfe/semi-ui-19'
import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { createApplication } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'

const APPLICATION_TYPES = [
  { key: 'APP', title: 'APP', description: 'Native mobile/desktop app client.' },
  { key: 'WEB', title: 'Web', description: 'Server-rendered web application.' },
  { key: 'SPA', title: 'SPA', description: 'Single-page application running in browser.' },
]

export function ApplicationCreatePage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const [clientType, setClientType] = useState<string | null>(null)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Create OAuth Application" className="card-block">
        <Space vertical style={{ width: '100%' }}>
          {!clientType && (
            <>
              <div style={{ fontWeight: 600 }}>Select Client Type</div>
              <Space wrap align="start" style={{ width: '100%' }}>
                {APPLICATION_TYPES.map((item) => (
                  <Card
                    key={item.key}
                    style={{ width: 260, cursor: 'pointer' }}
                    onClick={() => setClientType(item.key)}
                  >
                    <Space vertical spacing="tight">
                      <div style={{ fontWeight: 600 }}>{item.title}</div>
                      <div style={{ color: 'var(--semi-color-text-2)' }}>{item.description}</div>
                    </Space>
                  </Card>
                ))}
              </Space>
            </>
          )}
          {clientType && (
            <>
              <Input value={clientType} disabled />
              <Input value={name} onChange={setName} placeholder="application name" />
              <TextArea
                value={description}
                onChange={setDescription}
                placeholder="application description"
                rows={4}
              />
            </>
          )}
          <Space>
            <Button
              type="primary"
              disabled={!clientType}
              onClick={async () => {
                if (!clientType) {
                  Toast.error('Please select client type')
                  return
                }
                if (!name.trim()) {
                  Toast.error('Application name is required')
                  return
                }
                try {
                  const result = await createApplication({
                    clientType,
                    name: name.trim(),
                    description: description.trim() || undefined,
                  })
                  Toast.success('Application created')
                  if (result?.uuid) {
                    navigate(`${basePath}/applications/${result.uuid}/edit`)
                  }
                } catch (e) {
                  Toast.error((e as Error).message)
                }
              }}
            >
              Create Application
            </Button>
            {clientType && (
              <Button
                onClick={() => {
                  setClientType(null)
                  setName('')
                  setDescription('')
                }}
              >
                Re-select Type
              </Button>
            )}
            <Button onClick={() => navigate(`${basePath}/applications`)}>Back to List</Button>
          </Space>
        </Space>
      </Card>
    </Space>
  )
}
