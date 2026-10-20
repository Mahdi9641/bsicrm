"use client";
import { Editor, field } from "@/components/ui";
import { api, Row } from "@/lib/api";
import { useEffect, useState } from "react";
export default function TemplatesPanel() {
  const [rows, setRows] = useState<Row[]>([]),
    [edit, setEdit] = useState<Row | null>(null),
    [error, setError] = useState("");
  const load = () =>
    api("/message-templates")
      .then(setRows)
      .catch((e) => setError(e.message));
  useEffect(() => {
    load();
  }, []);
  const title: Record<string, string> = {
    BOOKING: "ثبت و جابه‌جایی نوبت",
    REMINDER: "یادآوری نوبت",
    BIRTHDAY: "تبریک تولد",
    FOLLOWUP: "پیگیری پس از خدمت",
  };
  return (
    <section className="automation-panel">
      <div className="panel-head">
        <div>
          <h2>ارتباط در زمان مناسب</h2>
          <p>
            قالب‌ها را فعال کنید؛ اجرای خودکار به فعال بودن زمان‌بندی سرور و
            ارسال به تنظیم پنل پیامک وابسته است.
          </p>
        </div>
      </div>
      {error && <p className="error">{error}</p>}
      <div className="automation-grid">
        {rows.map((r) => (
          <button
            className="automation-card"
            key={r.id}
            onClick={() => setEdit(r)}
          >
            <span className={r.active ? "subtle-chip" : "muted"}>
              {r.active ? "فعال" : "غیرفعال"}
            </span>
            <h3>{title[r.kind]}</h3>
            <p>{r.body}</p>
            <small>ویرایش قالب و زمان‌بندی ←</small>
          </button>
        ))}
      </div>
      {edit && (
        <Editor
          title={title[edit.kind]}
          initial={{ ...edit, delayHours: edit.delay_hours }}
          fields={[
            field("body", "متن پیام", "textarea", true, {
              max: 800,
              help: "متغیرهای مجاز: {name}، {date}، {organization}",
            }),
            field("active", "فعال", "checkbox"),
            field(
              "delayHours",
              edit.kind === "REMINDER"
                ? "چند ساعت پیش از نوبت"
                : "چند ساعت پس از خدمت (برای پیگیری)",
              "number",
              true,
              { min: 1, max: 168 },
            ),
          ]}
          onClose={() => setEdit(null)}
          submit={async (b) => {
            await api("/message-templates/" + edit.id, "PUT", {
              ...b,
              version: edit.version,
            });
            await load();
          }}
        />
      )}
    </section>
  );
}
