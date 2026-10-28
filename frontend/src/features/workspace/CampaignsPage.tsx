"use client";
import {Can} from "@/components/Permissions";
import { Badge, Empty } from "@/components/ui";
import { fa, labels, money } from "@/lib/api";
import { Mail, Megaphone, Send } from "lucide-react";
import type { WorkspaceContext } from "./types";
export default function CampaignsPage({
  filtered,
  busyAction,
  setDialog,
  action,
}: WorkspaceContext) {
  return (
    <>
      <div className="info">
        <Mail size={18} />
        آماده‌سازی کمپین فقط صف را می‌سازد. ارسال واقعی نیازمند فعال‌سازی سرویس
        پیامک است.
      </div>
      <div className="catalog-grid">
        {!filtered.length && <Empty />}
        {filtered.map((c) => (
          <section className="panel campaign-card" key={c.id}>
            <div className="profile-heading">
              <span className="service-icon shade-3">
                <Megaphone size={24} />
              </span>
              <Badge value={c.status} />
            </div>
            <h3>{c.name}</h3>
            <p>{c.objective || "ساخت ارتباطی ماندگار با مشتریان"}</p>
            <div className="campaign-body">{c.body}</div>
            <div className="staff-details">
              <span>
                مخاطبان<b>{labels[c.segment]}</b>
              </span>
              <span>
                انتخاب شده / ارسال شده
                <b>
                  {fa(c.recipients)} / {fa(c.sent)}
                </b>
              </span>
              <span>
                بودجه<b>{money(c.budget)}</b>
              </span>
            </div>
            <div className="card-actions">
              <button
                className="text-button"
                onClick={() =>
                  setDialog({ type: "growth", source: "campaigns", row: c })
                }
              >
                نتیجه و بازده سرمایه‌گذاری
              </button>
              {c.status === "DRAFT" && (
                <Can permission="campaigns.write"><button
                  className="primary"
                  disabled={busyAction}
                  onClick={() => setDialog({ type: "queueCampaign", row: c })}
                >
                  <Send size={16} />
                  آماده‌سازی ارسال
                </button></Can>
              )}
              {["DRAFT", "QUEUED"].includes(c.status) && (
                <Can permission="campaigns.write"><button
                  className="secondary"
                  disabled={busyAction}
                  onClick={() => action("/campaigns/" + c.id + "/cancel")}
                >
                  لغو کمپین
                </button></Can>
              )}
            </div>
          </section>
        ))}
      </div>
    </>
  );
}
