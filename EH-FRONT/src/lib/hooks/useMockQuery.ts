"use client";

import { useQuery } from "@tanstack/react-query";
import type { PortalKey } from "@/lib/auth/roles";
import { buildPageModel } from "@/lib/data/mock";

export function usePortalPageQuery(portal: PortalKey, slug?: string[]) {
  return useQuery({
    queryKey: ["portal-page", portal, slug?.join("/") ?? "dashboard"],
    queryFn: async () => buildPageModel(portal, slug),
  });
}
