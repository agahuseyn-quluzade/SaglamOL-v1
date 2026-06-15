"use client";

import { Button } from "@/components/ui/Button";

export default function ErrorPage({ reset }: { reset: () => void }) {
  return (
    <main className="center-screen">
      <section className="empty-panel">
        <p className="eyebrow">Xəta</p>
        <h1>Gözlənilməz problem yarandı</h1>
        <p>Səhifəni yenidən yükləmək problemi həll edə bilər.</p>
        <Button onClick={reset}>Yenidən cəhd et</Button>
      </section>
    </main>
  );
}
