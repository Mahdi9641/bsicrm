"use client";
import { api, Row } from "@/lib/api";
import { ArrowUpLeft, Search, UserRound } from "lucide-react";
import { useEffect, useState } from "react";
import { Modal } from "./ui";
export default function CommandPalette({
  nav,
  customersAllowed,
  navigate,
  customer,
  close,
}: {
  nav: Row[];
  customersAllowed: boolean;
  navigate: (id: string) => void;
  customer: (id: number) => void;
  close: () => void;
}) {
  const [q, setQ] = useState(""),
    [rows, setRows] = useState<Row[]>([]),
    [error, setError] = useState("");
  useEffect(() => {
    let live = true;
    setRows([]);
    setError("");
    if (q.trim().length < 2 || !customersAllowed) return;
    const t = setTimeout(
      () =>
        api("/customers?q=" + encodeURIComponent(q))
          .then((r) => {
            if (live) setRows(r.slice(0, 6));
          })
          .catch((e) => {
            if (live) setError(e.message);
          }),
      250,
    );
    return () => {
      live = false;
      clearTimeout(t);
    };
  }, [q, customersAllowed]);
  const pages = nav.filter((n) => !q || n.title.includes(q));
  return (
    <Modal title="جستجو و دسترسی سریع" onClose={close}>
      <div className="command-input">
        <Search size={22} />
        <input
          autoFocus
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="نام بخش، مشتری یا شماره موبایل…"
          aria-label="جستجوی سراسری"
        />
        <kbd>Esc</kbd>
      </div>
      <div className="command-results">
        {pages.length > 0 && <small>بخش‌های سامانه</small>}
        {pages.map((n) => (
          <button
            key={n.id}
            onClick={() => {
              close();
              navigate(n.id);
            }}
          >
            <n.icon size={18} />
            <span>{n.title}</span>
            <ArrowUpLeft size={16} />
          </button>
        ))}
        {rows.length > 0 && <small>مشتریان</small>}
        {rows.map((c) => (
          <button
            key={c.id}
            onClick={() => {
              close();
              customer(c.id);
            }}
          >
            <UserRound size={18} />
            <span>
              {c.first_name} {c.last_name}
              <small dir="ltr">{c.mobile}</small>
            </span>
            <ArrowUpLeft size={16} />
          </button>
        ))}
        {!pages.length && !rows.length && (
          <p className="command-empty">موردی پیدا نشد؛ عبارت دیگری بنویسید.</p>
        )}
        {error && <p className="error">{error}</p>}
      </div>
    </Modal>
  );
}
