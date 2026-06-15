import { NotificationItem } from "@/components/domain/notification/NotificationItem";

export function NotificationList() {
  return (
    <section className="card">
      <div>
        <h3>Bildirişlər</h3>
        <p>Oxunmamış sistem mesajları</p>
      </div>
      <div className="stack">
        {["Claim review tamamlandı", "Premium ödənişi alındı", "Yeni sənəd tələb edildi"].map((item) => (
          <NotificationItem key={item} title={item} />
        ))}
      </div>
    </section>
  );
}
