import { Button, Card, Input, Modal, Space, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { createApplication, deleteApplication, listApplications } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'
import type { Application } from '../../api/types'

const CLIENT_TYPES = [
  {
    value: 'NATIVE',
    title: 'Native',
    description: 'Mobile, desktop, CLI and smart device apps running natively.',
    examples: 'e.g.: iOS, Electron, Apple TV apps',
  },
  {
    value: 'SPA',
    title: 'Single Page Web Application',
    description: 'A JavaScript front-end app that uses an API.',
    examples: 'e.g.: Angular, React, Vue',
  },
  {
    value: 'WEB',
    title: 'Regular Web Application',
    description: 'Traditional web app using redirects.',
    examples: 'e.g.: Node.js Express, ASP.NET, Java, PHP',
  },
]

export function ApplicationsPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const basePath = resolveConsoleBasePath(location.pathname)
  const [applications, setApplications] = useState<Application[]>([])
  const [createVisible, setCreateVisible] = useState(false)
  const [name, setName] = useState('')
  const [clientType, setClientType] = useState<string>('')

  const load = async () => {
    setApplications(await listApplications())
  }

  useEffect(() => {
    load().catch(() => undefined)
  }, [])

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Application Management" className="card-block">
        <Space style={{ marginBottom: 16 }}>
          <Button type="primary" onClick={() => setCreateVisible(true)}>
            Create Application
          </Button>
          <Button onClick={() => load().catch(() => undefined)}>Refresh</Button>
        </Space>
        <Space wrap align="start" style={{ width: '100%' }}>
          {applications.map((application) => (
            <Card
              key={application.uuid}
              style={{ width: 360 }}
              title={application.name || '(Unnamed)'}
              footer={
                <Space>
                  <Button size="small" onClick={() => navigate(`${basePath}/applications/${application.uuid}/edit`)}>
                    Edit
                  </Button>
                  <Button
                    size="small"
                    type="danger"
                    onClick={async () => {
                      try {
                        await deleteApplication(application.uuid)
                        await load()
                        Toast.success('Application deleted')
                      } catch (e) {
                        Toast.error((e as Error).message)
                      }
                    }}
                  >
                    Delete
                  </Button>
                </Space>
              }
            >
              <Space vertical spacing="tight" style={{ width: '100%' }}>
                <Typography.Text type="tertiary">Application Type</Typography.Text>
                <Typography.Text>{application.clientType || '-'}</Typography.Text>
                <Typography.Text type="tertiary">Description</Typography.Text>
                <Typography.Text>{application.description || '-'}</Typography.Text>
                <Typography.Text type="tertiary">Client ID</Typography.Text>
                <Typography.Text style={{ wordBreak: 'break-all' }}>{application.clientId || '-'}</Typography.Text>
              </Space>
            </Card>
          ))}
        </Space>
      </Card>
      <Modal
        title="Create Application"
        visible={createVisible}
        width={800}
        onCancel={() => {
          setCreateVisible(false)
          setName('')
          setClientType('')
        }}
        onOk={async () => {
          if (!name.trim()) {
            Toast.error('Application name is required')
            return
          }
          if (!clientType) {
            Toast.error('Please select client type')
            return
          }
          try {
            const created = await createApplication({
              name: name.trim(),
              clientType,
            })
            setCreateVisible(false)
            setName('')
            setClientType('')
            Toast.success('Application created')
            await load()
            if (created?.uuid) {
              navigate(`${basePath}/applications/${created.uuid}/edit`)
            }
          } catch (e) {
            Toast.error((e as Error).message)
          }
        }}
      >
        <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 12, alignItems: 'stretch' }}>
          <Typography.Text strong style={{ display: 'block', textAlign: 'left' }}>
            Name <Typography.Text type="danger">*</Typography.Text>
          </Typography.Text>
          <Input value={name} onChange={setName} placeholder="application name" />
          <Typography.Text type="tertiary" style={{ textAlign: 'left' }}>
            You can change the application name later in the application settings.
          </Typography.Text>
          <Typography.Text strong style={{ display: 'block', textAlign: 'left' }}>
            Choose an application type
          </Typography.Text>
          <div
            style={{
              display: 'flex',
              flexDirection: 'row',
              gap: 12,
              width: '100%',
              overflowX: 'auto',
            }}
          >
            {CLIENT_TYPES.map((item) => (
              <Card
                key={item.value}
                style={{
                  width: 160,
                  flex: '0 0 160px',
                  cursor: 'pointer',
                  border:
                    clientType === item.value
                      ? '1px solid var(--semi-color-primary)'
                      : undefined,
                }}
                onClick={() => setClientType(item.value)}
              >
                <Space vertical spacing="tight">
                  <Typography.Text strong>{item.title}</Typography.Text>
                  <Typography.Text>{item.description}</Typography.Text>
                  <Typography.Text type="tertiary">{item.examples}</Typography.Text>
                </Space>
              </Card>
            ))}
          </div>
        </div>
      </Modal>
    </Space>
  )
}
