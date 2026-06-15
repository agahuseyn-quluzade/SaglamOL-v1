import { PortalShell } from "@/components/layout/PortalShell";

export default function HospitalLayout({ children }: { children: React.ReactNode }) {
  return <PortalShell portal="hospital">{children}</PortalShell>;
}
