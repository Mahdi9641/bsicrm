"use client";
import { api, date, expireSession, logout, Row } from "@/lib/api";
import {
  CheckCircle2,
  KeyRound,
  LogOut,
  Monitor,
  ShieldCheck,
} from "lucide-react";
import { useEffect, useState } from "react";
import { Loading } from "./ui";
export default function SecurityWorkspace() {
  const [sessions, setSessions] = useState<Row[] | null>(null),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  const [current, setCurrent] = useState(""),
    [next, setNext] = useState(""),
    [confirm, setConfirm] = useState("");
  const expired = expireSession;
  const load = () =>
    api("/auth/sessions")
      .then(setSessions)
      .catch((e) => setError(e.message));
  useEffect(() => {
    load();
  }, []);
  const revoke = async (s: Row) => {
    setBusy(true);
    setError("");
    try {
      await api("/auth/sessions/" + s.id, "DELETE");
      if (s.is_current) {
        await logout().catch(() => {});
        expired();
      } else await load();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  };
  return (
    <>
      <div className="module-hero security-hero">
        <div>
          <span className="eyebrow">کنترل حساب در اختیار شماست</span>
          <h2>فضایی امن برای کار روزانه</h2>
          <p>نشست‌های فعال را ببینید و دسترسی‌های قدیمی را ببندید.</p>
        </div>
        <ShieldCheck size={64} strokeWidth={1.2} />
      </div>
      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}
      <div className="security-grid">
        <section className="panel security-panel">
          <header>
            <Monitor size={22} />
            <h3>نشست‌های فعال</h3>
          </header>
          {!sessions ? (
            <Loading />
          ) : (
            sessions.map((s) => (
              <article className="session-card" key={s.id}>
                <span className="session-icon">
                  <Monitor size={22} />
                </span>
                <div>
                  <b>{s.is_current ? "همین نشست" : "نشست دیگر"}</b>
                  {s.is_current && (
                    <span className="current-session">
                      <CheckCircle2 size={12} />
                      جاری
                    </span>
                  )}
                  <p dir="ltr">{s.user_agent || "مرورگر نامشخص"}</p>
                  <small>ورود: {date(s.created_at, true)}</small>
                  <small>آخرین تمدید: {date(s.refreshed_at, true)}</small>
                </div>
                <button
                  className="icon-btn danger"
                  aria-label="پایان نشست"
                  disabled={busy}
                  onClick={() => revoke(s)}
                >
                  <LogOut size={18} />
                </button>
              </article>
            ))
          )}
        </section>
        <section className="panel security-panel">
          <header>
            <KeyRound size={22} />
            <h3>تغییر رمز عبور</h3>
          </header>
          <p className="security-help">
            پس از تغییر رمز، همه نشست‌های شما بسته می‌شوند و باید دوباره وارد
            شوید.
          </p>
          <form
            onSubmit={async (e) => {
              e.preventDefault();
              setError("");
              if (next !== confirm) {
                setError("تکرار رمز با رمز جدید یکسان نیست");
                return;
              }
              setBusy(true);
              try {
                await api("/auth/password", "POST", {
                  currentPassword: current,
                  newPassword: next,
                });
                expired();
              } catch (e) {
                setError((e as Error).message);
              } finally {
                setBusy(false);
              }
            }}
          >
            <label>
              <span>رمز فعلی</span>
              <input
                required
                autoComplete="current-password"
                type="password"
                value={current}
                onChange={(e) => setCurrent(e.target.value)}
              />
            </label>
            <label>
              <span>رمز جدید · حداقل ۱۲ نویسه</span>
              <input
                required
                minLength={12}
                maxLength={72}
                autoComplete="new-password"
                type="password"
                value={next}
                onChange={(e) => setNext(e.target.value)}
              />
            </label>
            <label>
              <span>تکرار رمز جدید</span>
              <input
                required
                minLength={12}
                maxLength={72}
                autoComplete="new-password"
                type="password"
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
              />
            </label>
            <button className="primary" disabled={busy}>
              <ShieldCheck size={17} />
              ذخیره رمز و خروج از نشست‌ها
            </button>
          </form>
        </section>
      </div>
    </>
  );
}
