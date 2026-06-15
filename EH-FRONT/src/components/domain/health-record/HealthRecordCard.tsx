import { HeartPulse } from "lucide-react";
import { Badge } from "@/components/ui/Badge";

export function HealthRecordCard() {
  return (
    <section className="card">
      <div className="between">
        <span className="cluster">
          <HeartPulse size={20} />
          <strong>REC-8821</strong>
        </span>
        <Badge tone="info">Treatment</Badge>
      </div>
      <p>Baku Medical tərəfindən yaradılmış müalicə qeydi.</p>
    </section>
  );
}
