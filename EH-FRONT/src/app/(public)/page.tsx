import Link from "next/link";
import {
  ArrowRight,
  Building2,
  CheckCircle2,
  ClipboardCheck,
  HeartPulse,
  Hospital,
  ShieldCheck,
  UserCog,
  Users,
} from "lucide-react";
import { Button } from "@/components/ui/Button";

const audiences = [
  {
    icon: HeartPulse,
    title: "Vetendaslar ucun",
    text: "Sigorta polisinizi, tibbi muracietlerinizi ve odenis tarixcenizi bir yerde izleyin.",
  },
  {
    icon: Hospital,
    title: "Klinika ve hospitalar ucun",
    text: "Teskilat kimi muraciet edin, tesdiqden sonra pasiyent senedleri ve tibbi qeydlerle isleyin.",
  },
  {
    icon: ShieldCheck,
    title: "Sigorta sirketleri ucun",
    text: "Sirket hesabi acmaq ucun muraciet edin, tesdiqden sonra yoxlama ve qerar proseslerini idare edin.",
  },
];

const steps = [
  "Evvelce vetendas, hospital ve ya sigorta sirketi kimi muraciet edirsiniz.",
  "Melumatlar yoxlanilir ve hesabiniz uygun is sahesine baglanir.",
  "Daxil olduqda sistem sizi yalniz oz kabinetinize yonlendirir.",
  "Muraciet, sened, qerar ve odenis merheleleri hemin kabinetde izlenir.",
];

const workspaces = [
  { title: "Pasiyent kabineti", icon: HeartPulse, text: "Polis, muraciet ve odenisler" },
  { title: "Hospital kabineti", icon: Building2, text: "Tibbi qeyd ve senedler" },
  { title: "Sigorta kabineti", icon: ShieldCheck, text: "Yoxlama ve qerarlar" },
  { title: "Idareetme paneli", icon: UserCog, text: "Yalniz daxili idareetme ucun" },
];

export default function HomePage() {
  return (
    <div className="home-main">
      <section
        className="site-hero"
        style={{
          backgroundImage:
            "linear-gradient(90deg, rgba(8, 47, 73, 0.86) 0%, rgba(8, 47, 73, 0.68) 42%, rgba(8, 47, 73, 0.18) 100%), url('/images/home-hero.png')",
        }}
      >
        <div className="site-hero-content">
          <p className="eyebrow">Reqemsal saglamliq sigortasi platformasi</p>
          <h1>SaglamOL</h1>
          <p>
            Pasiyent, hospital ve sigorta sirketleri ucun tibbi sigorta muracietlerini sade, seffaf ve izlene bilen
            formada birlesdiren onlayn xidmet.
          </p>
          <div className="cluster">
            <Button asChild>
              <Link href="/apply">
                Muraciet et
                <ArrowRight size={18} />
              </Link>
            </Button>
            <Button asChild variant="secondary">
              <Link href="/login">Hesabim var</Link>
            </Button>
          </div>
          <div className="hero-metrics" aria-label="Xidmet ustunlukleri">
            <span>
              <strong>Seffaf</strong>
              proses
            </span>
            <span>
              <strong>Az</strong>
              kagiz isi
            </span>
            <span>
              <strong>Tek</strong>
              kabinet
            </span>
          </div>
        </div>
      </section>

      <section className="section-band">
        <div className="section-inner">
          <div className="section-heading">
            <p className="eyebrow">Kimler ucundur?</p>
            <h2>Her istifadeci oz rolune uygun is sahesine daxil olur</h2>
            <p>
              Platformada evvelce muraciet novunuz mueyyenlesir. Hesab tesdiqlendikden sonra siz yalniz size aid
              kabinetde isleyirsiniz.
            </p>
          </div>
          <div className="grid-3">
            {audiences.map((item) => {
              const Icon = item.icon;
              return (
                <article className="card audience-card" key={item.title}>
                  <div className="stat-icon">
                    <Icon size={20} />
                  </div>
                  <h3>{item.title}</h3>
                  <p>{item.text}</p>
                </article>
              );
            })}
          </div>
        </div>
      </section>

      <section className="section-band section-band-alt" id="how-it-works">
        <div className="section-inner two-column-section">
          <div className="section-heading">
            <p className="eyebrow">Giris axini</p>
            <h2>Birinci muraciet, sonra size aid kabinet</h2>
            <p>
              Sigorta sirketi, hospital ve ya pasiyent kimi muraciet etdikden sonra hesabiniz hemin kateqoriyaya
              baglanir. Admin panel public muraciet ucun deyil.
            </p>
          </div>
          <div className="process-list">
            {steps.map((step, index) => (
              <div className="process-item" key={step}>
                <span>{index + 1}</span>
                <p>{step}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="section-band">
        <div className="section-inner">
          <div className="section-heading">
            <p className="eyebrow">Imkanlar</p>
            <h2>Platformaya daxil olmadan evvel ne ucun istifade olundugunu bilirsiniz</h2>
          </div>
          <div className="feature-strip">
            <div className="feature-item">
              <ClipboardCheck size={22} />
              <span>Muraciet izleme</span>
            </div>
            <div className="feature-item">
              <ShieldCheck size={22} />
              <span>Polis idareetmesi</span>
            </div>
            <div className="feature-item">
              <CheckCircle2 size={22} />
              <span>Qerar merheleleri</span>
            </div>
            <div className="feature-item">
              <Users size={22} />
              <span>Ayrilmis kabinetler</span>
            </div>
          </div>
        </div>
      </section>

      <section className="section-band section-band-alt">
        <div className="section-inner">
          <div className="section-heading">
            <p className="eyebrow">Kabinetler</p>
            <h2>Hesabiniz tesdiqlendikden sonra uygun saheye kecirsiniz</h2>
            <p>Bu saheler bir-birinden ayridir; istifadeci yalniz oz hesabina bagli kabinetde isleyir.</p>
          </div>
          <div className="grid-4">
            {workspaces.map((workspace) => {
              const Icon = workspace.icon;
              return (
                <article className="portal-entry-card" key={workspace.title}>
                  <div className="between">
                    <div className="stat-icon">
                      <Icon size={20} />
                    </div>
                    <ArrowRight size={18} />
                  </div>
                  <h3>{workspace.title}</h3>
                  <p>{workspace.text}</p>
                </article>
              );
            })}
          </div>
          <div className="cluster">
            <Button asChild>
              <Link href="/apply">Muraciet et</Link>
            </Button>
            <Button asChild variant="secondary">
              <Link href="/login">Hesaba daxil ol</Link>
            </Button>
          </div>
        </div>
      </section>
    </div>
  );
}
