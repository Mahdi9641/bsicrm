"use client";
import {Can} from "@/components/Permissions";
import { Badge, Table } from "@/components/ui";
import { fa, labels, money } from "@/lib/api";
import { ArrowUpLeft, Gift } from "lucide-react";
import { segmentColors } from "./shared";
import type { WorkspaceContext } from "./types";
export default function RfmPage({
  setQ,
  tab,
  filtered,
  rows,
  manager,
  q,
  setDialog,
  openProfile,
}: WorkspaceContext) {
  return (
    <>
      <div className="loyalty-banner">
        <div>
          <span className="eyebrow">رابطه‌ای فراتر از یک مراجعه</span>
          <h2>
            {tab === "rfm"
              ? "مشتریانتان را بهتر بشناسید."
              : "تجربه خوب، وفاداری می‌سازد."}
          </h2>
          <p>
            {tab === "rfm"
              ? "تحلیل بر اساس تازگی مراجعه، تعداد مراجعات و ارزش خرید واقعی."
              : "امتیاز خرید پس از تسویه کامل فاکتور به باشگاه اضافه می‌شود."}
          </p>
        </div>
        <Gift size={76} strokeWidth={1} />
      </div>
      {tab === "rfm" && (
        <div className="segment-cards">
          {Object.keys(segmentColors).map((s) => (
            <button
              key={s}
              style={{ borderTopColor: segmentColors[s] }}
              onClick={() => setQ(q === s ? "" : s)}
            >
              <span>{labels[s]}</span>
              <strong>{fa(rows.filter((r) => r.segment === s).length)}</strong>
              <small>
                مشاهده مشتریان
                <ArrowUpLeft size={14} />
              </small>
            </button>
          ))}
        </div>
      )}
      <section className="panel">
        <Table
          rows={filtered}
          columns={
            tab === "rfm"
              ? [
                  { key: "name", title: "مشتری" },
                  {
                    key: "recency",
                    title: "روز از آخرین مراجعه",
                    render: (r) =>
                      r.recency == null ? "بدون مراجعه" : fa(r.recency),
                  },
                  {
                    key: "frequency",
                    title: "تعداد مراجعه",
                    render: (r) => fa(r.frequency),
                  },
                  {
                    key: "monetary",
                    title: "ارزش خرید",
                    render: (r) => money(r.monetary),
                  },
                  {
                    key: "segment",
                    title: "خوشه",
                    render: (r) => <Badge value={r.segment} />,
                  },
                ]
              : [
                  { key: "name", title: "مشتری" },
                  {
                    key: "points",
                    title: "امتیاز",
                    render: (r) => <b className="gold-text">{fa(r.points)}</b>,
                  },
                  {
                    key: "tier",
                    title: "سطح باشگاه",
                    render: (r) => <Badge value={r.tier} />,
                  },
                ]
          }
          actions={(r) => (
            <>
              {tab === "loyalty" && manager ? (
                <button
                  className="table-button"
                  onClick={() => setDialog({ type: "points", row: r })}
                >
                  ثبت پاداش / مصرف
                </button>
              ) : (
                <Can permission="customers.read"><button
                  className="table-button"
                  onClick={() => openProfile(r.id)}
                >
                  پرونده مشتری
                </button></Can>
              )}
            </>
          )}
        />
      </section>
    </>
  );
}
