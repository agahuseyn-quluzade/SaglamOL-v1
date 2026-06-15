import { Upload } from "lucide-react";
import { Button } from "@/components/ui/Button";

export function DocumentUploader() {
  return (
    <section className="card">
      <div>
        <h3>Document upload</h3>
        <p>Presigned URL ilə MinIO yükləmə</p>
      </div>
      <label className="field" htmlFor="document">
        <span>Fayl</span>
        <input id="document" type="file" />
      </label>
      <Button>
        <Upload size={18} />
        Yüklə
      </Button>
    </section>
  );
}
