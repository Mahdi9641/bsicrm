"use client";
import { Table } from "@/components/ui";
import { fa, money } from "@/lib/api";
import { Pencil } from "lucide-react";
import type { WorkspaceContext } from "./types";
export default function ProductsPage({
  filtered,
  rows,
  manager,
  setDialog,
}: WorkspaceContext) {
  return (
    <section className="panel">
      <Table
        rows={filtered}
        columns={[
          { key: "name", title: "محصول" },
          { key: "category", title: "دسته‌بندی" },
          {
            key: "price",
            title: "قیمت فروش",
            render: (r) => money(r.price),
          },
          {
            key: "stock",
            title: "موجودی",
            render: (r) => (
              <span
                className={
                  "badge " +
                  (r.stock <= r.minimum_stock ? "b-at_risk" : "b-paid")
                }
              >
                {fa(r.stock)} عدد
              </span>
            ),
          },
          { key: "supplier", title: "تأمین‌کننده" },
        ]}
        actions={
          manager
            ? (r) => (
                <>
                  <button
                    className="table-button"
                    onClick={() => setDialog({ type: "stock", row: r })}
                  >
                    ورود / خروج
                  </button>
                  <button
                    className="icon-btn"
                    aria-label="ویرایش محصول"
                    onClick={() => setDialog({ type: "products", row: r })}
                  >
                    <Pencil size={16} />
                  </button>
                </>
              )
            : undefined
        }
      />
    </section>
  );
}
