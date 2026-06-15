import { Badge } from "@/components/ui/Badge";

export function PolicyStatusBadge({ status = "ACTIVE" }: { status?: "ACTIVE" | "SUSPENDED" | "CANCELLED" | "EXPIRED" }) {
  const tone = status === "ACTIVE" ? "success" : status === "EXPIRED" ? "warning" : "danger";
  return <Badge tone={tone}>{status}</Badge>;
}
