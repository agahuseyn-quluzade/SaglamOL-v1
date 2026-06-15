import { PortalPage } from "@/components/layout/PortalPage";

export default async function InsurancePage({ params }: { params: Promise<{ slug?: string[] }> }) {
  const { slug } = await params;
  return <PortalPage portal="insurance" slug={slug} />;
}
