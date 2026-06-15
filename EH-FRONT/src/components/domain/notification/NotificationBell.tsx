"use client";

import { Bell } from "lucide-react";
import { Button } from "@/components/ui/Button";

export function NotificationBell() {
  return (
    <Button aria-label="Bildirişlər" iconOnly title="Bildirişlər" variant="secondary">
      <Bell size={18} />
    </Button>
  );
}
