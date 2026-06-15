import { PortalPage } from "@/components/layout/PortalPage";

export default async function AdminPage({ params }: { params: Promise<{ slug?: string[] }> }) {
  const { slug } = await params;
  return <PortalPage portal="admin" slug={slug} />;
}
