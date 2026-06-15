import clsx from "clsx";

type BadgeTone = "success" | "warning" | "danger" | "info" | "neutral";

export function Badge({ children, tone = "neutral" }: { children: React.ReactNode; tone?: BadgeTone }) {
  return <span className={clsx("badge", `badge-${tone}`)}>{children}</span>;
}
