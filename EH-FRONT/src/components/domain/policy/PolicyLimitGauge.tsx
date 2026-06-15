export function PolicyLimitGauge() {
  return (
    <section className="card">
      <div>
        <h3>Policy limit</h3>
        <p>Available, used və reserved göstəriciləri</p>
      </div>
      <div className="progress-track">
        <div className="progress-fill" style={{ width: "42%" }} />
      </div>
      <div className="between">
        <span className="muted">İstifadə: 42%</span>
        <strong>₼13,800 qalıb</strong>
      </div>
    </section>
  );
}
