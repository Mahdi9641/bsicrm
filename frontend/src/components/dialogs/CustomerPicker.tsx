"use client";
import { api, Row } from "@/lib/api";
import { useEffect, useState } from "react";
const n = (v: unknown) => (v === "" || v == null ? null : Number(v));
export function CustomerPicker({
  value,
  onChange,
  initial = [],
  optional = false,
}: {
  value: number;
  onChange: (id: number) => void;
  initial?: Row[];
  optional?: boolean;
}) {
  const [q, setQ] = useState(""),
    [rows, setRows] = useState(initial),
    [selected, setSelected] = useState<Row | null>(
      initial.find((c) => c.id === value) || null,
    ),
    [error, setError] = useState("");
  useEffect(() => {
    let active = true;
    const t = setTimeout(() => {
      setError("");
      api("/lookups?q=" + encodeURIComponent(q))
        .then((r) => {
          if (active) setRows(r.customers);
        })
        .catch((e) => {
          if (active) setError(e.message);
        });
    }, 250);
    return () => {
      active = false;
      clearTimeout(t);
    };
  }, [q]);
  useEffect(() => {
    if (!value) {
      setSelected(null);
      return;
    }
    const row = rows.find((c) => c.id === value);
    if (row) {
      setSelected(row);
      return;
    }
    let live = true;
    api("/lookups?customerId=" + value)
      .then((r) => {
        if (live) setSelected(r.customers[0]||null);
      })
      .catch((e) => {
        if (live) setError(e.message);
      });
    return () => {
      live = false;
    };
  }, [value, rows]);
  const choices =
    selected && !rows.some((c) => c.id === selected.id)
      ? [selected, ...rows]
      : rows;
  return (
    <div className="customer-picker">
      <input
        aria-label="جستجوی مشتری"
        placeholder="جستجو با نام یا موبایل…"
        value={q}
        onChange={(e) => setQ(e.target.value)}
      />
      <select
        aria-label="انتخاب مشتری"
        required={!optional}
        value={value || ""}
        onChange={(e) => onChange(+e.target.value)}
      >
        <option value="">
          {optional ? "بدون مشتری مرتبط" : "انتخاب مشتری"}
        </option>
        {choices.map((c) => (
          <option key={c.id} value={c.id}>
            {c.first_name} {c.last_name} · {c.mobile}
          </option>
        ))}
      </select>
      {error && <small className="error">{error}</small>}
    </div>
  );
}
