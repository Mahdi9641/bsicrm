"use client";
import { Table } from "@/components/ui";
import { date } from "@/lib/api";
import type { WorkspaceContext } from "./types";
export default function AuditPage({
  filtered,
  rows,
  action,
}: WorkspaceContext) {
  return (
    <section className="panel">
      <Table
        rows={filtered}
        columns={[
          {
            key: "created_at",
            title: "زمان",
            render: (r) => date(r.created_at, true),
          },
          { key: "user_name", title: "کاربر" },
          { key: "action", title: "عملیات" },
          { key: "entity", title: "بخش" },
          { key: "entity_id", title: "شناسه" },
          { key: "detail", title: "جزئیات" },
        ]}
      />
    </section>
  );
}
