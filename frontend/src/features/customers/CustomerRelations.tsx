"use client";
import { useState } from "react";
import { api, date, Row } from "@/lib/api";
import { Editor, field, Table } from "@/components/ui";
export default function CustomerRelations({
  customerId,
  initial,
  canEdit,
}: {
  customerId: number;
  initial: Row[];
  canEdit: boolean;
}) {
  const [rows, setRows] = useState(initial),
    [editing, setEditing] = useState(false),
    [error, setError] = useState("");
  const reload = async () =>
    setRows((await api("/customers/" + customerId)).relations);
  return (
    <section>
      <div className="panel-head">
        <h3>اعضای خانواده و ارتباطات</h3>
        {canEdit && (
          <button className="secondary" onClick={() => setEditing(true)}>
            ثبت عضو خانواده
          </button>
        )}
      </div>
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
      <Table
        rows={rows}
        columns={[
          { key: "full_name", title: "نام" },
          {
            key: "relationship",
            title: "نسبت",
            render: (r) =>
              ({ SPOUSE: "همسر", CHILD: "فرزند", OTHER: "سایر" })[
                r.relationship as "SPOUSE"
              ],
          },
          {
            key: "birth_date",
            title: "تولد",
            render: (r) => date(r.birth_date),
          },
        ]}
        actions={
          canEdit
            ? (r) => (
                <button
                  className="text-button"
                  onClick={async () => {
                    if (!confirm("این ارتباط از پرونده حذف شود؟")) return;
                    try {
                      await api(
                        `/customers/${customerId}/relations/${r.id}`,
                        "DELETE",
                      );
                      await reload();
                    } catch (e) {
                      setError((e as Error).message);
                    }
                  }}
                >
                  حذف
                </button>
              )
            : undefined
        }
      />
      {editing && (
        <Editor
          title="عضو خانواده"
          initial={{ relationship: "CHILD" }}
          fields={[
            field("fullName", "نام و نام خانوادگی"),
            field("relationship", "نسبت", "select", true, {
              options: [
                { value: "SPOUSE", label: "همسر" },
                { value: "CHILD", label: "فرزند" },
                { value: "OTHER", label: "سایر" },
              ],
            }),
            field("birthDate", "تاریخ تولد", "date", false),
            field("notes", "یادداشت", "textarea", false, { max: 500 }),
          ]}
          onClose={() => setEditing(false)}
          submit={async (b) => {
            await api(`/customers/${customerId}/relations`, "POST", {
              ...b,
              birthDate: b.birthDate || null,
            });
            await reload();
          }}
        />
      )}
    </section>
  );
}
