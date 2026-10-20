"use client";
import { api, Row } from "@/lib/api";
import { useState } from "react";
import { Modal } from "../ui";
import type { Lookups } from "./types";
const n = (v: unknown) => (v === "" || v == null ? null : Number(v));
export function StaffDialog({
  row = {},
  lookups: l,
  onClose,
  onDone,
}: {
  row?: Row;
  lookups: Lookups;
  onClose: () => void;
  onDone: () => void;
}) {
  const [data, setData] = useState<Row>({
      name: row.name || "",
      mobile: row.mobile || "",
      specialty: row.specialty || "",
      commissionRate: row.commission_rate ?? 30,
      workStart: row.work_start?.slice(0, 5) || "09:00",
      workEnd: row.work_end?.slice(0, 5) || "21:00",
      workDays: row.work_days || "1,2,3,4,6,7",
      active: row.active ?? true,
      serviceIds: row.service_ids || [],
    }),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  const update = (key: string, value: any) =>
    setData({ ...data, [key]: value });
  return (
    <Modal title="اطلاعات پرسنل و مهارت‌ها" onClose={onClose} wide>
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          setBusy(true);
          try {
            await api(
              "/staff" + (row.id ? "/" + row.id : ""),
              row.id ? "PUT" : "POST",
              data,
            );
            onDone();
            onClose();
          } catch (e) {
            setError((e as Error).message);
          } finally {
            setBusy(false);
          }
        }}
      >
        <div className="form-grid">
          {[
            ["name", "نام و نام خانوادگی", "text"],
            ["mobile", "شماره موبایل", "tel"],
            ["specialty", "تخصص", "text"],
            ["commissionRate", "درصد کمیسیون", "number"],
            ["workStart", "شروع ساعت کاری", "time"],
            ["workEnd", "پایان ساعت کاری", "time"],
          ].map(([key, label, type]) => (
            <label key={key}>
              <span>{label}</span>
              <input
                type={type}
                value={data[key]}
                min={0}
                max={type === "number" ? 100 : undefined}
                required={key === "name"}
                onChange={(e) =>
                  update(
                    key,
                    type === "number" ? +e.target.value : e.target.value,
                  )
                }
              />
            </label>
          ))}
          <fieldset className="full">
            <legend>روزهای کاری</legend>
            <div className="choice-row">
              {[
                [6, "شنبه"],
                [7, "یکشنبه"],
                [1, "دوشنبه"],
                [2, "سه‌شنبه"],
                [3, "چهارشنبه"],
                [4, "پنجشنبه"],
                [5, "جمعه"],
              ].map(([v, label]) => (
                <label key={v}>
                  <input
                    type="checkbox"
                    checked={data.workDays.split(",").includes("" + v)}
                    onChange={(e) => {
                      const days = data.workDays.split(",").filter(Boolean);
                      update(
                        "workDays",
                        (e.target.checked
                          ? [...days, "" + v]
                          : days.filter((x: string) => x !== "" + v)
                        ).join(","),
                      );
                    }}
                  />
                  {label}
                </label>
              ))}
            </div>
          </fieldset>
          <fieldset className="full">
            <legend>خدمات مجاز</legend>
            <div className="choice-row">
              {l.services.map((s) => (
                <label key={s.id}>
                  <input
                    type="checkbox"
                    checked={data.serviceIds.includes(s.id)}
                    onChange={(e) =>
                      update(
                        "serviceIds",
                        e.target.checked
                          ? [...data.serviceIds, s.id]
                          : data.serviceIds.filter((id: number) => id !== s.id),
                      )
                    }
                  />
                  {s.name}
                </label>
              ))}
            </div>
          </fieldset>
          <label className="check-field">
            <span>همکاری فعال</span>
            <input
              type="checkbox"
              checked={data.active}
              onChange={(e) => update("active", e.target.checked)}
            />
          </label>
        </div>
        {error && <p className="error">{error}</p>}
        <div className="modal-actions">
          <button className="primary" disabled={busy}>
            ذخیره پرسنل
          </button>
        </div>
      </form>
    </Modal>
  );
}
