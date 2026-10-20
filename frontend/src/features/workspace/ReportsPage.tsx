"use client";
import JalaliInput from "@/components/JalaliInput";
import { Table } from "@/components/ui";
import { money } from "@/lib/api";
import { Download } from "lucide-react";
import { exportCsv } from "./shared";
import type { WorkspaceContext } from "./types";
import { PanelHead } from "./widgets";
export default function ReportsPage({
  data,
  rows,
  from,
  to,
  setFrom,
  setTo,
}: WorkspaceContext) {
  return (
    <>
      <section className="panel report-filter">
        <label>
          <span>از تاریخ</span>
          <JalaliInput
            value={from}
            onChange={(v) => {
              if (v) setFrom(v);
            }}
          />
        </label>
        <label>
          <span>تا تاریخ</span>
          <JalaliInput
            value={to}
            onChange={(v) => {
              if (v) setTo(v);
            }}
          />
        </label>
        <button
          className="secondary"
          onClick={() => exportCsv(data.services, "BsiCRM-service-report")}
        >
          <Download size={16} />
          خروجی CSV
        </button>
      </section>
      <div className="info">{data.note}</div>
      {["services", "customers"].map((key) => (
        <section className="panel section-space" key={key}>
          <PanelHead
            title={
              key === "services" ? "سودآوری خدمات و محصولات" : "سودآوری مشتریان"
            }
          />
          <Table
            rows={data[key]}
            columns={[
              { key: "name", title: "عنوان" },
              {
                key: "revenue",
                title: "فروش خالص",
                render: (r) => money(r.revenue),
              },
              {
                key: "material_cost",
                title: "هزینه مستقیم",
                render: (r) => money(r.material_cost),
              },
              {
                key: "commission",
                title: "کمیسیون",
                render: (r) => money(r.commission),
              },
              {
                key: "contribution",
                title: "سود مشارکتی",
                render: (r) => (
                  <b className={r.contribution >= 0 ? "teal-text" : "danger"}>
                    {money(r.contribution)}
                  </b>
                ),
              },
            ]}
          />
        </section>
      ))}
    </>
  );
}
