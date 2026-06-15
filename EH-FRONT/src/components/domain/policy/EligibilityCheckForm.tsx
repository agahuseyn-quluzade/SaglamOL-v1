import { Search } from "lucide-react";
import { Button } from "@/components/ui/Button";

export function EligibilityCheckForm() {
  return (
    <section className="card">
      <div>
        <h3>Eligibility check</h3>
        <p>Policy issue öncəsi uyğunluq yoxlaması</p>
      </div>
      <div className="grid-2">
        <label className="field" htmlFor="patientId">
          <span>Patient ID</span>
          <input id="patientId" placeholder="PAT-1002" />
        </label>
        <label className="field" htmlFor="productId">
          <span>Product ID</span>
          <input id="productId" placeholder="PRD-HEALTH" />
        </label>
      </div>
      <Button>
        <Search size={18} />
        Yoxla
      </Button>
    </section>
  );
}
