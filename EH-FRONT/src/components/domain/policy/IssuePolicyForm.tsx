import { Send } from "lucide-react";
import { Button } from "@/components/ui/Button";

export function IssuePolicyForm() {
  return (
    <section className="card">
      <div>
        <h3>Issue policy</h3>
        <p>Yeni polis yaratma formu</p>
      </div>
      <div className="grid-2">
        <label className="field" htmlFor="premium">
          <span>Premium</span>
          <input id="premium" placeholder="1200" type="number" />
        </label>
        <label className="field" htmlFor="limit">
          <span>Limit</span>
          <input id="limit" placeholder="18000" type="number" />
        </label>
      </div>
      <Button>
        <Send size={18} />
        Issue
      </Button>
    </section>
  );
}
