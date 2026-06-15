"use client";

import { create } from "zustand";

type Theme = "light" | "dark";

type UiState = {
  sidebarOpen: boolean;
  theme: Theme;
  toggleSidebar: () => void;
  toggleTheme: () => void;
};

export const useUiStore = create<UiState>((set) => ({
  sidebarOpen: true,
  theme: "light",
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
  toggleTheme: () => set((state) => ({ theme: state.theme === "light" ? "dark" : "light" })),
}));
