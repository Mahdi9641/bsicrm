"use client";
import { Badge, Table } from "@/components/ui";
import TreasuryPanel from "@/features/finance/TreasuryPanel";
import { date, fa, money, Row } from "@/lib/api";
import { Wallet } from "lucide-react";
import type { WorkspaceContext } from "./types";
import { PanelHead, Stat } from "./widgets";
export default function FinancePage({ data, rows, done }: WorkspaceContext) {
  return (
    <>
      <TreasuryPanel accounts={data.accounts} done={done} />
      <div className="stats-grid accounts">
        {data.accounts.map((r: Row) => (
          <Stat
            key={r.id}
            title={r.name}
            value={fa(r.balance)}
            unit="تومان"
            icon={Wallet}
            hint="مانده گردش‌های ثبت‌شده"
            color="teal"
          />
        ))}
      </div>
      <section className="panel">
        <PanelHead
          title="گردش حساب‌ها"
          subtitle="دریافت، هزینه، برگشت وجه و تسویه کمیسیون"
        />
        <Table
          rows={data.entries}
          columns={[
            {
              key: "created_at",
              title: "تاریخ",
              render: (r) => date(r.created_at, true),
            },
            {
              key: "kind",
              title: "نوع تراکنش",
              render: (r) => <Badge value={r.kind} />,
            },
            { key: "account_name", title: "حساب" },
            {
              key: "amount",
              title: "مبلغ",
              render: (r) => (
                <b className={r.amount >= 0 ? "teal-text" : "danger"}>
                  {money(r.amount)}
                </b>
              ),
            },
          ]}
        />
      </section>
      <section className="panel section-space">
        <PanelHead title="هزینه‌های ثبت‌شده" />
        <Table
          rows={data.expenses}
          columns={[
            { key: "title", title: "شرح" },
            { key: "category", title: "دسته‌بندی" },
            {
              key: "amount",
              title: "مبلغ",
              render: (r) => money(r.amount),
            },
            {
              key: "created_at",
              title: "تاریخ",
              render: (r) => date(r.created_at),
            },
          ]}
        />
      </section>
    </>
  );
}
