import { Banknote } from "lucide-react";
import { Button } from "@/components/ui/Button";

export function PayoutForm() {
  return (
    <section className="card">
      <div>
        <h3>Manual payout</h3>
        <p>Claim payout əməliyyatı</p>
      </div>
      <label className="field" htmlFor="claimIdPayout">
        <span>Claim ID</span>
        <input id="claimIdPayout" placeholder="CLM-24018" />
      </label>
      <Button>
        <Banknote size={18} />
        Payout
      </Button>
    </section>
  );
}
