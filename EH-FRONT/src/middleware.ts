import { NextResponse, type NextRequest } from "next/server";
import type { UserRole } from "@/lib/auth/roles";

const portalByRole: Record<UserRole, string> = {
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

const protectedPortals = ["patient", "hospital", "insurance", "admin"];

export function middleware(request: NextRequest) {
  const firstSegment = request.nextUrl.pathname.split("/").filter(Boolean)[0];
  if (!protectedPortals.includes(firstSegment)) {
    return NextResponse.next();
  }

  const role = request.cookies.get("saglamol_role")?.value as UserRole | undefined;
  if (!role || !portalByRole[role]) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("next", request.nextUrl.pathname);
    return NextResponse.redirect(loginUrl);
  }

  const allowedPortal = portalByRole[role];
  if (allowedPortal !== firstSegment) {
    return NextResponse.redirect(new URL(`/${allowedPortal}/dashboard`, request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/patient/:path*", "/hospital/:path*", "/insurance/:path*", "/admin/:path*"],
};
