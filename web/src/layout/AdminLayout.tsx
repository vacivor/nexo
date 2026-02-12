import { Avatar, Banner, Layout, Nav, Breadcrumb, Button } from '@douyinfe/semi-ui-19'
import { IconBell, IconHelpCircle } from '@douyinfe/semi-icons'
import { useEffect, useMemo, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { SESSION_HEADER } from '../api/http'

const navItems = [
  { itemKey: '/admin/users', text: 'Users' },
  { itemKey: '/admin/tenants', text: 'Tenants' },
  { itemKey: '/admin/applications', text: 'Applications' },
  { itemKey: '/admin/providers', text: 'Providers' },
] as const

export function AdminLayout() {
  const location = useLocation()
  const navigate = useNavigate()

  const navRoutes = navItems.map((item) => ({
    itemKey: item.itemKey,
    text: item.text,
    path: item.itemKey,
  }))

  const selectedKey = useMemo(() => {
    const pathname = location.pathname === '/admin' ? '/admin/users' : location.pathname
    const sorted = [...navRoutes].sort((a, b) => b.path.length - a.path.length)
    const match = sorted.find((r) => pathname === r.path || pathname.startsWith(`${r.path}/`))
    return match?.itemKey ?? '/admin/users'
  }, [location.pathname, navRoutes])

  const crumbs = useMemo(() => {
    const pathname = location.pathname === '/admin' ? '/admin/users' : location.pathname
    const list: { name: string; path?: string }[] = [
      { name: 'Admin', path: '/admin/users' },
    ]
    const parent = navRoutes
      .sort((a, b) => b.path.length - a.path.length)
      .find((r) => pathname === r.path || pathname.startsWith(`${r.path}/`))
    if (!parent) {
      return list
    }
    list.push({ name: parent.text, path: parent.path })
    if (pathname !== parent.path) {
      list.push({ name: 'Details' })
    }
    return list
  }, [location.pathname, navRoutes])

  const [collapsed, setCollapsed] = useState(false)

  const mdQuery = useMemo(() => {
    if (typeof window === 'undefined') return null as MediaQueryList | null
    return window.matchMedia('(max-width: 768px)')
  }, [])

  useEffect(() => {
    if (!mdQuery) return
    const sync = () => setCollapsed(mdQuery.matches)
    sync()
    mdQuery.addEventListener?.('change', sync)
    return () => mdQuery.removeEventListener?.('change', sync)
  }, [mdQuery])

  const onBreakpoint = (_screen: string, isBelow: boolean) => {
    setCollapsed(!isBelow)
  }

  return (
    <Layout style={{ border: '1px solid var(--semi-color-border)', height: '100vh' }}>
      <Layout.Header style={{ backgroundColor: 'var(--semi-color-bg-1)' }}>
        <Nav mode="horizontal" defaultSelectedKeys={['Admin']}>
          <Nav.Header>
            <div
              onClick={() => navigate('/admin/users')}
              role="button"
              aria-label="Nexo Admin"
              style={{
                display: 'flex',
                alignItems: 'center',
                height: 36,
                padding: '0 8px',
                cursor: 'pointer',
                userSelect: 'none',
                fontWeight: 700,
                fontSize: 20,
                letterSpacing: 0.5,
                color: 'var(--semi-color-text-0)',
              }}
            >
              NEXO Console
            </div>
          </Nav.Header>
          <Nav.Footer>
            <Button
              theme="borderless"
              icon={<IconBell size="large" />}
              style={{ color: 'var(--semi-color-text-2)', marginRight: '12px' }}
            />
            <Button
              theme="borderless"
              icon={<IconHelpCircle size="large" />}
              style={{ color: 'var(--semi-color-text-2)', marginRight: '12px' }}
            />
            <Avatar color="orange" size="small">
              NA
            </Avatar>
          </Nav.Footer>
        </Nav>
      </Layout.Header>
      <Layout style={{ minHeight: 0 }}>
        <Layout.Sider
          style={{
            backgroundColor: 'var(--semi-color-bg-1)',
            transition: 'width 0.2s ease',
            width: collapsed ? 64 : 220,
          }}
          breakpoint={['md']}
          onBreakpoint={onBreakpoint}
        >
          <Nav
            style={{ maxWidth: collapsed ? 64 : 220, height: '100%' }}
            items={navItems}
            selectedKeys={[selectedKey]}
            isCollapsed={collapsed}
            onCollapseChange={setCollapsed}
            onSelect={({ itemKey }) => navigate(String(itemKey))}
            footer={{ collapseButton: true }}
          />
        </Layout.Sider>
        <Layout.Content
          style={{
            padding: '24px',
            backgroundColor: 'var(--semi-color-bg-0)',
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <Breadcrumb style={{ marginBottom: '12px' }}>
            {crumbs.map((c, idx) => {
              const isLast = idx === crumbs.length - 1
              const clickable = !isLast && c.path
              return (
                <Breadcrumb.Item
                  key={`${c.name}-${idx}`}
                  onClick={clickable ? () => navigate(c.path as string) : undefined}
                >
                  {c.name}
                </Breadcrumb.Item>
              )
            })}
          </Breadcrumb>
          <div
            style={{
              borderRadius: '10px',
              border: '1px solid var(--semi-color-border)',
              padding: '24px',
              minHeight: '376px',
              overflow: 'auto',
            }}
          >
            <Banner
              type="info"
              title="Session Transport"
              description={`Current ${SESSION_HEADER}: ${localStorage.getItem('nexoSessionId') ?? '(none)'}`}
              closeIcon={null}
            />
            <div style={{ marginTop: 16 }}>
              <Outlet />
            </div>
          </div>
        </Layout.Content>
      </Layout>
    </Layout>
  )
}
