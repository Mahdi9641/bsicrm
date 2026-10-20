"use client";
import { Row, clock, date, fa, labels } from "@/lib/api";
import { addDays, todayIso } from "@/lib/dates";
import {
  ArrowLeftRight,
  Check,
  ChevronLeft,
  ChevronRight,
  Clock,
  LayoutGrid,
  List,
  UserRound,
} from "lucide-react";
import { useState } from "react";
import JalaliInput from "./JalaliInput";
import { Badge, Empty, Table } from "./ui";

type Props = {
  rows: Row[];
  day: string;
  onDay: (s: string) => void;
  editable: boolean;
  busy: boolean;
  onAction: (path: string, body: Row) => void;
  onReschedule: (id: number) => void;
  onCancel: (r: Row) => void;
};
export default function AppointmentsWorkspace({
  rows,
  day,
  onDay,
  editable,
  busy,
  onAction,
  onReschedule,
  onCancel,
}: Props) {
  const [status, setStatus] = useState("ALL"),
    [staff, setStaff] = useState("ALL"),
    [view, setView] = useState("agenda");
  const offset = (new Date(day + "T12:00:00Z").getUTCDay() + 1) % 7;
  const week = Array.from({ length: 7 }, (_, i) => addDays(day, i - offset));
  const specialists = Array.from(
    new Set(
      rows.flatMap((r) => (r.staff_names || "").split("، ")).filter(Boolean),
    ),
  ) as string[];
  const filtered = rows.filter(
    (r) =>
      (status === "ALL" || r.status === status) &&
      (staff === "ALL" || r.staff_names?.split("، ").includes(staff)),
  );
  const actions = (r: Row) => (
    <>
      {r.status === "BOOKED" && (
        <button
          className="table-button"
          disabled={busy}
          onClick={() =>
            onAction("/appointments/" + r.id + "/status", {
              status: "CONFIRMED",
            })
          }
        >
          <Check size={14} />
          تأیید
        </button>
      )}
      {["BOOKED", "CONFIRMED"].includes(r.status) && (
        <>
          <button
            className="primary small-button"
            disabled={busy}
            onClick={() =>
              onAction("/visits", {
                customerId: r.customer_id,
                appointmentId: r.id,
                priority: "NORMAL",
              })
            }
          >
            پذیرش
          </button>
          <button
            className="secondary small-button"
            disabled={busy}
            onClick={() => onReschedule(r.id)}
          >
            <ArrowLeftRight size={14} />
            جابه‌جایی
          </button>
          <button
            className="text-button danger"
            disabled={busy}
            onClick={() => onCancel(r)}
          >
            لغو / عدم حضور
          </button>
        </>
      )}
    </>
  );
  return (
    <>
      <section className="calendar-surface">
        <div className="calendar-top">
          <div>
            <span className="eyebrow">برنامهٔ سالن شما</span>
            <h2>{date(day + "T12:00:00+03:30")}</h2>
            <p>
              {fa(
                rows.filter((r) => !["CANCELLED", "NO_SHOW"].includes(r.status))
                  .length,
              )}{" "}
              نوبت فعال در این روز
            </p>
          </div>
          <div className="calendar-controls">
            <button className="secondary" onClick={() => onDay(todayIso())}>
              امروز
            </button>
            <JalaliInput value={day} onChange={(v) => v && onDay(v)} />
          </div>
        </div>
        <div className="week-navigation">
          <button
            className="icon-btn"
            aria-label="هفته قبل"
            onClick={() => onDay(addDays(day, -7))}
          >
            <ChevronRight size={19} />
          </button>
          <div className="week-strip">
            {week.map((d) => (
              <button
                key={d}
                className={
                  (day === d ? "selected " : "") +
                  (d === todayIso() ? "is-today" : "")
                }
                onClick={() => onDay(d)}
              >
                <small>
                  {new Intl.DateTimeFormat("fa-IR", {
                    weekday: "short",
                    timeZone: "Asia/Tehran",
                  }).format(new Date(d + "T12:00:00Z"))}
                </small>
                <b>
                  {new Intl.DateTimeFormat("fa-IR", {
                    day: "numeric",
                    timeZone: "Asia/Tehran",
                  }).format(new Date(d + "T12:00:00Z"))}
                </b>
                <i />
              </button>
            ))}
          </div>
          <button
            className="icon-btn"
            aria-label="هفته بعد"
            onClick={() => onDay(addDays(day, 7))}
          >
            <ChevronLeft size={19} />
          </button>
        </div>
      </section>
      <div className="workspace-tools">
        <div className="filter-pills">
          {["ALL", "BOOKED", "CONFIRMED", "ARRIVED", "COMPLETED"].map((s) => (
            <button
              key={s}
              className={status === s ? "selected" : ""}
              onClick={() => setStatus(s)}
            >
              {s === "ALL" ? "همه نوبت‌ها" : labels[s]}
            </button>
          ))}
        </div>
        <select
          aria-label="فیلتر متخصص"
          value={staff}
          onChange={(e) => setStaff(e.target.value)}
        >
          <option value="ALL">همه متخصص‌ها</option>
          {specialists.map((s) => (
            <option key={s}>{s}</option>
          ))}
        </select>
        <div className="segmented">
          <button
            aria-label="نمای برنامه"
            className={view === "agenda" ? "selected" : ""}
            onClick={() => setView("agenda")}
          >
            <LayoutGrid size={17} />
          </button>
          <button
            aria-label="نمای جدول"
            className={view === "table" ? "selected" : ""}
            onClick={() => setView("table")}
          >
            <List size={17} />
          </button>
        </div>
      </div>
      {view === "table" ? (
        <section className="panel">
          <Table
            rows={filtered}
            columns={[
              {
                key: "starts_at",
                title: "زمان",
                render: (r) => (
                  <b>
                    {clock(r.starts_at)} – {clock(r.ends_at)}
                  </b>
                ),
              },
              { key: "customer_name", title: "مشتری" },
              { key: "service_names", title: "خدمات" },
              { key: "staff_names", title: "متخصص" },
              {
                key: "status",
                title: "وضعیت",
                render: (r) => <Badge value={r.status} />,
              },
            ]}
            actions={editable ? actions : undefined}
          />
        </section>
      ) : (
        <div className="agenda">
          {!filtered.length ? (
            <section className="panel">
              <Empty
                text="برنامه‌ای با این فیلتر پیدا نشد"
                detail="روز یا فیلتر دیگری انتخاب کنید، یا یک نوبت تازه بسازید."
              />
            </section>
          ) : (
            filtered.map((r) => (
              <article
                className={"agenda-entry state-" + r.status.toLowerCase()}
                key={r.id}
              >
                <div className="agenda-time">
                  <b>{clock(r.starts_at)}</b>
                  <small>{clock(r.ends_at)}</small>
                  <span className="agenda-line" />
                </div>
                <section className="agenda-card">
                  <header>
                    <div className="person">
                      <span className="avatar">
                        {r.customer_name?.charAt(0)}
                      </span>
                      <div>
                        <h3>{r.customer_name}</h3>
                        <small>نوبت #{fa(r.id)}</small>
                      </div>
                    </div>
                    <Badge value={r.status} />
                  </header>
                  <p>{r.service_names}</p>
                  <div className="agenda-meta">
                    <span>
                      <UserRound size={15} />
                      {r.staff_names}
                    </span>
                    <span>
                      <Clock size={15} />
                      {fa(
                        Math.round(
                          (new Date(r.ends_at).getTime() -
                            new Date(r.starts_at).getTime()) /
                            60000,
                        ),
                      )}{" "}
                      دقیقه
                    </span>
                  </div>
                  {editable && <footer>{actions(r)}</footer>}
                </section>
              </article>
            ))
          )}
        </div>
      )}
    </>
  );
}
