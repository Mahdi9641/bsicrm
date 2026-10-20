"use client";
import { Badge, Table } from "@/components/ui";
import { date, money } from "@/lib/api";
import type { WorkspaceContext } from "./types";
export default function InvoicesPage({
  filtered,
  rows,
  finance,
  setDialog,
}: WorkspaceContext) {
  return (
    <section className="panel">
      <Table
        rows={filtered}
        columns={[
          {
            key: "id",
            title: "فاکتور",
            render: (r) => <b dir="ltr">BS-{r.id}</b>,
          },
          { key: "customer_name", title: "مشتری" },
          {
            key: "created_at",
            title: "تاریخ",
            render: (r) => date(r.created_at),
          },
          { key: "total", title: "مبلغ کل", render: (r) => money(r.total) },
          {
            key: "balance",
            title: "مانده",
            render: (r) => money(r.status === "VOID" ? 0 : r.total - r.paid),
          },
          {
            key: "status",
            title: "وضعیت",
            render: (r) => <Badge value={r.status} />,
          },
        ]}
        actions={(r) => (
          <>
            <button
              className="table-button"
              onClick={() => setDialog({ type: "invoiceDetail", id: r.id })}
            >
              جزئیات و پرداخت
            </button>
            {finance && r.status !== "VOID" && (
              <button
                className="text-button danger"
                onClick={() => setDialog({ type: "voidInvoice", row: r })}
              >
                ابطال
              </button>
            )}
          </>
        )}
      />
    </section>
  );
}
