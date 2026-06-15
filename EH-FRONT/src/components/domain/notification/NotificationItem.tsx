import { Badge } from "@/components/ui/Badge";

export function NotificationItem({ title }: { title: string }) {
  return (
    <div className="between">
      <div>
        <strong>{title}</strong>
        <p className="muted">Az əvvəl</p>
      </div>
      <Badge tone="info">New</Badge>
    </div>
  );
}
