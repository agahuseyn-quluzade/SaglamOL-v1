import type { LucideIcon } from "lucide-react";

export function StatCard({
  icon: Icon,
  label,
  trend,
  value,
}: {
  icon: LucideIcon;
  label: string;
  trend: string;
  value: string;
}) {
  return (
    <article className="card stat-card">
      <div className="stat-top">
        <div>
          <p className="muted">{label}</p>
          <p className="stat-value">{value}</p>
        </div>
        <div className="stat-icon">
          <Icon size={20} />
        </div>
      </div>
      <p className="muted">{trend}</p>
    </article>
  );
}
