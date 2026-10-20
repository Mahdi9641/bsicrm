"use client";
import { Badge, Table } from "@/components/ui";
import { fa, money } from "@/lib/api";
import type { WorkspaceContext } from "./types";
export default function CommissionsPage({
  filtered,
  rows,
  finance,
  busyAction,
  setDialog,
  action,
}: WorkspaceContext) {
  return (
    <section className="panel">
      <div className="info">
        مبنای محاسبه: مبلغ خدمت پس از تخفیف. نرخ هر ردیف در زمان صدور فاکتور ثبت
        می‌شود.
      </div>
      <Table
        rows={filtered}
        columns={[
          { key: "staff_name", title: "متخصص" },
          { key: "description", title: "خدمت" },
          { key: "invoice_id", title: "شماره فاکتور" },
          {
            key: "basis",
            title: "مبنای محاسبه",
            render: (r) => money(r.basis),
          },
          { key: "rate", title: "نرخ", render: (r) => fa(r.rate) + "٪" },
          {
            key: "amount",
            title: "کمیسیون",
            render: (r) => money(r.amount),
          },
          {
            key: "status",
            title: "وضعیت",
            render: (r) => <Badge value={r.status} />,
          },
        ]}
        actions={
          finance
            ? (r) => (
                <>
                  {r.status === "CALCULATED" && (
                    <button
                      className="table-button"
                      disabled={busyAction}
                      onClick={() =>
                        action("/commissions/" + r.id + "/status", {
                          status: "APPROVED",
                        })
                      }
                    >
                      تأیید کمیسیون
                    </button>
                  )}
                  {r.status === "APPROVED" && (
                    <button
                      className="table-button"
                      onClick={() => setDialog({ type: "settlement", row: r })}
                    >
                      تسویه
                    </button>
                  )}
                </>
              )
            : undefined
        }
      />
    </section>
  );
}
