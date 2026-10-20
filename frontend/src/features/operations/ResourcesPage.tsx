"use client";
import { Editor, field, Loading, Table } from "@/components/ui";
import { api, Row } from "@/lib/api";
import { useEffect, useState } from "react";
export default function ResourcesPage() {
  const [rows, setRows] = useState<Row[] | null>(null),
    [edit, setEdit] = useState<Row | null>(null),
    [error, setError] = useState("");
  const load = () =>
    api("/resources")
      .then(setRows)
      .catch((e) => setError(e.message));
  useEffect(() => {
    load();
  }, []);
  return (
    <section className="panel">
      <div className="panel-head">
        <div>
          <h2>فضا و تجهیزات سالن</h2>
          <p>هر صندلی، اتاق یا دستگاه یک منبع مستقل با ظرفیت یک نوبت است.</p>
        </div>
        <button
          className="primary"
          onClick={() => setEdit({ active: true, kind: "CHAIR" })}
        >
          منبع جدید
        </button>
      </div>
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
      {rows ? (
        <Table
          rows={rows}
          columns={[
            { key: "name", title: "نام" },
            {
              key: "kind",
              title: "نوع",
              render: (r) =>
                ({ ROOM: "اتاق", CHAIR: "صندلی", EQUIPMENT: "تجهیزات" })[
                  r.kind as "ROOM"
                ],
            },
            {
              key: "active",
              title: "وضعیت",
              render: (r) => (r.active ? "فعال" : "غیرفعال"),
            },
          ]}
          actions={(r) => (
            <button className="text-button" onClick={() => setEdit(r)}>
              ویرایش
            </button>
          )}
        />
      ) : (
        <Loading />
      )}
      {edit && (
        <Editor
          title="تنظیم منبع مشترک"
          initial={edit}
          fields={[
            field("name", "نام", "text", true),
            field("kind", "نوع", "select", true, {
              options: [
                { value: "ROOM", label: "اتاق" },
                { value: "CHAIR", label: "صندلی" },
                { value: "EQUIPMENT", label: "تجهیزات" },
              ],
            }),
            field("active", "فعال", "checkbox"),
          ]}
          onClose={() => setEdit(null)}
          submit={async (b) => {
            await api(
              "/resources" + (edit.id ? "/" + edit.id : ""),
              edit.id ? "PUT" : "POST",
              b,
            );
            await load();
          }}
        />
      )}
    </section>
  );
}
