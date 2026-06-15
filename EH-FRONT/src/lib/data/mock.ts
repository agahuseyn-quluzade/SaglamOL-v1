import type { PortalKey } from "@/lib/auth/roles";

export type StatItem = {
  label: string;
  trend: string;
  value: string;
};

export type Row = {
  id: string;
  amount: string;
  owner: string;
  status: string;
  type: string;
  updatedAt: string;
};

export type PortalPageModel = {
  actions: string[];
  description: string;
  endpoint: string;
  rows: Row[];
  stats: StatItem[];
  title: string;
};

const rows: Row[] = [
  {
    id: "CLM-24018",
    owner: "Leyla Məmmədova",
    type: "Hospital treatment",
    status: "In Review",
    amount: "₼ 1,240",
    updatedAt: "06.06.2026",
  },
  {
    id: "POL-10488",
    owner: "Araz Life",
    type: "Premium policy",
    status: "Active",
    amount: "₼ 18,000",
    updatedAt: "03.06.2026",
  },
  {
    id: "PAY-7731",
    owner: "Baku Medical",
    type: "Claim payout",
    status: "Paid",
    amount: "₼ 720",
    updatedAt: "30.05.2026",
  },
  {
    id: "FRD-901",
    owner: "Doctor audit",
    type: "Fraud signal",
    status: "Pending",
    amount: "82%",
    updatedAt: "28.05.2026",
  },
];

const baseStats: StatItem[] = [
  { label: "Açıq claim", value: "128", trend: "+14 bu həftə" },
  { label: "Aktiv polis", value: "4,812", trend: "+7.2%" },
  { label: "Ödəniş həcmi", value: "₼ 82k", trend: "+11.6%" },
  { label: "Risk siqnalı", value: "23", trend: "-4 dünəndən" },
];

const endpointMap: Record<PortalKey, Record<string, string>> = {
  patient: {
    dashboard: "/policies/me + /claims/my + /notifications/my",
    profile: "/profiles/patients/me",
    policies: "/policies/me",
    claims: "/claims/my",
    "claims/new": "POST /claims -> /claims/{id}/items -> submit",
    "health-records": "/health-records/my",
    payments: "/payments/my",
    notifications: "/notifications/my",
    settings: "/iam/password/change",
  },
  hospital: {
    dashboard: "/claims?hospitalId= + /fraud/hospitals/{id}/summary",
    claims: "/claims?hospitalId=",
    "claims/new": "POST /claims",
    "health-records": "/health-records",
    "documents/upload": "/health-records/{id}/documents",
    doctors: "/profiles/hospitals/{id}/doctors",
    staff: "/profiles/hospitals/{id}/staff",
    branches: "/profiles/hospitals/{id}/branches",
  },
  insurance: {
    dashboard: "Aggregation endpoints",
    claims: "/claims?companyId=",
    policies: "/policies?companyId=",
    "policies/issue": "POST /policies + /policies/eligibility-check",
    products: "/insurance-products?companyId=",
    payments: "/payments/by-company?companyId=",
    "payments/payout/new": "/payments/claim-payout",
    invoices: "/invoices/by-company?companyId=",
    fraud: "/fraud/companies/{id}/summary",
    risk: "/ai-risk/claims/{claimId}",
    contracts: "/provider-contracts/by-company/{id}",
    staff: "/insurance-companies/{id}/staff",
    agents: "/profiles/agents/by-company/{id}",
    company: "/insurance-companies/{id}",
  },
  admin: {
    dashboard: "System-wide KPIs",
    users: "/iam/users + /iam/users/search",
    companies: "/insurance-companies",
    hospitals: "/profiles/hospitals",
    patients: "/profiles/patients/search",
    "notifications/templates": "/notifications/templates",
  },
};

const titleMap: Record<string, string> = {
  dashboard: "Dashboard",
  profile: "Profil",
  policies: "Polislər",
  claims: "Claim-lər",
  "claims/new": "Yeni claim",
  "health-records": "Sağlamlıq qeydləri",
  payments: "Ödənişlər",
  notifications: "Bildirişlər",
  settings: "Tənzimləmələr",
  "documents/upload": "Sənəd yüklə",
  doctors: "Həkimlər",
  staff: "Personal",
  branches: "Filiallar",
  products: "Məhsullar",
  invoices: "Fakturalar",
  fraud: "Fraud analizi",
  risk: "AI risk",
  contracts: "Müqavilələr",
  agents: "Agentlər",
  company: "Şirkət profili",
  users: "İstifadəçilər",
  companies: "Sığorta şirkətləri",
  hospitals: "Hospitalar",
  patients: "Pasiyentlər",
  "notifications/templates": "Bildiriş şablonları",
};

export function buildPageModel(portal: PortalKey, slug?: string[]): PortalPageModel {
  const key = slug?.length ? slug.join("/") : "dashboard";
  const normalized = endpointMap[portal][key] ? key : key.split("/")[0] || "dashboard";
  const title = titleMap[key] ?? titleMap[normalized] ?? "Əməliyyat səhifəsi";

  return {
    title,
    description: `${title} üzrə əsas məlumatlar və gündəlik əməliyyatlar.`,
    endpoint: endpointMap[portal][key] ?? endpointMap[portal][normalized] ?? "Route placeholder",
    actions: key.includes("new") || key.includes("issue") ? ["Yadda saxla", "Qaralama"] : ["Filtr", "Yeni qeyd"],
    stats: baseStats,
    rows,
  };
}

export const pipelineData = [
  { name: "Draft", value: 18 },
  { name: "Submitted", value: 34 },
  { name: "Review", value: 42 },
  { name: "Approved", value: 25 },
  { name: "Rejected", value: 8 },
];

export const revenueData = [
  { month: "Yan", premium: 42000, payout: 18000 },
  { month: "Fev", premium: 51000, payout: 22000 },
  { month: "Mar", premium: 48000, payout: 21000 },
  { month: "Apr", premium: 62000, payout: 26000 },
  { month: "May", premium: 71000, payout: 31000 },
  { month: "Iyn", premium: 82000, payout: 35000 },
];

export const timeline = [
  "Claim yaradıldı",
  "Sənədlər əlavə edildi",
  "Review başladı",
  "Fraud guard yoxlanıldı",
];
