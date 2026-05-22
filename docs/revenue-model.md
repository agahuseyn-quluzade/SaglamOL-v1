# SaglamOL Revenue Model

Bu sənəd SaglamOL platformasında əsas tərəflər üçün gəlir və dəyər modelini
MVP baxımından dəqiqləşdirir. İlk versiyada məqsəd ödəniş sistemini mürəkkəb
etmək deyil, gəlir axınlarının məhsul dizaynına necə bağlandığını aydın
saxlamaqdır.

## Tərəflər

| Tərəf | Platformadan aldığı dəyər | Potensial gəlir/ödəniş modeli |
| --- | --- | --- |
| Patient | Sığorta planlarını görür, policy alır, claim yaradır, status izləyir | B2C premium ödənişi, əlavə xidmət paketi, document fast-track fee |
| Insurance company | Policy, claim, risk və fraud proseslərini avtomatlaşdırır | SaaS subscription, per-claim fee, AI analysis usage fee |
| Doctor / Hospital | Tibbi record və sənədləri rəqəmsal ötürür, sığorta təsdiqini sürətləndirir | Partner portal subscription, verified provider listing, integration fee |
| Agent | Claim review işini azaldır, AI/Fraud nəticəsi ilə daha tez qərar verir | Daxili istifadəçidir; platforma agent məhsuldarlığını artırmaqla şirkət üçün qənaət yaradır |
| Admin / Platform owner | Plan, qayda, istifadəçi, audit və monitoring idarə edir | SaaS lisenziya, enterprise setup, support və maintenance fee |
| AI provider | Risk scoring/explanation üçün API verir | Platforma tərəfindən usage-based API xərci kimi ödənilir |
| Notification provider | Email/SMS göndərir | Platforma tərəfindən per-message xərc kimi ödənilir |
| Payment provider | Premium və payout əməliyyatlarını aparır | Transaction commission və gateway fee |
| Payment Service | Premium, payout və refund əməliyyatlarını platformada izləyir | Birbaşa gəlir yaratmır; billing, audit və provider reconciliation üçün baza yaradır |

## MVP Üçün Əsas Gəlir Modeli

MVP-də ən real model B2B SaaS modelidir. Platforma sığorta şirkətinə satılır,
patient və doctor isə sistemin istifadəçiləri olur.

1. Sığorta şirkəti aylıq platforma haqqı ödəyir.
2. Hər claim üçün əlavə processing fee hesablana bilər.
3. AI risk analizi üçün usage-based fee ayrıca ölçülür.
4. Notification, AI və payment provider xərcləri platformanın cost modelinə
   yazılır.

Bu yanaşma MVP üçün daha sadədir, çünki patient-dən birbaşa ödəniş almaq üçün
payment, invoice, refund və hüquqi proseslər erkən mərhələdə ağırlaşır.

## Gəlir Axınları

| Model | Kim ödəyir? | Nəyə görə ödəyir? | MVP statusu |
| --- | --- | --- | --- |
| SaaS subscription | Insurance company | Platformadan aylıq istifadə | İlk real model |
| Per-claim processing | Insurance company | Hər claim submission/review üçün | MVP-də ölçülür, billing sonra |
| AI usage fee | Insurance company | Risk score və explanation üçün | MVP-də loglanır |
| Provider portal fee | Hospital/clinic | Verified provider paneli və API inteqrasiyası | Sonrakı mərhələ |
| Patient premium | Patient/employer | Sığorta policy-si üçün | Biznes domain-də var, real payment sonra |
| Fast review fee | Patient/company | Claim-in prioritet review olunması | Sonrakı mərhələ |
| Enterprise setup | Insurance company | Onboarding, custom rule, migration | Sonrakı mərhələ |
| Analytics package | Insurance company | Fraud/risk dashboard və report-lar | Sonrakı mərhələ |

## Xərc Modeli

| Xərc | İzah | Nəzarət mexanizmi |
| --- | --- | --- |
| AI API cost | Risk scoring üçün external AI çağırışları | `AiRequestLog`, timeout, retry limit, monthly cap |
| SMS/email cost | Notification göndərişləri | Template limit, retry policy |
| Storage cost | Medical document-lər MinIO/S3-də saxlanır | File size limit, retention policy |
| Infrastructure cost | DB, Kafka, Redis, monitoring | Service-level scaling |
| Support cost | Enterprise müştəri dəstəyi | SLA planları |

## MVP-də Billing Necə Saxlanılmalıdır

MVP-də real pul çıxışı əvəzinə ölçülə bilən usage event-lər saxlanılmalıdır:

- `ClaimSubmitted`
- `ClaimReviewed`
- `AiRiskAnalyzed`
- `FraudChecked`
- `NotificationSent`
- `DocumentUploaded`
- `IamUserRegistered`
- `PaymentCompleted`
- `PayoutRequested`

Sonra bu event-lər əsasında invoice/billing modulu əlavə edilə bilər.

## Tövsiyə Olunan İlk Qiymətləndirmə Məntiqi

İlk demo üçün sadə B2B paketlər:

| Paket | Kim üçün | Limit | Gəlir məntiqi |
| --- | --- | --- | --- |
| Starter | Kiçik sığorta komandası | Aylıq claim limiti, basic AI | Fixed monthly fee |
| Business | Orta sığorta şirkəti | Daha çox claim, fraud rules, reports | Monthly fee + overage |
| Enterprise | Böyük şirkət | Custom rules, SLA, integrations | Custom contract |

Bu paketlər kodda hələ billing kimi implement edilməməlidir. Əvvəlcə usage
log-lar və audit məlumatları toplanmalıdır.
