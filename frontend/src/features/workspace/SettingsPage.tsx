"use client";
import { Badge, Table } from "@/components/ui";
import { labels } from "@/lib/api";
import { ShieldCheck } from "lucide-react";
import type { WorkspaceContext } from "./types";
import { PanelHead } from "./widgets";
export default function SettingsPage({
  rows,
  user,
  bootstrap,
  setDialog,
}: WorkspaceContext) {
  return (
    <>
      <div className="settings-grid">
        <section className="panel">
          <PanelHead title="هویت و تنظیمات سالن" />
          <dl className="detail-list">
            <dt>نام سازمان</dt>
            <dd>{bootstrap.settings?.organization}</dd>
            <dt>شعبه جاری</dt>
            <dd>{bootstrap.branch?.name}</dd>
            <dt>واحد پول</dt>
            <dd>تومان</dd>
            <dt>منطقه زمانی</dt>
            <dd>تهران</dd>
            <dt>سطح امکانات</dt>
            <dd>
              <Badge value={bootstrap.settings?.plan} />
            </dd>
          </dl>
          <button
            className="secondary"
            onClick={() =>
              setDialog({ type: "settings", row: bootstrap.settings })
            }
          >
            ویرایش تنظیمات
          </button>
        </section>
        <section className="panel">
          <PanelHead title="امنیت حساب و دسترسی" />
          <div className="security-card">
            <ShieldCheck size={38} />
            <div>
              <h3>حساب شما با JWT محافظت می‌شود</h3>
              <p>دسترسی‌ها در سرور بر اساس نقش و شعبه کنترل می‌شوند.</p>
            </div>
          </div>
          <div className="card-actions">
            <button
              className="secondary"
              onClick={() => setDialog({ type: "password" })}
            >
              تغییر رمز عبور
            </button>
            <button
              className="secondary"
              onClick={() => setDialog({ type: "branches" })}
            >
              افزودن شعبه
            </button>
          </div>
        </section>
      </div>
      <section className="panel section-space">
        <PanelHead title="کاربران و نقش‌ها" />
        <Table
          rows={rows}
          columns={[
            { key: "name", title: "نام" },
            { key: "username", title: "نام کاربری" },
            { key: "role", title: "نقش", render: (r) => labels[r.role] },
            { key: "branch_id", title: "شناسه شعبه" },
            {
              key: "active",
              title: "وضعیت",
              render: (r) => (r.active ? "فعال" : "غیرفعال"),
            },
          ]}
          actions={(r) => (
            <div className="table-actions">
              <button
                className="text-button"
                onClick={() => setDialog({ type: "userPassword", row: r })}
              >
                تغییر رمز
              </button>
              {r.active && r.id !== user.id ? (
                <button
                  className="text-button danger"
                  onClick={() => setDialog({ type: "deactivateUser", row: r })}
                >
                  غیرفعال کردن
                </button>
              ) : null}
            </div>
          )}
        />
      </section>
      <div className="license-grid">
        {["BASIC", "STANDARD", "ADVANCED", "SMART"].map((p, i) => (
          <section
            className={
              "panel license-card " +
              (bootstrap.settings?.plan === p ? "selected" : "")
            }
            key={p}
          >
            <span>۰{i + 1}</span>
            <h3>{labels[p]}</h3>
            <p>
              {
                [
                  "هسته عملیاتی، مشتریان، نوبت و فروش",
                  "باشگاه مشتریان و کمیسیون پرسنل",
                  "RFM، کمپین، رویداد و تحلیل سودآوری",
                  "سطح پیشرفته؛ قابلیت‌های AI در نقشه توسعه",
                ][i]
              }
            </p>
            {bootstrap.settings?.plan === p && <Badge value="ACTIVE" />}
          </section>
        ))}
      </div>
    </>
  );
}
