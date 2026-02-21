import { Button, Card, Space, Table, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { listMyConsents, revokeMyConsent, revokeMyConsentScopes } from '../api/admin'
import type { UserConsentGrant } from '../api/types'

export function ConsentManagementPage() {
  const [grants, setGrants] = useState<UserConsentGrant[]>([])
  const [loading, setLoading] = useState(false)

  const load = async () => {
    setLoading(true)
    try {
      setGrants(await listMyConsents())
    } catch (e) {
      Toast.error((e as Error).message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void load()
  }, [])

  return (
    <Card title="Authorized Applications" className="card-block" loading={loading}>
      <Space style={{ marginBottom: 16 }}>
        <Button onClick={() => void load()}>Refresh</Button>
      </Space>
      <Table<UserConsentGrant>
        rowKey="clientId"
        dataSource={grants}
        pagination
        columns={[
          {
            title: 'Application',
            render: (_value: unknown, record: UserConsentGrant) => (
              <Space>
                <Typography.Text strong>{record.clientName || record.clientId}</Typography.Text>
                <Typography.Text type="tertiary">{record.clientId}</Typography.Text>
              </Space>
            ),
          },
          {
            title: 'Scopes',
            render: (_value: unknown, record: UserConsentGrant) => (
              <Space wrap>
                {record.scopes.map((scope) => (
                  <Button
                    key={`${record.clientId}-${scope}`}
                    size="small"
                    type="tertiary"
                    onClick={async () => {
                      try {
                        const remaining = await revokeMyConsentScopes(record.clientId, [scope])
                        if (remaining && remaining.length > 0) {
                          Toast.success(`Scope revoked: ${scope}`)
                        } else {
                          Toast.success('Consent revoked')
                        }
                        await load()
                      } catch (e) {
                        Toast.error((e as Error).message)
                      }
                    }}
                  >
                    {scope} x
                  </Button>
                ))}
              </Space>
            ),
          },
          {
            title: 'Granted At',
            render: (_value: unknown, record: UserConsentGrant) => (
              <Typography.Text>
                {record.grantedAt ? new Date(record.grantedAt).toLocaleString() : '-'}
              </Typography.Text>
            ),
          },
          {
            title: 'Actions',
            render: (_value: unknown, record: UserConsentGrant) => (
              <Button
                type="danger"
                size="small"
                onClick={async () => {
                  try {
                    await revokeMyConsent(record.clientId)
                    Toast.success('Consent revoked')
                    await load()
                  } catch (e) {
                    Toast.error((e as Error).message)
                  }
                }}
              >
                Revoke
              </Button>
            ),
          },
        ]}
      />
    </Card>
  )
}
