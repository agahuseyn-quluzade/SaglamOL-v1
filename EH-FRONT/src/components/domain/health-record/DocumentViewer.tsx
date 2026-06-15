import { FileText } from "lucide-react";

export function DocumentViewer() {
  return (
    <section className="card">
      <div>
        <h3>Document viewer</h3>
        <p>Seçilmiş sənəd önizləməsi</p>
      </div>
      <div className="empty-panel" style={{ width: "100%", padding: "2rem" }}>
        <FileText style={{ marginInline: "auto" }} />
        <strong>Invoice.pdf</strong>
      </div>
    </section>
  );
}
