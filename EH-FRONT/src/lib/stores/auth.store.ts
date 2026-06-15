"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { UserRole } from "@/lib/auth/roles";

export type AuthUser = {
  email: string;
  role: UserRole;
  userId?: string;
  fullName?: string;
};

type AuthState = {
  accessToken?: string;
  hydrated: boolean;
  refreshToken?: string;
  user?: AuthUser;
  logout: () => void;
  setHydrated: (hydrated: boolean) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  setUser: (user: AuthUser) => void;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      hydrated: false,
      logout: () => set({ accessToken: undefined, refreshToken: undefined, user: undefined }),
      setHydrated: (hydrated) => set({ hydrated }),
      setTokens: (accessToken, refreshToken) => set({ accessToken, refreshToken }),
      setUser: (user) => set({ user }),
    }),
    {
      name: "saglamol-auth",
      onRehydrateStorage: () => (state) => {
        state?.setHydrated(true);
      },
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
      }),
    },
  ),
);
