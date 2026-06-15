import { Activity } from "lucide-react";
import { Badge } from "@/components/ui/Badge";

export function ClaimRiskBanner() {
  return (
    <section className="card">
      <div className="between">
        <span className="cluster">
          <Activity size={20} />
          <strong>AI risk assessment</strong>
        </span>
        <Badge tone="info">Medium</Badge>
      </div>
      <p>AI risk xidməti claim üçün məbləğ və təkrarlanma pattern-lərini analiz edir.</p>
    </section>
  );
}
