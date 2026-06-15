import { FileText } from "lucide-react";

const documents = ["Invoice.pdf", "Treatment-plan.pdf", "Doctor-note.pdf"];

export function ClaimDocumentList() {
  return (
    <section className="card">
      <div>
        <h3>Sənədlər</h3>
        <p>Claim-ə bağlı fayllar</p>
      </div>
      <div className="stack">
        {documents.map((document) => (
          <div className="between" key={document}>
            <span className="cluster">
              <FileText size={18} />
              <strong>{document}</strong>
            </span>
            <span className="muted">MinIO</span>
          </div>
        ))}
      </div>
    </section>
  );
}
