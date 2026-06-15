import { FileQuestion } from "lucide-react";

export function EmptyState({ title, description }: { title: string; description: string }) {
  return (
    <section className="empty-panel">
      <div className="stat-icon" style={{ marginInline: "auto" }}>
        <FileQuestion size={20} />
      </div>
      <h2>{title}</h2>
      <p>{description}</p>
    </section>
  );
}
