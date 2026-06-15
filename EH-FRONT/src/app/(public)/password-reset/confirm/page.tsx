"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useState, Suspense } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { isAxiosError } from "axios";
import { KeyRound, Loader2 } from "lucide-react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { FormField } from "@/components/forms/FormField";
import { Button } from "@/components/ui/Button";
import { passwordApi } from "@/lib/api/password.api";

const schema = z
  .object({
    newPassword: z.string().min(8, "Minimum 8 simvol"),
    confirmPassword: z.string().min(8, "Minimum 8 simvol"),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Şifrələr uyğun gəlmir",
    path: ["confirmPassword"],
  });

type ConfirmForm = z.infer<typeof schema>;

function ConfirmFormContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get("token") ?? "";
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    formState: { errors },
    handleSubmit,
    register,
  } = useForm<ConfirmForm>({
    defaultValues: { newPassword: "", confirmPassword: "" },
    resolver: zodResolver(schema),
  });

  async function onSubmit(data: ConfirmForm) {
    if (!token) {
      setServerError("Sıfırlama tokeni tapılmadı. Zəhmət olmasa yeni link tələb edin.");
      return;
    }

    setLoading(true);
    setServerError(null);

    try {
      await passwordApi.confirmReset(token, data.newPassword);
      router.push("/login");
    } catch (error) {
      if (isAxiosError(error)) {
        const status = error.response?.status;
        const message = error.response?.data?.message;

        if (status === 400 || status === 404) {
          setServerError(message || "Token etibarsızdır və ya vaxtı bitib. Yeni link tələb edin.");
        } else if (!error.response) {
          setServerError("Server əlçatmazdır");
        } else {
          setServerError(message || "Xəta baş verdi");
        }
      } else {
        setServerError("Gözlənilməz xəta baş verdi");
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="center-screen" style={{ minHeight: "calc(100vh - 140px)" }}>
      <form className="card form" onSubmit={handleSubmit(onSubmit)} style={{ width: "min(100%, 460px)" }}>
        <div>
          <p className="eyebrow">Şifrə bərpası</p>
          <h1>Yeni şifrə</h1>
          <p>Yeni şifrənizi təyin edin.</p>
        </div>

        {!token && (
          <div className="card" style={{ padding: "0.75rem 1rem", borderLeft: "3px solid var(--color-warning, #d69e2e)" }}>
            <p style={{ color: "var(--color-warning, #d69e2e)", margin: 0 }}>
              Token tapılmadı. <Link href="/password-reset/request">Yeni link tələb edin</Link>.
            </p>
          </div>
        )}

        {serverError && (
          <div className="card" style={{ padding: "0.75rem 1rem", borderLeft: "3px solid var(--color-danger, #e53e3e)" }}>
            <p style={{ color: "var(--color-danger, #e53e3e)", margin: 0 }}>{serverError}</p>
          </div>
        )}

        <FormField error={errors.newPassword} label="Yeni şifrə" type="password" {...register("newPassword")} />
        <FormField error={errors.confirmPassword} label="Şifrə təkrarı" type="password" {...register("confirmPassword")} />

        <div className="between">
          <Link className="muted" href="/login">Login</Link>
          <Button disabled={loading || !token} type="submit">
            {loading ? <Loader2 className="spin" size={18} /> : <KeyRound size={18} />}
            {loading ? "Yenilənir..." : "Yenilə"}
          </Button>
        </div>
      </form>
    </section>
  );
}

export default function PasswordResetConfirmPage() {
  return (
    <Suspense fallback={
      <section className="center-screen" style={{ minHeight: "calc(100vh - 140px)" }}>
        <div className="card form" style={{ width: "min(100%, 460px)", textAlign: "center" }}>
          <Loader2 className="spin" size={32} />
          <p className="muted">Yüklənir...</p>
        </div>
      </section>
    }>
      <ConfirmFormContent />
    </Suspense>
  );
}
