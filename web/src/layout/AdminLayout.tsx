import { Avatar, Layout, Nav, Breadcrumb, Button, Tooltip } from '@douyinfe/semi-ui-19'
import {
  IconAppCenter,
  IconBell,
  IconHelpCircle,
  IconLink,
  IconSafe,
  IconSidebar,
  IconUserGroup,
} from '@douyinfe/semi-icons'
import { useEffect, useMemo, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'

type AdminLayoutProps = {
  basePath?: '/admin' | '/platform'
  title?: string
}

export function AdminLayout({ basePath = '/admin', title = 'Admin' }: AdminLayoutProps) {
  const location = useLocation()
  const navigate = useNavigate()
  const navItems = useMemo(() => {
    const items = [
      { itemKey: `${basePath}/users`, text: 'Users', icon: <IconUserGroup /> },
      { itemKey: `${basePath}/applications`, text: 'Applications', icon: <IconAppCenter /> },
      { itemKey: `${basePath}/providers`, text: 'Providers', icon: <IconLink /> },
      { itemKey: `${basePath}/sessions`, text: 'Sessions', icon: <IconSafe /> },
    ]
    return items
  }, [basePath])

  const navRoutes = navItems.map((item) => ({
    itemKey: item.itemKey,
    text: item.text,
    path: item.itemKey,
  }))

  const selectedKey = useMemo(() => {
    const pathname = location.pathname === basePath ? `${basePath}/users` : location.pathname
    const sorted = [...navRoutes].sort((a, b) => b.path.length - a.path.length)
    const match = sorted.find((r) => pathname === r.path || pathname.startsWith(`${r.path}/`))
    return match?.itemKey ?? `${basePath}/users`
  }, [basePath, location.pathname, navRoutes])

  const crumbs = useMemo(() => {
    const pathname = location.pathname === basePath ? `${basePath}/users` : location.pathname
    const list: { name: string; path?: string }[] = [
      { name: title, path: `${basePath}/users` },
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
  }, [basePath, location.pathname, navRoutes, title])

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

  return (
    <Layout style={{ border: '1px solid var(--semi-color-border)', height: '100vh' }}>
      <Layout.Header style={{ backgroundColor: 'var(--semi-color-bg-1)' }}>
        <Nav mode="horizontal" defaultSelectedKeys={['Admin']}>
          <Nav.Header>
            <div
              onClick={() => navigate(`${basePath}/users`)}
              role="button"
              aria-label={`Nexo ${title}`}
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
              {`NEXO ${title}`}
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
          }}
        >
          <Nav
            style={{ width: collapsed ? 64 : 220, height: '100%', transition: 'width 0.2s ease' }}
            items={navItems}
            selectedKeys={[selectedKey]}
            isCollapsed={collapsed}
            onCollapseChange={setCollapsed}
            onSelect={({ itemKey }) => navigate(String(itemKey))}
            footer={{
              collapseButton: (
                <Tooltip content={collapsed ? 'Expand sidebar' : 'Collapse sidebar'} position="right">
                  <span>
                    <Button
                      icon={<IconSidebar />}
                      type="tertiary"
                      theme="borderless"
                      onClick={() => setCollapsed(!collapsed)}
                    />
                  </span>
                </Tooltip>
              ),
            }}
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
          <Outlet />
        </Layout.Content>
      </Layout>
    </Layout>
  )
}
