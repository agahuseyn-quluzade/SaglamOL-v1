"use client";

import Link from "next/link";
import { useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { isAxiosError } from "axios";
import { CheckCircle2, Loader2, Mail } from "lucide-react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { FormField } from "@/components/forms/FormField";
import { Button } from "@/components/ui/Button";
import { passwordApi } from "@/lib/api/password.api";

const schema = z.object({
  email: z.string().email("Email düzgün deyil"),
});

type ResetRequestForm = z.infer<typeof schema>;

export default function PasswordResetRequestPage() {
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);
  const [sent, setSent] = useState(false);

  const {
    formState: { errors },
    handleSubmit,
    register,
  } = useForm<ResetRequestForm>({
    defaultValues: { email: "" },
    resolver: zodResolver(schema),
  });

  async function onSubmit(data: ResetRequestForm) {
    setLoading(true);
    setServerError(null);

    try {
      await passwordApi.requestReset(data.email);
      setSent(true);
    } catch (error) {
      if (isAxiosError(error)) {
        const message = error.response?.data?.message;
        if (!error.response) {
          setServerError("Server əlçatmazdır");
        } else {
          setServerError(message || "Xəta baş verdi. Yenidən cəhd edin.");
        }
      } else {
        setServerError("Gözlənilməz xəta baş verdi");
      }
    } finally {
      setLoading(false);
    }
  }

  if (sent) {
    return (
      <section className="center-screen" style={{ minHeight: "calc(100vh - 140px)" }}>
        <div className="card form" style={{ width: "min(100%, 460px)", textAlign: "center" }}>
          <CheckCircle2 size={40} style={{ color: "var(--color-success, #38a169)", margin: "0 auto" }} />
          <h2>Link göndərildi</h2>
          <p>Email-inizə şifrə sıfırlama linki göndərildi. Zəhmət olmasa poçtunuzu yoxlayın.</p>
          <Link className="muted" href="/login">Login səhifəsinə qayıt</Link>
        </div>
      </section>
    );
  }

  return (
    <section className="center-screen" style={{ minHeight: "calc(100vh - 140px)" }}>
      <form className="card form" onSubmit={handleSubmit(onSubmit)} style={{ width: "min(100%, 460px)" }}>
        <div>
          <p className="eyebrow">Şifrə bərpası</p>
          <h1>Reset linki</h1>
          <p>Email ünvanınızı daxil edin, şifrə bərpa linki göndəriləcək.</p>
        </div>

        {serverError && (
          <div className="card" style={{ padding: "0.75rem 1rem", borderLeft: "3px solid var(--color-danger, #e53e3e)" }}>
            <p style={{ color: "var(--color-danger, #e53e3e)", margin: 0 }}>{serverError}</p>
          </div>
        )}

        <FormField error={errors.email} label="Email" type="email" {...register("email")} />

        <div className="between">
          <Link className="muted" href="/login">Login</Link>
          <Button disabled={loading} type="submit">
            {loading ? <Loader2 className="spin" size={18} /> : <Mail size={18} />}
            {loading ? "Göndərilir..." : "Göndər"}
          </Button>
        </div>
      </form>
    </section>
  );
}
