export type Page<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type ErrorResponse = {
  code?: string;
  details?: Record<string, string>;
  message: string;
  status?: number;
};

export type ClaimStatus =
  | "DRAFT"
  | "SUBMITTED"
  | "IN_REVIEW"
  | "APPROVED"
  | "REJECTED"
  | "NEEDS_MORE_DOCUMENTS"
  | "PAYOUT_FAILED";

export type ClaimSummaryResponse = {
  amount: number;
  claimId: string;
  patientName: string;
  status: ClaimStatus;
  submittedAt?: string;
};

export type ClaimResponse = ClaimSummaryResponse & {
  documents: string[];
  items: ClaimItemRequest[];
};

export type CreateClaimRequest = {
  hospitalId?: string;
  patientId?: string;
  policyId: string;
};

export type ClaimItemRequest = {
  code: string;
  description: string;
  quantity: number;
  unitPrice: number;
};

export type ClaimItemResponse = ClaimItemRequest & {
  id: string;
};

export type AttachClaimDocumentRequest = {
  documentId: string;
  type: string;
};

export type ReviewClaimRequest = {
  note?: string;
  reason?: string;
};

export type ClaimSearchFilters = {
  companyId?: string;
  hospitalId?: string;
  page?: number;
  q?: string;
  size?: number;
  status?: ClaimStatus;
};
