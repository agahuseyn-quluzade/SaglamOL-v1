"use client";

import { useState } from "react";
import { ArrowRight, Check } from "lucide-react";
import { Button } from "@/components/ui/Button";

const steps = ["Policy", "Items", "Documents", "Submit"];

export function ClaimCreationWizard() {
  const [step, setStep] = useState(0);

  return (
    <section className="card">
      <div>
        <h3>Claim wizard</h3>
        <p>POST chain: claim, item, document, submit</p>
      </div>
      <div className="tabs">
        {steps.map((item, index) => (
          <button className={`tab ${index === step ? "is-active" : ""}`} key={item} onClick={() => setStep(index)}>
            {item}
          </button>
        ))}
      </div>
      <div className="grid-2">
        <label className="field" htmlFor="policyId">
          <span>Policy ID</span>
          <input id="policyId" placeholder="POL-10488" />
        </label>
        <label className="field" htmlFor="amount">
          <span>Məbləğ</span>
          <input id="amount" placeholder="1240" type="number" />
        </label>
      </div>
      <Button onClick={() => setStep((current) => Math.min(current + 1, steps.length - 1))}>
        {step === steps.length - 1 ? <Check size={18} /> : <ArrowRight size={18} />}
        {step === steps.length - 1 ? "Hazırdır" : "Növbəti"}
      </Button>
    </section>
  );
}
