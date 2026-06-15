import { PortalPage } from "@/components/layout/PortalPage";

export default async function PatientPage({ params }: { params: Promise<{ slug?: string[] }> }) {
  const { slug } = await params;
  return <PortalPage portal="patient" slug={slug} />;
}
