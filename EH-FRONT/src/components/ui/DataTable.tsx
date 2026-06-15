import { Badge } from "@/components/ui/Badge";

export type TableColumn<T> = {
  key: keyof T | string;
  header: string;
  render?: (row: T) => React.ReactNode;
};

export function DataTable<T extends Record<string, unknown>>({
  columns,
  rows,
}: {
  columns: TableColumn<T>[];
  rows: T[];
}) {
  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={String(column.key)}>{column.header}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, index) => (
            <tr key={String(row.id ?? index)}>
              {columns.map((column) => (
                <td key={String(column.key)}>{column.render ? column.render(row) : String(row[column.key] ?? "")}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function StatusCell({ value }: { value: string }) {
  const tone = value.includes("Rejected")
    ? "danger"
    : value.includes("Review") || value.includes("Pending")
      ? "warning"
      : value.includes("Approved") || value.includes("Active") || value.includes("Paid")
        ? "success"
        : "info";

  return <Badge tone={tone}>{value}</Badge>;
}
