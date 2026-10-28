"use client";
import { Can } from "@/components/Permissions";
import { fa } from "@/lib/api";
import { avatar } from "./shared";
import type { WorkspaceContext } from "./types";
export default function StaffPage({ filtered, setDialog }: WorkspaceContext) {
  return (
    <div className="catalog-grid">
      {filtered.map((s, i) => (
        <section className="panel staff-card" key={s.id}>
          <span className={"staff-avatar shade-" + (i % 5)}>
            {avatar(s.name)}
          </span>
          <h3>{s.name}</h3>
          <p>{s.specialty || "عضو تیم زیبایی"}</p>
          <div className="staff-details">
            <span>
              ساعت کاری
              <b dir="ltr">
                {s.work_start.slice(0, 5)} — {s.work_end.slice(0, 5)}
              </b>
            </span>
            <span>
              کمیسیون<b>{fa(s.commission_rate)}٪</b>
            </span>
            <span>
              خدمات مجاز<b>{fa(s.service_ids?.length)}</b>
            </span>
          </div>
          <Can permission="staff.write"><div className="card-actions">
            <button
              className="secondary"
              onClick={() => setDialog({ type: "staff", row: s })}
            >
              ویرایش پروفایل
            </button>
            <button
              className="text-button"
              onClick={() => setDialog({ type: "leave", row: s })}
            >
              ثبت مرخصی
            </button>
          </div></Can>
        </section>
      ))}
    </div>
  );
}
