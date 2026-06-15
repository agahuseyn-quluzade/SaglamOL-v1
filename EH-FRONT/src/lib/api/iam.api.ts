import { apiClient } from "@/lib/api/client";

// POST /api/v1/iam/login/email  → backend EmailLoginRequest
export type LoginRequest = {
  email: string;
  password: string;
};

// POST /api/v1/iam/register → backend RegisterRequest (no role)
export type RegisterRequest = {
  email: string;
  phoneNumber?: string;
  password: string;
};

// Backend TokenResponse — no user/role embedded
export type TokenResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
};

// Backend MeResponse
export type MeResponse = {
  userId: string;
  email: string;
  phoneNumber?: string | null;
  roles: string[];
};

// Backend RegisterResponse
export type RegisterResponse = {
  userId: string;
  status: string;
};

export const iamApi = {
  login: (data: LoginRequest) => apiClient.post<TokenResponse>("/api/v1/iam/login/email", data),
  me: () => apiClient.get<MeResponse>("/api/v1/iam/me"),
  register: (data: RegisterRequest) => apiClient.post<RegisterResponse>("/api/v1/iam/register", data),
};
