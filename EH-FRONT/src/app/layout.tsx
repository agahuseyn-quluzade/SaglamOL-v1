import type { Metadata } from "next";
import { AppProviders } from "@/components/layout/AppProviders";
import "@/styles/variables.css";
import "@/styles/globals.css";
import "@/styles/layout.css";
import "@/styles/components.css";
import "@/styles/utilities.css";

export const metadata: Metadata = {
  title: "SaglamOL",
  description: "Health insurance operations portal",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="az">
      <body>
        <AppProviders>{children}</AppProviders>
      </body>
    </html>
  );
}
