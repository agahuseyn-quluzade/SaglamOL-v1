import Link from "next/link";
import { Button } from "@/components/ui/Button";

export default function NotFound() {
  return (
    <main className="center-screen">
      <section className="empty-panel">
        <p className="eyebrow">404</p>
        <h1>Səhifə tapılmadı</h1>
        <p>Bu ünvan üçün səhifə mövcud deyil.</p>
        <Button asChild>
          <Link href="/">Ana səhifə</Link>
        </Button>
      </section>
    </main>
  );
}
