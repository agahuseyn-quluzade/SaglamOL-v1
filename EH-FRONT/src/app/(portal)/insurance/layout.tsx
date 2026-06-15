import { PortalShell } from "@/components/layout/PortalShell";

export default function InsuranceLayout({ children }: { children: React.ReactNode }) {
  return <PortalShell portal="insurance">{children}</PortalShell>;
}
