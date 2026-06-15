import Image from "next/image";
import Link from "next/link";
import { ClipboardCheck, LogIn } from "lucide-react";
import { Button } from "@/components/ui/Button";

export function PublicHeader() {
  return (
    <header className="public-header">
      <Link className="brand" href="/">
        <Image alt="SaglamOL" height={36} src="/logo-mark.png" width={36} />
        <span>SaglamOL</span>
      </Link>
      <nav className="public-nav" aria-label="Public navigation">
        <Button asChild variant="secondary">
          <Link href="/login">
            <LogIn size={18} />
            Login
          </Link>
        </Button>
        <Button asChild>
          <Link href="/apply">
            <ClipboardCheck size={18} />
            Muraciet et
          </Link>
        </Button>
      </nav>
    </header>
  );
}
