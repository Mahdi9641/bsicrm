"use client";
import { Badge, Empty } from "@/components/ui";
import { date, Row } from "@/lib/api";
import { Plus, Receipt } from "lucide-react";
import { avatar } from "./shared";
import type { WorkspaceContext } from "./types";
export default function VisitsPage({
  filtered,
  operations,
  busyAction,
  setDialog,
  action,
}: WorkspaceContext) {
  return (
    <div className="visit-grid">
      {!filtered.length && <Empty />}
      {filtered.map((v) => (
        <section className="panel visit-card" key={v.id}>
          <div className="profile-heading">
            <div className="person">
              <span className="avatar">{avatar(v.customer_name)}</span>
              <div>
                <h3>{v.customer_name}</h3>
                <small>ورود {date(v.check_in_at, true)}</small>
              </div>
            </div>
            <Badge value={v.status} />
          </div>
          <div className="visit-items">
            {v.items.map((i: Row) => (
              <div key={i.id}>
                <div>
                  <b>{i.service_name}</b>
                  <small>{i.staff_name}</small>
                </div>
                <Badge value={i.status} />
                {["PLANNED", "IN_SERVICE"].includes(i.status) && (
                  <button
                    className="table-button"
                    disabled={busyAction}
                    onClick={() =>
                      action("/visit-services/" + i.id + "/status", {
                        status:
                          i.status === "PLANNED" ? "IN_SERVICE" : "COMPLETED",
                      })
                    }
                  >
                    {i.status === "PLANNED" ? "شروع" : "تکمیل"}
                  </button>
                )}
              </div>
            ))}
          </div>
          {operations && (
            <div className="card-actions">
              {["WAITING", "IN_SERVICE"].includes(v.status) && (
                <>
                  <button
                    className="secondary"
                    onClick={() => setDialog({ type: "visitService", row: v })}
                  >
                    <Plus size={15} />
                    خدمت
                  </button>
                  <button
                    className="primary"
                    disabled={busyAction}
                    onClick={() => action("/visits/" + v.id + "/complete")}
                  >
                    پایان مراجعه
                  </button>
                </>
              )}
              {v.status === "COMPLETED" && !v.invoice_id && (
                <button
                  className="primary"
                  onClick={() => setDialog({ type: "invoice", visit: v })}
                >
                  <Receipt size={16} />
                  صدور فاکتور
                </button>
              )}
              {v.invoice_id && (
                <button
                  className="secondary"
                  onClick={() =>
                    setDialog({ type: "invoiceDetail", id: v.invoice_id })
                  }
                >
                  مشاهده فاکتور
                </button>
              )}
            </div>
          )}
        </section>
      ))}
    </div>
  );
}
