import { Siren } from "lucide-react";

export function FraudSummaryCard() {
  return (
    <section className="card">
      <div className="stat-top">
        <div>
          <h3>Company fraud summary</h3>
          <p>Hospital və claim üzrə risk bölgüsü</p>
        </div>
        <div className="stat-icon">
          <Siren size={20} />
        </div>
      </div>
      <p className="stat-value">23</p>
      <p className="muted">Açıq fraud siqnalı</p>
    </section>
  );
}
