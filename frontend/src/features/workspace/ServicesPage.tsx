"use client";
import { Empty } from "@/components/ui";
import { fa } from "@/lib/api";
import { Clock, Pencil, Scissors } from "lucide-react";
import type { WorkspaceContext } from "./types";
export default function ServicesPage({
  filtered,
  manager,
  setDialog,
}: WorkspaceContext) {
  return (
    <div className="catalog-grid">
      {!filtered.length && <Empty />}
      {filtered.map((s, i) => (
        <section className="panel service-card" key={s.id}>
          <div className="service-card-top">
            <span className={"service-icon shade-" + (i % 5)}>
              <Scissors size={26} />
            </span>
            <span className="subtle-chip">{s.category}</span>
          </div>
          <h3>{s.name}</h3>
          <p>{s.description || "تجربه‌ای حرفه‌ای، با توجه به جزئیات"}</p>
          <div className="service-meta">
            <span>
              <Clock size={15} />
              {fa(s.duration_minutes)} دقیقه
            </span>
            <span className={s.active ? "teal-text" : "muted"}>
              {s.active ? "فعال" : "غیرفعال"}
            </span>
          </div>
          <div className="service-price">
            <strong>
              {fa(s.price)}
              <small> تومان</small>
            </strong>
            {manager && (
              <button
                className="icon-btn"
                aria-label="ویرایش خدمت"
                onClick={() => setDialog({ type: "services", row: s })}
              >
                <Pencil size={17} />
              </button>
            )}
          </div>
        </section>
      ))}
    </div>
  );
}
