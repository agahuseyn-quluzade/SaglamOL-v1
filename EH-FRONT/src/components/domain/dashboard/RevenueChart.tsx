"use client";

import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { revenueData } from "@/lib/data/mock";

export function RevenueChart() {
  return (
    <section className="card">
      <div>
        <h3>Premium və payout</h3>
        <p>Son 6 ay üzrə əməliyyat axını</p>
      </div>
      <div style={{ height: 260 }}>
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={revenueData}>
            <CartesianGrid strokeDasharray="3 3" vertical={false} />
            <XAxis dataKey="month" />
            <YAxis />
            <Tooltip />
            <Area dataKey="premium" fill="#cffafe" stroke="#0891b2" strokeWidth={2} />
            <Area dataKey="payout" fill="#ede9fe" stroke="#7c3aed" strokeWidth={2} />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </section>
  );
}
