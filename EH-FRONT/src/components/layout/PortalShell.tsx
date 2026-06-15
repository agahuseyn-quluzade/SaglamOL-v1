"use client";

import Image from "next/image";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect } from "react";
import { LogOut, Menu, Moon, Sun } from "lucide-react";
import { NotificationBell } from "@/components/domain/notification/NotificationBell";
import { Button } from "@/components/ui/Button";
import { getPortalForRole, type PortalKey } from "@/lib/auth/roles";
import { getPortalConfig } from "@/lib/data/portal-config";
import { useAuthStore } from "@/lib/stores/auth.store";
import { useUiStore } from "@/lib/stores/ui.store";

export function PortalShell({ children, portal }: { children: React.ReactNode; portal: PortalKey }) {
  const config = getPortalConfig(portal);
  const pathname = usePathname();
  const router = useRouter();
  const accessToken = useAuthStore((state) => state.accessToken);
  const hydrated = useAuthStore((state) => state.hydrated);
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const theme = useUiStore((state) => state.theme);
  const toggleTheme = useUiStore((state) => state.toggleTheme);

  function handleLogout() {
    document.cookie = "saglamol_role=; path=/; max-age=0; SameSite=Lax";
    logout();
    router.push("/login");
  }

  useEffect(() => {
    if (!hydrated) return;
    if (!accessToken) {
      router.replace("/login");
      return;
    }
    if (user && getPortalForRole(user.role) !== portal) {
      router.replace(`/${getPortalForRole(user.role)}/dashboard`);
    }
  }, [accessToken, hydrated, portal, router, user]);

  return (
    <div className="portal-shell" data-theme={theme}>
      <aside className="sidebar">
        <div className="sidebar-header">
          <Link className="brand" href={config.home}>
            <Image alt="SaglamOL" height={36} src="/logo-mark.png" width={36} />
            <span>SaglamOL</span>
          </Link>
          <Button aria-label="Menyu" iconOnly title="Menyu" variant="ghost">
            <Menu size={18} />
          </Button>
        </div>
        <nav className="sidebar-nav" aria-label={`${config.title} navigation`}>
          {config.nav.map((item) => {
            const Icon = item.icon;
            const active = pathname === item.href || pathname.startsWith(`${item.href}/`);
            return (
              <Link className={`sidebar-link ${active ? "is-active" : ""}`} href={item.href} key={item.href}>
                <Icon size={18} />
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>
        <div className="sidebar-footer">
          <p className="muted">{user?.fullName ?? "Demo user"}</p>
          <p className="eyebrow">{user?.role ?? config.title}</p>
        </div>
      </aside>
      <main className="portal-main">
        <header className="topbar">
          <div>
            <strong>{config.title}</strong>
            <p className="muted">{config.description}</p>
          </div>
          <div className="cluster">
            <NotificationBell />
            <Button aria-label="Theme" iconOnly onClick={toggleTheme} title="Theme" variant="secondary">
              {theme === "light" ? <Moon size={18} /> : <Sun size={18} />}
            </Button>
            <Button onClick={handleLogout} variant="secondary">
              <LogOut size={18} />
              Logout
            </Button>
          </div>
        </header>
        <div className="content-frame">{children}</div>
      </main>
    </div>
  );
}
