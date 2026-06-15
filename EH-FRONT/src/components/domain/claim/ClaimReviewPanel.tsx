import { CheckCircle2, RotateCcw, ShieldAlert, XCircle } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";

export function ClaimReviewPanel() {
  return (
    <section className="card">
      <div className="between">
        <div>
          <h3>Review panel</h3>
          <p>Approve, reject, more-docs və gələcək resubmit/retry axınları</p>
        </div>
        <Badge tone="warning">Fraud check: active</Badge>
      </div>
      <label className="field" htmlFor="reviewNote">
        <span>Review qeydi</span>
        <textarea id="reviewNote" placeholder="Qərar qeydi" />
      </label>
      <div className="cluster">
        <Button>
          <CheckCircle2 size={18} />
          Approve
        </Button>
        <Button variant="secondary">
          <ShieldAlert size={18} />
          More docs
        </Button>
        <Button variant="secondary">
          <RotateCcw size={18} />
          Resubmit
        </Button>
        <Button variant="danger">
          <XCircle size={18} />
          Reject
        </Button>
      </div>
    </section>
  );
}
