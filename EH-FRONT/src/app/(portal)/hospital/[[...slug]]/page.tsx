import { PortalPage } from "@/components/layout/PortalPage";

export default async function HospitalPage({ params }: { params: Promise<{ slug?: string[] }> }) {
  const { slug } = await params;
  return <PortalPage portal="hospital" slug={slug} />;
}
