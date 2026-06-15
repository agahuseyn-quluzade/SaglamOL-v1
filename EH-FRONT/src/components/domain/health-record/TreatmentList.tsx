export function TreatmentList() {
  return (
    <section className="card">
      <div>
        <h3>Treatments</h3>
        <p>Sağlamlıq qeydinə bağlı müalicələr</p>
      </div>
      <div className="stack">
        {["Initial exam", "Lab diagnostics", "Follow-up"].map((item) => (
          <div className="between" key={item}>
            <strong>{item}</strong>
            <span className="muted">Completed</span>
          </div>
        ))}
      </div>
    </section>
  );
}
