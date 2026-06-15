import { CreditCard } from "lucide-react";
import { Button } from "@/components/ui/Button";

export function PremiumPaymentForm() {
  return (
    <section className="card">
      <div>
        <h3>Premium payment</h3>
        <p>Patient premium ödənişi</p>
      </div>
      <label className="field" htmlFor="paymentAmount">
        <span>Məbləğ</span>
        <input id="paymentAmount" placeholder="120" type="number" />
      </label>
      <Button>
        <CreditCard size={18} />
        Ödə
      </Button>
    </section>
  );
}
