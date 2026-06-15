import { ShieldCheck } from "lucide-react";
import { Badge } from "@/components/ui/Badge";

export function PolicyCard() {
  return (
    <section className="card">
      <div className="between">
        <span className="cluster">
          <ShieldCheck size={20} />
          <strong>Premium Health POL-10488</strong>
        </span>
        <Badge tone="success">Active</Badge>
      </div>
      <p>Limit: ₼18,000, istifadə: ₼4,200, rezerv: ₼1,240</p>
    </section>
  );
}
