"use client";
import { jalaaliMonthLength, toGregorian, toJalaali } from "jalaali-js";
import { CalendarDays, ChevronLeft, ChevronRight } from "lucide-react";
import { useState } from "react";
const months = [
  "فروردین",
  "اردیبهشت",
  "خرداد",
  "تیر",
  "مرداد",
  "شهریور",
  "مهر",
  "آبان",
  "آذر",
  "دی",
  "بهمن",
  "اسفند",
];
const pad = (n: number) => String(n).padStart(2, "0");
export default function JalaliInput({
  value,
  onChange,
  time = false,
  required = false,
}: {
  value: string;
  onChange: (v: string) => void;
  time?: boolean;
  required?: boolean;
}) {
  const current = value
    ? new Date(value.slice(0, 10) + "T12:00:00")
    : new Date();
  const j = toJalaali(current);
  const [open, setOpen] = useState(false),
    [year, setYear] = useState(j.jy),
    [month, setMonth] = useState(j.jm);
  const g = toGregorian(year, month, 1),
    offset = (new Date(g.gy, g.gm - 1, g.gd).getDay() + 1) % 7;
  const label = value ? `${j.jy}/${pad(j.jm)}/${pad(j.jd)}` : "";
  const select = (d: number) => {
    const v = toGregorian(year, month, d);
    onChange(
      `${v.gy}-${pad(v.gm)}-${pad(v.gd)}${time ? "T" + (value.split("T")[1]?.slice(0, 5) || "10:00") : ""}`,
    );
    setOpen(false);
  };
  const move = (n: number) => {
    let m = month + n,
      y = year;
    if (m === 0) {
      m = 12;
      y--;
    }
    if (m === 13) {
      m = 1;
      y++;
    }
    setMonth(m);
    setYear(y);
  };
  return (
    <div className="date-field">
      <div className="date-controls">
        <button
          type="button"
          className="date-button"
          onClick={() => setOpen(!open)}
          aria-expanded={open}
        >
          <CalendarDays size={17} />
          <span dir="ltr">{label || "انتخاب تاریخ شمسی"}</span>
        </button>
        {time && (
          <input
            aria-label="ساعت تهران"
            type="time"
            value={value.split("T")[1]?.slice(0, 5) || "10:00"}
            onChange={(e) =>
              onChange(
                (value.slice(0, 10) || `${g.gy}-${pad(g.gm)}-${pad(g.gd)}`) +
                  "T" +
                  e.target.value,
              )
            }
            required={required}
          />
        )}
      </div>
      <input
        tabIndex={-1}
        className="date-validation"
        value={value}
        onChange={() => {}}
        required={required}
        aria-label="تاریخ انتخابی"
      />
      {open && (
        <div className="calendar-pop">
          <div className="calendar-head">
            <button type="button" onClick={() => move(-1)} aria-label="ماه قبل">
              <ChevronRight size={16} />
            </button>
            <select
              aria-label="ماه"
              value={month}
              onChange={(e) => setMonth(+e.target.value)}
            >
              {months.map((m, i) => (
                <option value={i + 1} key={m}>
                  {m}
                </option>
              ))}
            </select>
            <input
              aria-label="سال شمسی"
              type="number"
              min="1300"
              max="1500"
              value={year}
              onChange={(e) => {
                const y = +e.target.value;
                if (y >= 1300 && y <= 1500) setYear(y);
              }}
            />
            <button type="button" onClick={() => move(1)} aria-label="ماه بعد">
              <ChevronLeft size={16} />
            </button>
          </div>
          <div className="calendar-grid">
            {["ش", "ی", "د", "س", "چ", "پ", "ج"].map((d, i) => (
              <small key={i}>{d}</small>
            ))}
            {Array.from({ length: offset }, (_, i) => (
              <span key={"blank" + i} />
            ))}
            {Array.from({ length: jalaaliMonthLength(year, month) }, (_, i) => (
              <button
                type="button"
                key={i}
                className={
                  j.jy === year && j.jm === month && j.jd === i + 1
                    ? "selected"
                    : ""
                }
                onClick={() => select(i + 1)}
              >
                {(i + 1).toLocaleString("fa-IR")}
              </button>
            ))}
          </div>
          <button
            type="button"
            className="text-button"
            onClick={() => {
              onChange("");
              setOpen(false);
            }}
          >
            پاک کردن تاریخ
          </button>
        </div>
      )}
    </div>
  );
}
