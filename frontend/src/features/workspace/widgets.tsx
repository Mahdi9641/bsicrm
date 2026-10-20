"use client";
import { Badge, Empty } from "@/components/ui";
import { clock, date, fa, labels, money, Row } from "@/lib/api";
import { ChevronLeft } from "lucide-react";
import { segmentColors } from "./shared";
export function Stat({
  title,
  value,
  unit = "",
  icon: Icon,
  hint,
  color,
}: {
  title: string;
  value: string;
  unit?: string;
  icon: any;
  hint: string;
  color: string;
}) {
  return (
    <section className="stat-card">
      <div className="stat-top">
        <span>{title}</span>
        <span className={"stat-icon " + color}>
          <Icon size={20} />
        </span>
      </div>
      <div className="stat-value">
        {value}
        <small>{unit}</small>
      </div>
      <div className="stat-hint">
        <span className={"tiny-dot " + color} />
        {hint}
      </div>
    </section>
  );
}
export function PanelHead({
  title,
  subtitle,
  action,
}: {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="panel-head">
      <div>
        <h3>{title}</h3>
        {subtitle && <p>{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}
export function RevenueChart({ rows }: { rows: Row[] }) {
  const max = Math.max(...rows.map((r) => +r.total), 1),
    total = rows.reduce((s, r) => s + Number(r.total), 0);
  return (
    <div className="revenue-chart">
      <div className="chart-total">
        <strong>{fa(total)}</strong>
        <span>تومان فروش هفت روز اخیر</span>
      </div>
      <div
        className="bar-chart"
        role="img"
        aria-label={"نمودار فروش هفت روز، مجموع " + money(total)}
      >
        <div className="chart-grid">
          <i />
          <i />
          <i />
        </div>
        {rows.map((r, i) => (
          <div
            className="bar-column"
            key={i}
            title={date(r.day) + ": " + money(r.total)}
          >
            <span className="bar-tip">{fa(r.total)}</span>
            <div
              className={"bar " + (i === rows.length - 1 ? "last" : "")}
              style={{
                height: Math.max(2, (Number(r.total) / max) * 140) + "px",
              }}
            />
            <small>
              {new Intl.DateTimeFormat("fa-IR", { weekday: "short" }).format(
                new Date(r.day),
              )}
            </small>
          </div>
        ))}
      </div>
    </div>
  );
}
export function Segments({ data }: { data: Row }) {
  const total = Object.values(data).reduce((s: number, n) => s + Number(n), 0);
  let start = 0;
  const gradient = Object.keys(segmentColors)
    .map((k) => {
      const end = start + (total ? (data[k] / total) * 100 : 0),
        part = `${segmentColors[k]} ${start}% ${end}%`;
      start = end;
      return part;
    })
    .join(",");
  return (
    <div className="segments">
      <div
        className="donut"
        style={{
          background: total ? "conic-gradient(" + gradient + ")" : "#edf0f2",
        }}
      >
        <div>
          <strong>{fa(total)}</strong>
          <small>مشتری فعال</small>
        </div>
      </div>
      <div className="segment-legend">
        {Object.keys(segmentColors).map((k) => (
          <div key={k}>
            <span style={{ background: segmentColors[k] }} />
            <label>{labels[k]}</label>
            <b>{fa(data[k])}</b>
            <small>
              {fa(total ? Math.round((data[k] / total) * 100) : 0)}٪
            </small>
          </div>
        ))}
      </div>
    </div>
  );
}
export function Schedule({
  rows,
  onOpen,
}: {
  rows: Row[];
  onOpen: () => void;
}) {
  if (!rows.length)
    return (
      <Empty
        text="امروز هنوز نوبتی ثبت نشده"
        detail="با ثبت اولین نوبت، برنامه روزتان را بسازید."
      />
    );
  return (
    <div className="schedule-list">
      {rows.slice(0, 5).map((r, i) => (
        <button key={r.id} onClick={onOpen}>
          <span className="schedule-time">
            {clock(r.starts_at)}
            <small>{clock(r.ends_at)}</small>
          </span>
          <span className={"schedule-mark shade-" + (i % 5)} />
          <span className="schedule-person">
            <strong>{r.customer_name}</strong>
            <small>{r.service_names || "مشاهده جزئیات نوبت"}</small>
          </span>
          <Badge value={r.status} />
          <ChevronLeft size={16} />
        </button>
      ))}
    </div>
  );
}
