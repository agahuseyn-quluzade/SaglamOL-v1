"use client";

import { RadialBar, RadialBarChart, ResponsiveContainer } from "recharts";

const data = [{ name: "Fraud", value: 82, fill: "#dc2626" }];

export function FraudScoreGauge() {
  return (
    <section className="card">
      <div>
        <h3>Fraud score</h3>
        <p>Claim siqnalları üzrə yekun skor</p>
      </div>
      <div style={{ height: 190 }}>
        <ResponsiveContainer width="100%" height="100%">
          <RadialBarChart innerRadius="70%" outerRadius="100%" data={data} startAngle={180} endAngle={0}>
            <RadialBar dataKey="value" background cornerRadius={8} />
          </RadialBarChart>
        </ResponsiveContainer>
      </div>
      <p className="stat-value">82%</p>
    </section>
  );
}
