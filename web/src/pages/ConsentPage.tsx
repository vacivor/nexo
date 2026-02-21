import { Button, Card, Space, Toast, Typography } from '@douyinfe/semi-ui-19'
import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  approveOauthConsent,
  approveOidcConsent,
  denyOauthConsent,
  denyOidcConsent,
  getOauthConsent,
  getOidcConsent,
} from '../api/admin'
import type { ConsentView } from '../api/types'

type ConsentProtocol = 'oauth' | 'oidc'

type ConsentPageProps = {
  protocol: ConsentProtocol
}

export function ConsentPage({ protocol }: ConsentPageProps) {
  const [searchParams] = useSearchParams()
  const requestId = searchParams.get('request_id')?.trim() ?? ''
  const [consent, setConsent] = useState<ConsentView | null>(null)
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const title = useMemo(
    () => (protocol === 'oauth' ? 'OAuth2 Authorization Consent' : 'OIDC Authorization Consent'),
    [protocol],
  )

  useEffect(() => {
    if (!requestId) {
      Toast.error('Missing request_id')
      return
    }
    setLoading(true)
    const load = async () => {
      try {
        const response = protocol === 'oauth'
          ? await getOauthConsent(requestId)
          : await getOidcConsent(requestId)
        if (!response) {
          Toast.error('Consent request not found')
          return
        }
        setConsent(response)
      } catch (e) {
        Toast.error((e as Error).message)
      } finally {
        setLoading(false)
      }
    }
    void load()
  }, [protocol, requestId])

  const submit = async (action: 'approve' | 'deny') => {
    if (!consent) {
      return
    }
    setSubmitting(true)
    try {
      const payload = {
        request_id: consent.requestId,
        csrf_token: consent.csrfToken,
      }
      const response = protocol === 'oauth'
        ? action === 'approve'
          ? await approveOauthConsent(payload)
          : await denyOauthConsent(payload)
        : action === 'approve'
          ? await approveOidcConsent(payload)
          : await denyOidcConsent(payload)
      if (!response?.redirectUri) {
        Toast.error('Missing redirectUri in consent response')
        return
      }
      window.location.assign(response.redirectUri)
    } catch (e) {
      Toast.error((e as Error).message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Card title={title} className="card-block" loading={loading}>
      {!consent ? (
        <Typography.Text type="tertiary">No pending consent request.</Typography.Text>
      ) : (
        <Space vertical align="start" style={{ width: '100%' }}>
          <Typography.Text strong>Client</Typography.Text>
          <Typography.Text>{consent.clientId}</Typography.Text>

          <Typography.Text strong>Scopes</Typography.Text>
          <ul style={{ margin: 0, paddingLeft: 18 }}>
            {consent.scopes.map((scope) => (
              <li key={scope}>
                <Typography.Text>{scope}</Typography.Text>
              </li>
            ))}
          </ul>

          <Space>
            <Button
              theme="solid"
              type="primary"
              loading={submitting}
              onClick={() => {
                void submit('approve')
              }}
            >
              Allow
            </Button>
            <Button
              type="tertiary"
              loading={submitting}
              onClick={() => {
                void submit('deny')
              }}
            >
              Deny
            </Button>
          </Space>
        </Space>
      )}
    </Card>
  )
}
