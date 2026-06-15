import { Check } from "lucide-react";
import { timeline } from "@/lib/data/mock";

export function ClaimStatusTimeline() {
  return (
    <section className="card">
      <div>
        <h3>Status timeline</h3>
        <p>Claim lifecycle mərhələləri</p>
      </div>
      <div className="timeline">
        {timeline.map((item) => (
          <div className="timeline-item" key={item}>
            <div className="timeline-dot">
              <Check size={15} />
            </div>
            <div>
              <strong>{item}</strong>
              <p className="muted">Sistem tərəfindən qeyd edildi</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
