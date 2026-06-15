"use client";

import { Activity, ClipboardList, CreditCard, ShieldCheck } from "lucide-react";
import { ClaimCreationWizard } from "@/components/domain/claim/ClaimCreationWizard";
import { ClaimDocumentList } from "@/components/domain/claim/ClaimDocumentList";
import { ClaimFraudBanner } from "@/components/domain/claim/ClaimFraudBanner";
import { ClaimReviewPanel } from "@/components/domain/claim/ClaimReviewPanel";
import { ClaimRiskBanner } from "@/components/domain/claim/ClaimRiskBanner";
import { ClaimStatusTimeline } from "@/components/domain/claim/ClaimStatusTimeline";
import { ClaimPipelineChart } from "@/components/domain/dashboard/ClaimPipelineChart";
import { FraudHeatmap } from "@/components/domain/dashboard/FraudHeatmap";
import { RecentActivityFeed } from "@/components/domain/dashboard/RecentActivityFeed";
import { RevenueChart } from "@/components/domain/dashboard/RevenueChart";
import { StatCard } from "@/components/domain/dashboard/StatCard";
import { FraudScoreGauge } from "@/components/domain/fraud/FraudScoreGauge";
import { FraudSignalList } from "@/components/domain/fraud/FraudSignalList";
import { FraudSummaryCard } from "@/components/domain/fraud/FraudSummaryCard";
import { DocumentUploader } from "@/components/domain/health-record/DocumentUploader";
import { DocumentViewer } from "@/components/domain/health-record/DocumentViewer";
import { HealthRecordCard } from "@/components/domain/health-record/HealthRecordCard";
import { TreatmentList } from "@/components/domain/health-record/TreatmentList";
import { NotificationList } from "@/components/domain/notification/NotificationList";
import { PaymentTimeline } from "@/components/domain/payment/PaymentTimeline";
import { PayoutForm } from "@/components/domain/payment/PayoutForm";
import { PremiumPaymentForm } from "@/components/domain/payment/PremiumPaymentForm";
import { EligibilityCheckForm } from "@/components/domain/policy/EligibilityCheckForm";
import { IssuePolicyForm } from "@/components/domain/policy/IssuePolicyForm";
import { PolicyCard } from "@/components/domain/policy/PolicyCard";
import { PolicyLimitGauge } from "@/components/domain/policy/PolicyLimitGauge";
import { Button } from "@/components/ui/Button";
import { DataTable, StatusCell } from "@/components/ui/DataTable";
import { PageHeader } from "@/components/ui/PageHeader";
import type { PortalKey } from "@/lib/auth/roles";
import { usePortalPageQuery } from "@/lib/hooks/useMockQuery";

const statIcons = [ClipboardList, ShieldCheck, CreditCard, Activity];

export function PortalPage({ portal, slug }: { portal: PortalKey; slug?: string[] }) {
  const { data } = usePortalPageQuery(portal, slug);
  const key = slug?.join("/") ?? "dashboard";

  if (!data) return null;

  return (
    <>
      <PageHeader
        actions={data.actions.map((action) => (
          <Button key={action} variant={action.includes("Yeni") || action.includes("Yadda") ? "primary" : "secondary"}>
            {action}
          </Button>
        ))}
        description={data.description}
        eyebrow="Is sahesi"
        title={data.title}
      />

      <section className="grid-4">
        {data.stats.map((stat, index) => (
          <StatCard icon={statIcons[index] ?? Activity} key={stat.label} {...stat} />
        ))}
      </section>

      <RouteModules keyName={key} portal={portal} />

      <DataTable
        columns={[
          { key: "id", header: "ID" },
          { key: "owner", header: "Owner" },
          { key: "type", header: "Type" },
          { key: "status", header: "Status", render: (row) => <StatusCell value={String(row.status)} /> },
          { key: "amount", header: "Amount" },
          { key: "updatedAt", header: "Updated" },
        ]}
        rows={data.rows}
      />
    </>
  );
}

function RouteModules({ keyName, portal }: { keyName: string; portal: PortalKey }) {
  if (keyName.includes("new") && keyName.includes("claims")) {
    return <ClaimCreationWizard />;
  }

  if (keyName.includes("review")) {
    return <ClaimReviewPanel />;
  }

  if (keyName.includes("issue")) {
    return (
      <div className="grid-2">
        <EligibilityCheckForm />
        <IssuePolicyForm />
      </div>
    );
  }

  if (keyName.includes("upload")) {
    return <DocumentUploader />;
  }

  if (keyName.includes("payout")) {
    return <PayoutForm />;
  }

  if (keyName.includes("fraud")) {
    return (
      <div className="grid-3">
        <FraudSummaryCard />
        <FraudScoreGauge />
        <FraudSignalList />
      </div>
    );
  }

  if (keyName.includes("risk")) {
    return (
      <div className="grid-2">
        <ClaimRiskBanner />
        <ClaimFraudBanner />
      </div>
    );
  }

  if (keyName.includes("health-records")) {
    return (
      <div className="grid-2">
        <HealthRecordCard />
        <TreatmentList />
        <DocumentViewer />
        <DocumentUploader />
      </div>
    );
  }

  if (keyName.includes("policies")) {
    return (
      <div className="grid-2">
        <PolicyCard />
        <PolicyLimitGauge />
      </div>
    );
  }

  if (keyName.includes("payments")) {
    return (
      <div className="grid-2">
        <PaymentTimeline />
        {portal === "patient" ? <PremiumPaymentForm /> : <PayoutForm />}
      </div>
    );
  }

  if (keyName.includes("notifications")) {
    return <NotificationList />;
  }

  if (keyName.includes("claims")) {
    return (
      <div className="grid-2">
        <ClaimStatusTimeline />
        <ClaimDocumentList />
        {portal === "insurance" ? <ClaimReviewPanel /> : null}
      </div>
    );
  }

  return (
    <div className="split-layout">
      <div className="grid-2">
        <ClaimPipelineChart />
        <RevenueChart />
      </div>
      <div className="stack">
        <FraudHeatmap />
        <RecentActivityFeed />
      </div>
    </div>
  );
}
