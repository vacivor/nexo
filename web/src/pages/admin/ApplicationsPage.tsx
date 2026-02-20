import { Button, Card, Dropdown, Input, Modal, Space, Table, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { createApplication, deleteApplication, listApplications } from '../../api/admin'
import { resolveConsoleBasePath } from '../../layout/consoleScope'
import type { Application } from '../../api/types'
import { IconMore } from '@douyinfe/semi-icons'

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
  const isPlatform = basePath === '/platform'

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
        {isPlatform ? (
          <Table
            className="applications-table"
            rowKey="uuid"
            pagination
            dataSource={applications}
            onRow={(record?: Application) => ({
              onClick: () => {
                if (!record) {
                  return
                }
                navigate(`${basePath}/applications/${record.uuid}/edit`)
              },
              style: { cursor: 'pointer' },
            })}
            columns={[
              {
                title: 'Logo',
                dataIndex: 'logo',
                render: (v: string) =>
                  v ? (
                    <img src={v} alt="logo" style={{ width: 24, height: 24, objectFit: 'cover', borderRadius: 4 }} />
                  ) : (
                    '-'
                  ),
              },
              { title: 'Name', dataIndex: 'name', render: (v: string) => v || '(Unnamed)' },
              { title: 'Type', dataIndex: 'clientType', render: (v: string) => v || '-' },
              { title: 'Description', dataIndex: 'description', render: (v: string) => v || '-' },
              {
                title: 'Client ID',
                dataIndex: 'clientId',
                render: (v: string) => <span style={{ wordBreak: 'break-all' }}>{v || '-'}</span>,
              },
              {
                title: 'Actions',
                render: (_: unknown, record: Application) => (
                  <Space>
                    <Button
                      size="small"
                      onClick={(e) => {
                        e.stopPropagation()
                        navigate(`${basePath}/applications/${record.uuid}/edit`)
                      }}
                    >
                      Edit
                    </Button>
                    <Button
                      size="small"
                      type="danger"
                      onClick={async (e) => {
                        e.stopPropagation()
                        try {
                          await deleteApplication(record.uuid)
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
                ),
              },
            ]}
          />
        ) : (
          <div style={{ maxHeight: '70vh', overflowY: 'auto', paddingRight: 4 }}>
            <Space vertical style={{ width: '100%' }}>
              {applications.map((application) => (
                <Card
                  key={application.uuid}
                  className="applications-clickable-item"
                  style={{ width: '100%' }}
                  bodyStyle={{ padding: 14 }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 16 }}>
                    <div
                      style={{ display: 'flex', alignItems: 'center', gap: 20, minWidth: 0, flex: 1, cursor: 'pointer' }}
                      onClick={() => navigate(`${basePath}/applications/${application.uuid}/edit`)}
                    >
                      <div style={{ width: 32, height: 32, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        {application.logo ? (
                          <img
                            src={application.logo}
                            alt="logo"
                            style={{ width: 32, height: 32, objectFit: 'cover', borderRadius: 6 }}
                          />
                        ) : (
                          '-'
                        )}
                      </div>
                      <div style={{ minWidth: 180 }}>
                        <div>{application.name || '(Unnamed)'}</div>
                        <Typography.Text type="tertiary">{application.clientType || '-'}</Typography.Text>
                      </div>
                      <div style={{ minWidth: 260 }}>
                        <Typography.Text type="tertiary">Client ID</Typography.Text>
                        <div style={{ wordBreak: 'break-all' }}>{application.clientId || '-'}</div>
                      </div>
                    </div>
                    <div onClick={(e) => e.stopPropagation()}>
                      <Dropdown
                        trigger="click"
                        position="bottomRight"
                        menu={[
                          {
                            node: 'item',
                            name: 'Settings',
                            onClick: () => navigate(`${basePath}/applications/${application.uuid}/edit`),
                          },
                        ]}
                      >
                        <span style={{ display: 'inline-flex' }}>
                          <Button theme="borderless" type="tertiary" icon={<IconMore />} />
                        </span>
                      </Dropdown>
                    </div>
                  </div>
                </Card>
              ))}
            </Space>
          </div>
        )}
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
              <div
                key={item.value}
                style={{ width: 160, flex: '0 0 160px', cursor: 'pointer' }}
                onClick={() => setClientType(item.value)}
              >
                <Card
                  style={{
                    width: '100%',
                    border:
                      clientType === item.value
                        ? '1px solid var(--semi-color-primary)'
                        : undefined,
                  }}
                >
                  <Space vertical spacing="tight">
                    <Typography.Text strong>{item.title}</Typography.Text>
                    <Typography.Text>{item.description}</Typography.Text>
                    <Typography.Text type="tertiary">{item.examples}</Typography.Text>
                  </Space>
                </Card>
              </div>
            ))}
          </div>
        </div>
      </Modal>
    </Space>
  )
}
