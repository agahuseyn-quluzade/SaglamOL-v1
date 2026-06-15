import { Badge } from "@/components/ui/Badge";

const signals = ["Duplicate invoice", "Unusual doctor frequency", "High claim amount"];

export function FraudSignalList() {
  return (
    <section className="card">
      <div>
        <h3>Fraud signals</h3>
        <p>Scored signal siyahısı</p>
      </div>
      <div className="stack">
        {signals.map((signal, index) => (
          <div className="between" key={signal}>
            <strong>{signal}</strong>
            <Badge tone={index === 0 ? "danger" : "warning"}>{index === 0 ? "High" : "Medium"}</Badge>
          </div>
        ))}
      </div>
    </section>
  );
}
