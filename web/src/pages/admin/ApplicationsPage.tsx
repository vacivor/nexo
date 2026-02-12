import { Banner, Button, Card, Input, Space, TextArea, Toast } from '@douyinfe/semi-ui-19'
import { useState } from 'react'
import { createApplication } from '../../api/admin'
import type { Application } from '../../api/types'

export function ApplicationsPage() {
  const [applicationTenantUuid, setApplicationTenantUuid] = useState('')
  const [applicationName, setApplicationName] = useState('')
  const [applicationRedirectUris, setApplicationRedirectUris] = useState('')
  const [createdApplication, setCreatedApplication] = useState<Application | null>(null)

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Create OAuth Application" className="card-block">
        <Space vertical style={{ width: '100%' }}>
          <Input value={applicationTenantUuid} onChange={setApplicationTenantUuid} placeholder="tenant uuid" />
          <Input value={applicationName} onChange={setApplicationName} placeholder="application name" />
          <TextArea
            value={applicationRedirectUris}
            onChange={setApplicationRedirectUris}
            placeholder="redirect uris, comma or new line separated"
            rows={6}
          />
          <Button
            type="primary"
            onClick={async () => {
              try {
                const redirectUris = applicationRedirectUris
                  .split(/[\n,]/)
                  .map((s) => s.trim())
                  .filter((s) => s.length > 0)
                const result = await createApplication({
                  tenantId: applicationTenantUuid,
                  name: applicationName,
                  redirectUris,
                })
                setCreatedApplication(result ?? null)
                Toast.success('Application created')
              } catch (e) {
                Toast.error((e as Error).message)
              }
            }}
          >
            Create Application
          </Button>
          {createdApplication && (
            <Banner
              type="success"
              title={`clientId: ${createdApplication.clientId}`}
              description={`clientSecret: ${createdApplication.clientSecret}`}
              closeIcon={null}
            />
          )}
        </Space>
      </Card>
    </Space>
  )
}
