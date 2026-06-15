const heat = [
  ["Baku Medical", 18, 42, 31],
  ["Central Clinic", 12, 28, 46],
  ["Caspian Hospital", 22, 35, 19],
];

export function FraudHeatmap() {
  return (
    <section className="card">
      <div>
        <h3>Fraud istilik xəritəsi</h3>
        <p>Hospital və həkim səviyyəsində siqnallar</p>
      </div>
      <div className="stack">
        {heat.map(([name, low, medium, high]) => (
          <div className="field" key={String(name)}>
            <div className="between">
              <strong>{name}</strong>
              <span className="muted">{high}% yüksək</span>
            </div>
            <div className="progress-track" aria-label={`${name} fraud risk`}>
              <div
                className="progress-fill"
                style={{
                  width: `${Number(low) + Number(medium) + Number(high)}%`,
                  background: Number(high) > 35 ? "#dc2626" : "#d97706",
                }}
              />
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
