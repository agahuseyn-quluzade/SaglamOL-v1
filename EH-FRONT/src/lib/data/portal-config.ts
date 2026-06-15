import {
  Activity,
  Bell,
  Building2,
  ClipboardList,
  CreditCard,
  FileText,
  HeartPulse,
  Home,
  Hospital,
  Landmark,
  LayoutDashboard,
  Receipt,
  ShieldCheck,
  Siren,
  Stethoscope,
  UserCog,
  Users,
} from "lucide-react";
import type { LucideIcon } from "lucide-react";
import type { PortalKey } from "@/lib/auth/roles";

export type PortalNavItem = {
  href: string;
  icon: LucideIcon;
  label: string;
};

export type PortalConfig = {
  description: string;
  home: string;
  key: PortalKey;
  nav: PortalNavItem[];
  title: string;
};

export const portalConfigs: Record<PortalKey, PortalConfig> = {
  patient: {
    key: "patient",
    title: "Patient Portal",
    description: "Polislər, claim-lər, sağlamlıq qeydləri və ödənişlər",
    home: "/patient/dashboard",
    nav: [
      { href: "/patient/dashboard", label: "Dashboard", icon: LayoutDashboard },
      { href: "/patient/profile", label: "Profil", icon: Users },
      { href: "/patient/policies", label: "Polislər", icon: ShieldCheck },
      { href: "/patient/claims", label: "Claim-lər", icon: ClipboardList },
      { href: "/patient/health-records", label: "Sağlamlıq qeydləri", icon: HeartPulse },
      { href: "/patient/payments", label: "Ödənişlər", icon: CreditCard },
      { href: "/patient/notifications", label: "Bildirişlər", icon: Bell },
      { href: "/patient/settings", label: "Tənzimləmələr", icon: UserCog },
    ],
  },
  hospital: {
    key: "hospital",
    title: "Hospital Portal",
    description: "Claim yaradılması, tibbi qeydlər, həkim və filial idarəetməsi",
    home: "/hospital/dashboard",
    nav: [
      { href: "/hospital/dashboard", label: "Dashboard", icon: LayoutDashboard },
      { href: "/hospital/claims", label: "Claim-lər", icon: ClipboardList },
      { href: "/hospital/health-records", label: "Tibbi qeydlər", icon: HeartPulse },
      { href: "/hospital/documents/upload", label: "Sənəd yüklə", icon: FileText },
      { href: "/hospital/doctors", label: "Həkimlər", icon: Stethoscope },
      { href: "/hospital/staff", label: "Personal", icon: Users },
      { href: "/hospital/branches", label: "Filiallar", icon: Hospital },
    ],
  },
  insurance: {
    key: "insurance",
    title: "Insurance Portal",
    description: "Claim review, policy issue, məhsullar, fraud və ödəniş əməliyyatları",
    home: "/insurance/dashboard",
    nav: [
      { href: "/insurance/dashboard", label: "Dashboard", icon: LayoutDashboard },
      { href: "/insurance/claims", label: "Claim-lər", icon: ClipboardList },
      { href: "/insurance/policies", label: "Polislər", icon: ShieldCheck },
      { href: "/insurance/products", label: "Məhsullar", icon: Landmark },
      { href: "/insurance/payments", label: "Ödənişlər", icon: CreditCard },
      { href: "/insurance/invoices", label: "Fakturalar", icon: Receipt },
      { href: "/insurance/fraud", label: "Fraud", icon: Siren },
      { href: "/insurance/risk/demo-claim", label: "AI Risk", icon: Activity },
      { href: "/insurance/contracts", label: "Müqavilələr", icon: FileText },
      { href: "/insurance/staff", label: "Personal", icon: Users },
      { href: "/insurance/agents", label: "Agentlər", icon: Home },
      { href: "/insurance/company", label: "Şirkət", icon: Building2 },
    ],
  },
  admin: {
    key: "admin",
    title: "Admin Panel",
    description: "İstifadəçi, tenant və sistem bildiriş şablonları",
    home: "/admin/dashboard",
    nav: [
      { href: "/admin/dashboard", label: "Dashboard", icon: LayoutDashboard },
      { href: "/admin/users", label: "İstifadəçilər", icon: Users },
      { href: "/admin/companies", label: "Şirkətlər", icon: Building2 },
      { href: "/admin/hospitals", label: "Hospitalar", icon: Hospital },
      { href: "/admin/patients", label: "Pasiyentlər", icon: HeartPulse },
      { href: "/admin/notifications/templates", label: "Şablonlar", icon: Bell },
    ],
  },
};

export function getPortalConfig(key: PortalKey) {
  return portalConfigs[key];
}
