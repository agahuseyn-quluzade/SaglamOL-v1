import { CreditCard } from "lucide-react";

export function PaymentTimeline() {
  return (
    <section className="card">
      <div>
        <h3>Payment timeline</h3>
        <p>Transaction history</p>
      </div>
      <div className="timeline">
        {["Premium charged", "Invoice issued", "Claim payout queued"].map((item) => (
          <div className="timeline-item" key={item}>
            <div className="timeline-dot">
              <CreditCard size={15} />
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
