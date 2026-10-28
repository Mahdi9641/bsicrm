"use client";
import {Can} from "@/components/Permissions";
import { Badge, Table } from "@/components/ui";
import TemplatesPanel from "@/features/engagement/TemplatesPanel";
import { date } from "@/lib/api";
import type { WorkspaceContext } from "./types";
export default function MessagesPage({
  filtered,
  rows,
  busyAction,
  action,
}: WorkspaceContext) {
  return (
    <section className="panel">
      <TemplatesPanel />
      <Table
        rows={filtered}
        columns={[
          { key: "customer_name", title: "مشتری" },
          { key: "body", title: "متن پیام" },
          {
            key: "status",
            title: "وضعیت",
            render: (r) => <Badge value={r.status} />,
          },
          {
            key: "created_at",
            title: "تاریخ",
            render: (r) => date(r.created_at, true),
          },
          { key: "error", title: "توضیحات" },
          {
            key: "delivery_status",
            title: "تحویل",
            render: (r) =>
              ({
                UNKNOWN: "نامشخص",
                DELIVERED: "تحویل‌شده",
                UNDELIVERABLE: "تحویل نشد",
              })[r.delivery_status as "UNKNOWN"],
          },
        ]}
        actions={(r) =>
          r.status === "QUEUED" ? (
            <Can permission="messages.write"><button
              className="text-button danger"
              disabled={busyAction}
              onClick={() => action("/messages/" + r.id + "/cancel")}
            >
              لغو ارسال
            </button></Can>
          ) : null
        }
      />
    </section>
  );
}
