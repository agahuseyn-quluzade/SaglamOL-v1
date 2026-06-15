import { ShieldAlert } from "lucide-react";
import { Badge } from "@/components/ui/Badge";

export function ClaimFraudBanner() {
  return (
    <section className="card">
      <div className="between">
        <span className="cluster">
          <ShieldAlert size={20} />
          <strong>Fraud guard</strong>
        </span>
        <Badge tone="warning">82% risk</Badge>
      </div>
      <p>Hospital, doctor və claim item uyğunluğu üzrə əlavə yoxlama tövsiyə edilir.</p>
    </section>
  );
}
