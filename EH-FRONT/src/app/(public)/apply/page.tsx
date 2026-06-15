"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { zodResolver } from "@hookform/resolvers/zod";
import { isAxiosError } from "axios";
import { Building2, HeartPulse, Hospital, Loader2, Send, ShieldCheck } from "lucide-react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { FormField } from "@/components/forms/FormField";
import { Button } from "@/components/ui/Button";
import { iamApi } from "@/lib/api/iam.api";

const applicantTypes = [
  {
    icon: HeartPulse,
    value: "PATIENT" as const,
    title: "Pasiyent kimi",
    text: "Polislərinizi, müraciətlərinizi və ödənişlərinizi izləmək üçün şəxsi kabinet müraciəti.",
  },
  {
    icon: Hospital,
    value: "HOSPITAL" as const,
    title: "Hospital kimi",
    text: "Tibbi qeyd, sənəd və pasiyent müraciətləri ilə işləmək üçün təşkilat müraciəti.",
  },
  {
    icon: ShieldCheck,
    value: "INSURANCE" as const,
    title: "Sığorta şirkəti kimi",
    text: "Polis, müraciət yoxlaması və ödəniş proseslərini idarə etmək üçün şirkət müraciəti.",
  },
];

const schema = z.object({
  applicantType: z.enum(["PATIENT", "HOSPITAL", "INSURANCE"], {
    required_error: "Müraciət növünü seçin",
  }),
  name: z.string().min(2, "Minimum 2 simvol"),
  email: z.string().email("Email düzgün deyil"),
  phone: z
    .string()
    .min(10, "Telefon nömrəsi düzgün deyil")
    .regex(/^\+994/, "Nömrə +994 ilə başlamalıdır"),
  identifier: z.string().optional(),
  note: z.string().optional(),
  password: z.string().min(8, "Minimum 8 simvol"),
  passwordConfirm: z.string().min(8, "Minimum 8 simvol"),
}).refine((data) => data.password === data.passwordConfirm, {
  message: "Şifrələr uyğun gəlmir",
  path: ["passwordConfirm"],
});

type ApplyForm = z.infer<typeof schema>;

export default function ApplyPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const {
    formState: { errors },
    handleSubmit,
    register,
    watch,
  } = useForm<ApplyForm>({
    defaultValues: {
      applicantType: "PATIENT",
      name: "",
      email: "",
      phone: "+994",
      identifier: "",
      note: "",
      password: "",
      passwordConfirm: "",
    },
    resolver: zodResolver(schema),
  });

  const selectedType = watch("applicantType");

  async function onSubmit(data: ApplyForm) {
    setLoading(true);
    setServerError(null);

    try {
      // Backend register accepts only { email, phoneNumber?, password } — no role.
      // applicantType stays a UX hint; actual role assignment is a backend/admin step.
      await iamApi.register({
        email: data.email,
        phoneNumber: data.phone,
        password: data.password,
      });

      setSuccess(true);
      setTimeout(() => router.push("/login"), 3000);
    } catch (error) {
      if (isAxiosError(error)) {
        const status = error.response?.status;
        const message = error.response?.data?.message;

        if (status === 409) {
          setServerError("Bu email artıq qeydiyyatdan keçib");
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

  if (success) {
    return (
      <section className="section-band">
        <div className="section-inner">
          <div className="card" style={{ maxWidth: 600, margin: "0 auto", textAlign: "center", padding: "3rem 2rem" }}>
            <div className="stat-icon" style={{ margin: "0 auto 1rem" }}>
              <ShieldCheck size={24} />
            </div>
            <h2>Müraciətiniz qəbul edildi</h2>
            <p>Məlumatlarınız yoxlanıldıqdan sonra hesabınız uyğun kabinetlə əlaqələndiriləcək.</p>
            <p className="muted">Login səhifəsinə yönləndirilirsiniz...</p>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section className="section-band">
      <div className="section-inner">
        <div className="section-heading">
          <p className="eyebrow">Müraciət</p>
          <h1>Əvvəlcə kim olaraq istifadə edəcəyinizi müəyyənləşdirin</h1>
          <p>
            SaglamOL-da hər hesab konkret bir növlə bağlanır. Müraciət təsdiqlənddikdən sonra sizə uyğun kabinet
            aktivləşdirilir.
          </p>
        </div>

        <div className="grid-3">
          {applicantTypes.map((type) => {
            const Icon = type.icon;
            const isSelected = selectedType === type.value;
            return (
              <label
                className={`card audience-card${isSelected ? " is-active" : ""}`}
                key={type.value}
                style={{ cursor: "pointer", outline: isSelected ? "2px solid var(--color-primary, #3b82f6)" : "none" }}
              >
                <input type="radio" value={type.value} {...register("applicantType")} style={{ display: "none" }} />
                <div className="stat-icon">
                  <Icon size={20} />
                </div>
                <h3>{type.title}</h3>
                <p>{type.text}</p>
              </label>
            );
          })}
        </div>

        <form className="card form" onSubmit={handleSubmit(onSubmit)} style={{ maxWidth: 760 }}>
          <div>
            <h2>Müraciət formu</h2>
            <p>Məlumatlar yoxlanıldıqdan sonra hesabınız uyğun kabinetlə əlaqələndirilir.</p>
          </div>

          {serverError && (
            <div className="card" style={{ padding: "0.75rem 1rem", borderLeft: "3px solid var(--color-danger, #e53e3e)" }}>
              <p style={{ color: "var(--color-danger, #e53e3e)", margin: 0 }}>{serverError}</p>
            </div>
          )}

          <div className="grid-2">
            <FormField error={errors.name} label="Ad Soyad / Təşkilat adı" {...register("name")} />
            <FormField error={errors.email} label="Email" type="email" {...register("email")} />
          </div>
          <div className="grid-2">
            <FormField error={errors.phone} label="Telefon" {...register("phone")} />
            <FormField error={errors.identifier} label="FIN / VÖEN" {...register("identifier")} />
          </div>
          <div className="grid-2">
            <FormField error={errors.password} label="Şifrə" type="password" {...register("password")} />
            <FormField error={errors.passwordConfirm} label="Şifrə təkrarı" type="password" {...register("passwordConfirm")} />
          </div>
          <FormField error={errors.note} label="Qısa qeyd" {...register("note")} />

          <div className="between">
            <Link className="muted" href="/login">
              Artıq hesabınız var?
            </Link>
            <Button disabled={loading} type="submit">
              {loading ? <Loader2 className="spin" size={18} /> : <Send size={18} />}
              {loading ? "Göndərilir..." : "Müraciət göndər"}
            </Button>
          </div>
        </form>

        <section className="card">
          <div className="between">
            <span className="cluster">
              <Building2 size={20} />
              <strong>Admin panel public müraciət üçün deyil</strong>
            </span>
          </div>
          <p>
            İdarəetmə panelinə yalnız sistem tərəfindən yaradılan daxili hesablar daxil ola bilir. Hospital, sığorta və
            pasiyentlər isə öz müraciət növünə uyğun kabinetdən istifadə edir.
          </p>
        </section>
      </div>
    </section>
  );
}
