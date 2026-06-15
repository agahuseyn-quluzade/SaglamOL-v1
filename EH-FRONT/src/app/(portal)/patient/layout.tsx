import { PortalShell } from "@/components/layout/PortalShell";

export default function PatientLayout({ children }: { children: React.ReactNode }) {
  return <PortalShell portal="patient">{children}</PortalShell>;
}
