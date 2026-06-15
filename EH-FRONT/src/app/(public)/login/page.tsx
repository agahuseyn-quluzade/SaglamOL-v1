"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { isAxiosError } from "axios";
import { Loader2, LogIn } from "lucide-react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { FormField } from "@/components/forms/FormField";
import { Button } from "@/components/ui/Button";
import { iamApi } from "@/lib/api/iam.api";
import { getPortalForRole, getPrimaryRole } from "@/lib/auth/roles";
import { useAuthStore } from "@/lib/stores/auth.store";

const schema = z.object({
  email: z.string().email("Email düzgün deyil"),
  password: z.string().min(6, "Minimum 6 simvol"),
});

type LoginForm = z.infer<typeof schema>;

export default function LoginPage() {
  const router = useRouter();
  const setTokens = useAuthStore((state) => state.setTokens);
  const setUser = useAuthStore((state) => state.setUser);
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    formState: { errors },
    handleSubmit,
    register,
  } = useForm<LoginForm>({
    defaultValues: { email: "", password: "" },
    resolver: zodResolver(schema),
  });

  async function onSubmit(data: LoginForm) {
    setLoading(true);
    setServerError(null);

    try {
      // 1) Authenticate — backend returns only tokens (no user/role).
      const { data: tokens } = await iamApi.login(data);
      setTokens(tokens.accessToken, tokens.refreshToken);

      // 2) Resolve identity + roles from /me and derive the primary role.
      const { data: me } = await iamApi.me();
      const role = getPrimaryRole(me.roles);

      setUser({ email: me.email, role, userId: me.userId });
      document.cookie = `saglamol_role=${role}; path=/; max-age=604800; SameSite=Lax`;

      router.push(`/${getPortalForRole(role)}/dashboard`);
    } catch (error) {
      if (isAxiosError(error)) {
        const status = error.response?.status;
        const message = error.response?.data?.message;

        if (status === 401 || status === 403) {
          setServerError(message || "Email və ya şifrə yanlışdır");
        } else if (status === 400) {
          setServerError(message || "Məlumatlar düzgün deyil");
        } else if (!error.response) {
          setServerError("Server əlçatmazdır. Zəhmət olmasa bir az sonra yenidən cəhd edin.");
        } else {
          setServerError(message || "Gözlənilməz xəta baş verdi");
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
      <form className="card form" onSubmit={handleSubmit(onSubmit)} style={{ width: "min(100%, 500px)" }}>
        <div>
          <p className="eyebrow">Giriş</p>
          <h1>Login</h1>
          <p>Hesabınız hansı növlə təsdiqlənibsə, sistem sizi həmin kabinetə aparacaq.</p>
        </div>

        {serverError && (
          <div className="card" style={{ padding: "0.75rem 1rem", borderLeft: "3px solid var(--color-danger, #e53e3e)" }}>
            <p style={{ color: "var(--color-danger, #e53e3e)", margin: 0 }}>{serverError}</p>
          </div>
        )}

        <FormField error={errors.email} label="Email" {...register("email")} />
        <FormField error={errors.password} label="Şifrə" type="password" {...register("password")} />

        <div className="between">
          <Link className="muted" href="/password-reset/request">
            Şifrəni unutmusunuz?
          </Link>
          <Button disabled={loading} type="submit">
            {loading ? <Loader2 className="spin" size={18} /> : <LogIn size={18} />}
            {loading ? "Daxil olunur..." : "Login"}
          </Button>
        </div>

        <p className="muted">
          Hesabınız yoxdur? <Link href="/apply">Müraciət edin</Link>.
        </p>
      </form>
    </section>
  );
}
