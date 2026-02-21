import { Button, Card, Space, Table, Tag, Toast } from '@douyinfe/semi-ui-19'
import { useEffect, useState } from 'react'
import { deleteSession, listSessions } from '../../api/admin'
import type { SessionView } from '../../api/types'

function formatDateTime(value?: string): string {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString()
}

export function SessionsPage() {
  const [sessions, setSessions] = useState<SessionView[]>([])
  const [nextCursor, setNextCursor] = useState<string | undefined>(undefined)
  const [hasMore, setHasMore] = useState(false)
  const [pageNo, setPageNo] = useState(1)
  const [loading, setLoading] = useState(false)
  const [loadingNext, setLoadingNext] = useState(false)

  const load = async () => {
    setLoading(true)
    try {
      const page = await listSessions(undefined, 10)
      setSessions(page.items ?? [])
      setNextCursor(page.nextCursor)
      setHasMore(Boolean(page.hasMore))
      setPageNo(1)
    } finally {
      setLoading(false)
    }
  }

  const loadNextPage = async () => {
    if (!hasMore || !nextCursor || loadingNext) {
      return
    }
    setLoadingNext(true)
    try {
      const page = await listSessions(nextCursor, 10)
      setSessions(page.items ?? [])
      setNextCursor(page.nextCursor)
      setHasMore(Boolean(page.hasMore))
      setPageNo((prev) => prev + 1)
    } finally {
      setLoadingNext(false)
    }
  }

  useEffect(() => {
    load().catch(() => undefined)
  }, [])

  return (
    <Space vertical style={{ width: '100%' }}>
      <Card title="Session Management" className="card-block">
        <Space style={{ marginBottom: 16 }}>
          <Button onClick={() => load().catch(() => undefined)} loading={loading}>
            Refresh
          </Button>
        </Space>
        <Table
          rowKey="id"
          loading={loading}
          dataSource={sessions}
          pagination={false}
          columns={[
            { title: 'Principal', dataIndex: 'principal', render: (v: string) => v || '-' },
            { title: 'Session ID', dataIndex: 'id', render: (v: string) => <span style={{ wordBreak: 'break-all' }}>{v}</span> },
            { title: 'Created At', dataIndex: 'createdAt', render: (v: string) => formatDateTime(v) },
            { title: 'Last Accessed', dataIndex: 'lastAccessedAt', render: (v: string) => formatDateTime(v) },
            { title: 'Expires At', dataIndex: 'expiresAt', render: (v: string) => formatDateTime(v) },
            {
              title: 'Status',
              render: (_: unknown, record: SessionView) => {
                const expired = record.expiresAt ? new Date(record.expiresAt).getTime() <= Date.now() : false
                return expired ? <Tag color="red">Expired</Tag> : <Tag color="green">Active</Tag>
              },
            },
            {
              title: 'Actions',
              render: (_: unknown, record: SessionView) => (
                <Button
                  size="small"
                  type="danger"
                  onClick={async () => {
                    try {
                      await deleteSession(record.id)
                      Toast.success('Session revoked')
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
        <Space style={{ marginTop: 16 }}>
          <span>{`Page ${pageNo}`}</span>
          <Button onClick={() => loadNextPage().catch(() => undefined)} disabled={!hasMore} loading={loadingNext}>
            Next Page
          </Button>
        </Space>
      </Card>
    </Space>
  )
}
