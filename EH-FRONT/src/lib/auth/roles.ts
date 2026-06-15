export type UserRole =
  | "PATIENT"
  | "DOCTOR"
  | "HOSPITAL_ADMIN"
  | "HOSPITAL_STAFF"
  | "INSURANCE_ADMIN"
  | "INSURANCE_STAFF"
  | "AGENT"
  | "ADMIN"
  | "SYSTEM";

export type PortalKey = "patient" | "hospital" | "insurance" | "admin";

export const rolePortalMap: Record<UserRole, PortalKey> = {
  PATIENT: "patient",
  DOCTOR: "hospital",
  HOSPITAL_ADMIN: "hospital",
  HOSPITAL_STAFF: "hospital",
  INSURANCE_ADMIN: "insurance",
  INSURANCE_STAFF: "insurance",
  AGENT: "insurance",
  ADMIN: "admin",
  SYSTEM: "admin",
};

export function getPortalForRole(role: UserRole) {
  return rolePortalMap[role];
}

// Highest-precedence role first. Mirrors CLAUDE.md §5 primary-role precedence.
const rolePrecedence: UserRole[] = [
  "ADMIN",
  "SYSTEM",
  "HOSPITAL_ADMIN",
  "HOSPITAL_STAFF",
  "INSURANCE_ADMIN",
  "INSURANCE_STAFF",
  "AGENT",
  "DOCTOR",
  "PATIENT",
];

const knownRoles = new Set<string>(rolePrecedence);

// Backend `/iam/me` returns roles as a string[] (optionally "ROLE_"-prefixed).
// Pick the single highest-precedence role the frontend understands.
export function getPrimaryRole(roles: string[] | undefined | null): UserRole {
  const normalized = (roles ?? [])
    .map((r) => r?.toUpperCase().replace(/^ROLE_/, ""))
    .filter((r): r is UserRole => knownRoles.has(r));

  for (const role of rolePrecedence) {
    if (normalized.includes(role)) return role;
  }
  // Safe default for an authenticated user with no recognised role.
  return "PATIENT";
}
