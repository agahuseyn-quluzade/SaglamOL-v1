import { CheckCircle2 } from "lucide-react";

const activity = ["Policy issued", "Claim review started", "Payout completed", "Template updated"];

export function RecentActivityFeed() {
  return (
    <section className="card">
      <div>
        <h3>Son fəaliyyət</h3>
        <p>Əməliyyat jurnalı</p>
      </div>
      <div className="timeline">
        {activity.map((item) => (
          <div className="timeline-item" key={item}>
            <div className="timeline-dot">
              <CheckCircle2 size={16} />
            </div>
            <div>
              <strong>{item}</strong>
              <p className="muted">Bu gün yeniləndi</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
