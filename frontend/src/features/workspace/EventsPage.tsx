"use client";
import { Badge, Empty } from "@/components/ui";
import { clock, date, fa } from "@/lib/api";
import { ArrowUpLeft, CalendarHeart } from "lucide-react";
import type { WorkspaceContext } from "./types";
export default function EventsPage({ filtered, setDialog }: WorkspaceContext) {
  return (
    <div className="catalog-grid">
      {!filtered.length && <Empty />}
      {filtered.map((e) => (
        <section className="panel event-card" key={e.id}>
          <div className="event-cover">
            <CalendarHeart size={48} />
            <span>{date(e.starts_at)}</span>
          </div>
          <div className="event-info">
            <button
              className="text-button"
              onClick={() =>
                setDialog({ type: "growth", source: "events", row: e })
              }
            >
              نتیجه و بازده سرمایه‌گذاری
            </button>
            <Badge value={e.status} />
            <h3>{e.name}</h3>
            <p>{e.description}</p>
            <small>
              {e.venue} · {clock(e.starts_at)}
            </small>
            <div className="capacity-track">
              <i
                style={{
                  width: Math.min(100, (e.registered / e.capacity) * 100) + "%",
                }}
              />
            </div>
            <div className="capacity-label">
              <span>{fa(e.registered)} ثبت‌نام</span>
              <span>ظرفیت {fa(e.capacity)} نفر</span>
            </div>
            <button
              className="secondary full-button"
              onClick={() => setDialog({ type: "participants", row: e })}
            >
              مهمانان و ثبت حضور
              <ArrowUpLeft size={16} />
            </button>
          </div>
        </section>
      ))}
    </div>
  );
}
