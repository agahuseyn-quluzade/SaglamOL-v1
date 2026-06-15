import { PublicHeader } from "@/components/layout/PublicHeader";

export default function PublicLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="public-shell">
      <PublicHeader />
      <main className="public-main">{children}</main>
    </div>
  );
}
