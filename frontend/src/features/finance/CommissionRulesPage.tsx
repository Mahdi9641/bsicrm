"use client";
import type { Lookups } from "@/components/BusinessDialogs";
import { Editor, field, Loading, Table } from "@/components/ui";
import { api, date, money, Row } from "@/lib/api";
import { todayIso } from "@/lib/dates";
import { useEffect, useState } from "react";
export default function CommissionRulesPage({
  lookups,
  manager,
}: {
  lookups: Lookups;
  manager: boolean;
}) {
  const [rows, setRows] = useState<Row[] | null>(null),
    [edit, setEdit] = useState(false),
    [error, setError] = useState("");
  const load = () =>
    api("/commission-rules")
      .then(setRows)
      .catch((e) => setError(e.message));
  useEffect(() => {
    load();
  }, []);
  return (
    <section className="panel">
      <div className="panel-head">
        <div>
          <h2>قواعد کمیسیون</h2>
          <p>
            مبنای محاسبه مبلغ خالص هر ردیف پس از تخفیف است. پلکان روی همان ردیف
            اعمال می‌شود.
          </p>
        </div>
        {manager && (
          <button className="primary" onClick={() => setEdit(true)}>
            قاعده جدید
          </button>
        )}
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
            { key: "name", title: "قاعده" },
            { key: "staff_name", title: "متخصص" },
            { key: "service_name", title: "خدمت" },
            {
              key: "kind",
              title: "نوع",
              render: (r) =>
                ({
                  PERCENT: "درصدی",
                  FIXED: "ثابت به‌ازای واحد",
                  TIERED: "پلکانی ردیف",
                })[r.kind as "PERCENT"],
            },
            { key: "rate", title: "درصد پایه" },
            {
              key: "fixed_amount",
              title: "مبلغ ثابت",
              render: (r) => money(r.fixed_amount),
            },
            {
              key: "valid_to",
              title: "پایان اعتبار",
              render: (r) => date(r.valid_to),
            },
            {
              key: "active",
              title: "وضعیت",
              render: (r) => (r.active ? "فعال" : "متوقف"),
            },
          ]}
          actions={(r) =>
            manager && r.active ? (
              <button
                className="text-button danger"
                onClick={async () => {
                  try {
                    await api(
                      "/commission-rules/" + r.id + "/deactivate",
                      "POST",
                    );
                    await load();
                  } catch (e) {
                    setError((e as Error).message);
                  }
                }}
              >
                توقف برای فروش‌های بعدی
              </button>
            ) : null
          }
        />
      ) : (
        <Loading />
      )}
      {edit && (
        <Editor
          title="قاعده کمیسیون تاریخ‌دار"
          initial={{
            kind: "PERCENT",
            rate: 30,
            fixedAmount: 0,
            threshold: 0,
            upperRate: 0,
            validFrom: todayIso(),
            validTo: todayIso(),
          }}
          fields={[
            field("name", "عنوان", "text", true),
            field("staffId", "متخصص", "select", true, {
              options: lookups.staff.map((s) => ({
                value: s.id,
                label: s.name,
              })),
            }),
            field("serviceId", "خدمت", "select", true, {
              options: lookups.services.map((s) => ({
                value: s.id,
                label: s.name,
              })),
            }),
            field("kind", "نوع", "select", true, {
              options: [
                { value: "PERCENT", label: "درصدی" },
                { value: "FIXED", label: "ثابت" },
                { value: "TIERED", label: "پلکانی" },
              ],
            }),
            field("rate", "درصد پایه", "number", true, { min: 0, max: 100 }),
            field("fixedAmount", "مبلغ ثابت هر واحد (تومان)", "number", true, {
              min: 0,
            }),
            field("threshold", "مرز پلکان (تومان)", "number", true, { min: 0 }),
            field("upperRate", "درصد بخش بالاتر از مرز", "number", true, {
              min: 0,
              max: 100,
            }),
            field("validFrom", "شروع اعتبار", "date", true),
            field("validTo", "پایان اعتبار", "date", true),
          ]}
          onClose={() => setEdit(false)}
          submit={async (b) => {
            await api("/commission-rules", "POST", {
              ...b,
              staffId: +b.staffId,
              serviceId: +b.serviceId,
            });
            await load();
          }}
        />
      )}
    </section>
  );
}
