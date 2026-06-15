import { Badge } from "@/components/ui/Badge";

export function PaymentStatusBadge({ status = "PAID" }: { status?: "PAID" | "PENDING" | "FAILED" }) {
  const tone = status === "PAID" ? "success" : status === "PENDING" ? "warning" : "danger";
  return <Badge tone={tone}>{status}</Badge>;
}
