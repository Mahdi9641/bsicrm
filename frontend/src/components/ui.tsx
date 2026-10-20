"use client";
import { Row, labels } from "@/lib/api";
import { AlertCircle, Check, Inbox, LoaderCircle, X } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import JalaliInput from "./JalaliInput";
export function Logo() {
  return (
    <div className="logo">
      <svg width="38" height="42" viewBox="0 0 40 48" fill="none">
        <path
          d="M20 2C7 14 16 22 20 26C24 22 33 14 20 2Z"
          fill="currentColor"
        />
        <path
          d="M3 16C1 32 13 33 18 35C17 27 12 20 3 16ZM37 16C39 32 27 33 22 35C23 27 28 20 37 16ZM5 35L20 47L35 35C28 34 23 35 20 40C17 35 12 34 5 35Z"
          fill="currentColor"
        />
      </svg>
      <div>
        <strong dir="ltr">
          Bsi<span>CRM</span>
        </strong>
        <small>زیبایی در مدیریت، هوشمندی در رشد</small>
      </div>
    </div>
  );
}
export function Badge({ value }: { value: unknown }) {
  const v = String(value || "");
  return <span className={"badge b-" + v.toLowerCase()}>{labels[v] || v}</span>;
}
export function Empty({
  text = "هنوز اطلاعاتی ثبت نشده است",
  detail = "از دکمه افزودن برای ثبت اولین مورد استفاده کنید.",
}: {
  text?: string;
  detail?: string;
}) {
  return (
    <div className="empty">
      <div>
        <Inbox size={28} />
      </div>
      <h3>{text}</h3>
      <p>{detail}</p>
    </div>
  );
}
export function Loading() {
  return (
    <div className="loading">
      <LoaderCircle size={25} className="spin" />
      <span>در حال دریافت اطلاعات…</span>
    </div>
  );
}
export function Modal({
  title,
  onClose,
  children,
  wide = false,
}: {
  title: string;
  onClose: () => void;
  children: React.ReactNode;
  wide?: boolean;
}) {
  const ref = useRef<HTMLDivElement>(null);
  const closeRef = useRef(onClose);
  closeRef.current = onClose;
  useEffect(() => {
    const previous = document.activeElement as HTMLElement;
    const dialog = ref.current;
    if (!dialog?.contains(document.activeElement)) dialog?.focus();
    const key = (e: KeyboardEvent) => {
      if (
        e.key === "Escape" &&
        dialog === Array.from(document.querySelectorAll(".modal")).at(-1)
      )
        closeRef.current();
      if (
        e.key === "Tab" &&
        dialog === Array.from(document.querySelectorAll(".modal")).at(-1)
      ) {
        const all = dialog?.querySelectorAll<HTMLElement>(
          'button:not([disabled]),input:not([disabled]),select,textarea,[tabindex="0"]',
        );
        if (!all?.length) return;
        const first = all[0],
          last = all[all.length - 1];
        if (
          e.shiftKey &&
          (document.activeElement === first ||
            document.activeElement === dialog)
        ) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    };
    document.addEventListener("keydown", key);
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", key);
      document.body.style.overflow = prev;
      previous?.focus();
    };
  }, []);
  return (
    <div
      className="modal-backdrop"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        className={"modal " + (wide ? "wide" : "")}
        ref={ref}
        tabIndex={-1}
        role="dialog"
        aria-modal="true"
        aria-label={title}
      >
        <div className="modal-head">
          <h2>{title}</h2>
          <button className="icon-btn" onClick={onClose} aria-label="بستن">
            <X size={21} />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}
export type Field = {
  key: string;
  label: string;
  type?: string;
  required?: boolean;
  options?: { value: string | number; label: string }[];
  min?: number;
  max?: number;
  wide?: boolean;
  help?: string;
};
export function Editor({
  title,
  fields,
  initial = {},
  submit,
  onClose,
}: {
  title: string;
  fields: Field[];
  initial?: Row;
  submit: (data: Row) => Promise<void>;
  onClose: () => void;
}) {
  const [data, setData] = useState<Row>(initial),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  return (
    <Modal title={title} onClose={onClose} wide={fields.length > 7}>
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          setError("");
          setBusy(true);
          try {
            await submit(data);
            onClose();
          } catch (e) {
            setError((e as Error).message);
          } finally {
            setBusy(false);
          }
        }}
      >
        <div className="form-grid">
          {fields.map((f) => (
            <label
              key={f.key}
              className={
                (f.wide ? "full " : "") +
                (f.type === "checkbox" ? "check-field" : "")
              }
            >
              <span>
                {f.label}
                {f.required && <i> *</i>}
              </span>
              {f.type === "select" ? (
                <select
                  value={data[f.key] ?? ""}
                  required={f.required}
                  onChange={(e) =>
                    setData({ ...data, [f.key]: e.target.value })
                  }
                >
                  <option value="">انتخاب کنید</option>
                  {f.options?.map((o) => (
                    <option key={o.value} value={o.value}>
                      {o.label}
                    </option>
                  ))}
                </select>
              ) : f.type === "textarea" ? (
                <textarea
                  maxLength={f.max || 1000}
                  value={data[f.key] ?? ""}
                  onChange={(e) =>
                    setData({ ...data, [f.key]: e.target.value })
                  }
                  required={f.required}
                />
              ) : f.type === "date" || f.type === "datetime" ? (
                <JalaliInput
                  value={data[f.key] || ""}
                  time={f.type === "datetime"}
                  required={f.required}
                  onChange={(v) => setData({ ...data, [f.key]: v })}
                />
              ) : f.type === "checkbox" ? (
                <input
                  type="checkbox"
                  checked={!!data[f.key]}
                  onChange={(e) =>
                    setData({ ...data, [f.key]: e.target.checked })
                  }
                />
              ) : (
                <input
                  type={f.type || "text"}
                  min={f.min}
                  max={f.max}
                  maxLength={f.type === "number" ? undefined : f.max || 250}
                  step={f.type === "number" ? "1" : undefined}
                  required={f.required}
                  value={data[f.key] ?? ""}
                  onChange={(e) =>
                    setData({
                      ...data,
                      [f.key]:
                        f.type === "number"
                          ? Number(e.target.value)
                          : e.target.value,
                    })
                  }
                />
              )}{" "}
              {f.help && <small>{f.help}</small>}
            </label>
          ))}
        </div>
        {error && (
          <div role="alert" className="error">
            <AlertCircle size={17} />
            {error}
          </div>
        )}
        <div className="modal-actions">
          <button type="submit" className="primary" disabled={busy}>
            {busy ? (
              <LoaderCircle size={17} className="spin" />
            ) : (
              <Check size={17} />
            )}
            ذخیره اطلاعات
          </button>
          <button
            type="button"
            className="secondary"
            onClick={onClose}
            disabled={busy}
          >
            انصراف
          </button>
        </div>
      </form>
    </Modal>
  );
}
export type Column = {
  key: string;
  title: string;
  render?: (row: Row) => React.ReactNode;
};
export function Table({
  rows,
  columns,
  actions,
}: {
  rows: Row[];
  columns: Column[];
  actions?: (row: Row) => React.ReactNode;
}) {
  if (!rows.length) return <Empty />;
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            {columns.map((c) => (
              <th key={c.key}>{c.title}</th>
            ))}
            {actions && <th>عملیات</th>}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, i) => (
            <tr key={row.id ?? i}>
              {columns.map((c) => (
                <td key={c.key}>
                  {c.render ? c.render(row) : String(row[c.key] ?? "—")}
                </td>
              ))}
              {actions && (
                <td>
                  <div className="row-actions">{actions(row)}</div>
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
export const field = (
  key: string,
  label: string,
  type = "text",
  required = true,
  extra: Partial<Field> = {},
): Field => ({ key, label, type, required, ...extra });
export const options = (values: string[]) =>
  values.map((v) => ({ value: v, label: labels[v] || v }));
