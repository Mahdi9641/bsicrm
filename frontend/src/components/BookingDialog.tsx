"use client";
import { api, clock, date, fa, money, Row } from "@/lib/api";
import { localDateTime, todayIso } from "@/lib/dates";
import {
  ArrowLeftRight,
  CalendarDays,
  CheckCircle2,
  Clock,
  Plus,
  Search,
  Trash2,
} from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { CustomerPicker, Lookups } from "./BusinessDialogs";
import JalaliInput from "./JalaliInput";
import { Loading, Modal } from "./ui";

export default function BookingDialog({
  lookups: l,
  appointmentId,
  onClose,
  onDone,
}: {
  lookups: Lookups;
  appointmentId?: number;
  onClose: () => void;
  onDone: () => void;
}) {
  const [resources, setResources] = useState<Row[]>([]);
  useEffect(() => {
    api("/resources")
      .then(setResources)
      .catch((e) => setError(e.message));
  }, []);
  const [customer, setCustomer] = useState(0),
    [customerName, setCustomerName] = useState(""),
    [startsAt, setStartsAt] = useState(todayIso() + "T10:00"),
    [notes, setNotes] = useState(""),
    [reason, setReason] = useState("");
  const [items, setItems] = useState<Row[]>([{ serviceId: "", staffId: "" }]),
    [history, setHistory] = useState<Row[]>([]),
    [version, setVersion] = useState<number | null>(null);
  const [error, setError] = useState(""),
    [busy, setBusy] = useState(false),
    [loading, setLoading] = useState(!!appointmentId),
    [finding, setFinding] = useState(false),
    [slots, setSlots] = useState<Row[] | null>(null);
  const sequence = useRef(0);
  const key = startsAt.slice(0, 10) + JSON.stringify(items);
  useEffect(() => {
    ++sequence.current;
    setSlots(null);
    setFinding(false);
  }, [key]);
  useEffect(() => {
    if (!appointmentId) return;
    let live = true;
    api("/appointments/" + appointmentId)
      .then((d) => {
        if (!live) return;
        setCustomer(d.appointment.customer_id);
        setCustomerName(d.appointment.customer_name);
        setStartsAt(localDateTime(d.appointment.starts_at));
        setVersion(d.appointment.version);
        setItems(
          d.items.map((i: Row) => ({
            serviceId: i.service_id,
            staffId: i.staff_id,
            resourceId: i.resource_id,
          })),
        );
        setHistory(d.history);
      })
      .catch((e) => {
        if (live) setError(e.message);
      })
      .finally(() => {
        if (live) setLoading(false);
      });
    return () => {
      live = false;
    };
  }, [appointmentId]);
  const ready = items.every((i) => i.serviceId && i.staffId) && !!startsAt;
  const find = async () => {
    const seq = ++sequence.current;
    setFinding(true);
    setError("");
    try {
      const r = await api("/appointments/availability", "POST", {
        date: startsAt.slice(0, 10),
        items: items.map((i) => ({
          serviceId: +i.serviceId,
          staffId: +i.staffId,
          resourceId: i.resourceId ? +i.resourceId : null,
        })),
        excludeAppointmentId: appointmentId || null,
      });
      if (seq === sequence.current) setSlots(r.slots);
    } catch (e) {
      if (seq === sequence.current) setError((e as Error).message);
    } finally {
      if (seq === sequence.current) setFinding(false);
    }
  };
  const catalog = (id: number) => l.services.find((s) => s.id === +id);
  const duration = items.reduce(
    (n, i, index) =>
      n +
      Number(catalog(i.serviceId)?.duration_minutes || 0) +
      (index < items.length - 1
        ? Number(catalog(i.serviceId)?.buffer_minutes || 0)
        : 0),
    0,
  );
  return (
    <Modal
      title={appointmentId ? "جابه‌جایی نوبت" : "رزرو یک تجربه زیبا"}
      onClose={onClose}
      wide
    >
      {loading ? (
        <Loading />
      ) : (
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            setBusy(true);
            setError("");
            try {
              const body = {
                customerId: customer,
                startsAt: startsAt + ":00+03:30",
                items: items.map((i) => ({
                  serviceId: +i.serviceId,
                  staffId: +i.staffId,
                  resourceId: i.resourceId ? +i.resourceId : null,
                })),
                notes,
                source: "RECEPTION",
                reason,
                version,
              };
              await api(
                appointmentId
                  ? `/appointments/${appointmentId}/schedule`
                  : "/appointments",
                appointmentId ? "PUT" : "POST",
                body,
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
          <div className="booking-intro">
            <span className="booking-icon">
              <CalendarDays size={25} />
            </span>
            <div>
              <b>{appointmentId ? customerName : "زمانی برای درخشش"}</b>
              <small>
                {appointmentId
                  ? "سوابق جابه‌جایی در پرونده نوبت نگهداری می‌شود."
                  : "خدمات را انتخاب کنید و ساعت‌های آزاد واقعی را ببینید."}
              </small>
            </div>
          </div>
          <div className="form-grid">
            {!appointmentId && (
              <label>
                <span>مشتری</span>
                <CustomerPicker
                  value={customer}
                  onChange={setCustomer}
                  initial={l.customers}
                />
              </label>
            )}
            <label>
              <span>تاریخ و ساعت شروع · تهران</span>
              <JalaliInput
                value={startsAt}
                onChange={setStartsAt}
                time
                required
              />
            </label>
          </div>
          <div className="booking-services">
            {items.map((item, index) => (
              <div className="booking-service" key={index}>
                <span className="step-number">{fa(index + 1)}</span>
                <label>
                  <span>خدمت</span>
                  <select
                    required
                    value={item.serviceId}
                    onChange={(e) =>
                      setItems(
                        items.map((r, j) =>
                          j === index
                            ? { ...r, serviceId: e.target.value, staffId: "" }
                            : r,
                        ),
                      )
                    }
                  >
                    <option value="">انتخاب خدمت</option>
                    {l.services
                      .filter((s) => s.active)
                      .map((s) => (
                        <option value={s.id} key={s.id}>
                          {s.name} · {fa(s.duration_minutes)} دقیقه
                        </option>
                      ))}
                  </select>
                </label>
                <label>
                  <span>متخصص</span>
                  <select
                    required
                    value={item.staffId}
                    onChange={(e) =>
                      setItems(
                        items.map((r, j) =>
                          j === index ? { ...r, staffId: e.target.value } : r,
                        ),
                      )
                    }
                  >
                    <option value="">انتخاب متخصص</option>
                    {l.staff
                      .filter(
                        (s) =>
                          s.active && s.service_ids?.includes(+item.serviceId),
                      )
                      .map((s) => (
                        <option value={s.id} key={s.id}>
                          {s.name}
                        </option>
                      ))}
                  </select>
                </label>
                <label>
                  <span>اتاق / صندلی (اختیاری)</span>
                  <select
                    value={item.resourceId || ""}
                    onChange={(e) =>
                      setItems(
                        items.map((r, j) =>
                          j === index
                            ? { ...r, resourceId: e.target.value }
                            : r,
                        ),
                      )
                    }
                  >
                    <option value="">بدون منبع مشترک</option>
                    {resources
                      .filter((r) => r.active)
                      .map((r) => (
                        <option key={r.id} value={r.id}>
                          {r.name}
                        </option>
                      ))}
                  </select>
                </label>
                <button
                  type="button"
                  className="icon-btn danger"
                  disabled={items.length === 1}
                  onClick={() => setItems(items.filter((_, j) => j !== index))}
                  aria-label="حذف خدمت"
                >
                  <Trash2 size={16} />
                </button>
              </div>
            ))}
            <button
              type="button"
              disabled={items.length >= 10}
              className="text-button"
              onClick={() =>
                setItems([...items, { serviceId: "", staffId: "" }])
              }
            >
              <Plus size={16} />
              افزودن خدمت بعدی
            </button>
          </div>
          <div className="booking-summary">
            <span>
              <Clock size={16} />
              {fa(duration)} دقیقه با فاصله بین خدمات
            </span>
            <b>
              {money(
                items.reduce(
                  (n, i) => n + Number(catalog(i.serviceId)?.price || 0),
                  0,
                ),
              )}
            </b>
          </div>
          <section className="availability">
            <header>
              <div>
                <h3>ساعت‌های آزاد</h3>
                <small>
                  مهارت، ساعت کاری، مرخصی و نوبت‌های ثبت‌شده بررسی می‌شوند.
                </small>
              </div>
              <button
                type="button"
                disabled={!ready || finding}
                className="secondary"
                onClick={find}
              >
                <Search size={16} />
                {finding ? "در حال بررسی…" : "بررسی زمان‌ها"}
              </button>
            </header>
            {slots !== null && (
              <>
                <div className="slot-grid">
                  {slots.map((slot) => (
                    <button
                      key={slot.startsAt}
                      type="button"
                      className={
                        localDateTime(slot.startsAt) === startsAt
                          ? "selected"
                          : ""
                      }
                      onClick={() => setStartsAt(localDateTime(slot.startsAt))}
                    >
                      {clock(slot.startsAt)}
                    </button>
                  ))}
                </div>
                {!slots.length && (
                  <p className="slot-empty">
                    برای این ترکیب خدمات و متخصص‌ها، ساعت آزادی در این روز پیدا
                    نشد.
                  </p>
                )}
                <small>این ساعت‌ها تا زمان ثبت نهایی رزرو نمی‌شوند.</small>
              </>
            )}
          </section>
          <label>
            <span>{appointmentId ? "دلیل جابه‌جایی" : "یادداشت پذیرش"}</span>
            <textarea
              value={appointmentId ? reason : notes}
              onChange={(e) =>
                appointmentId
                  ? setReason(e.target.value)
                  : setNotes(e.target.value)
              }
              required={!!appointmentId}
              maxLength={appointmentId ? 500 : 1000}
            />
          </label>
          {history.length > 0 && (
            <details className="schedule-history">
              <summary>{fa(history.length)} جابه‌جایی پیشین</summary>
              {history.map((h) => (
                <div key={h.id}>
                  <b>
                    {date(h.old_start, true)} ← {date(h.new_start, true)}
                  </b>
                  <p>
                    {h.reason} · {h.actor_name}
                  </p>
                </div>
              ))}
            </details>
          )}
          {error && (
            <p className="error" role="alert">
              {error}
            </p>
          )}
          <div className="modal-actions">
            <button
              className="primary"
              disabled={busy || !ready || (!!appointmentId && version === null)}
            >
              {appointmentId ? (
                <ArrowLeftRight size={17} />
              ) : (
                <CheckCircle2 size={17} />
              )}{" "}
              {busy
                ? "در حال ثبت…"
                : appointmentId
                  ? "ثبت جابه‌جایی"
                  : "ثبت نوبت"}
            </button>
            <button
              className="secondary"
              type="button"
              disabled={busy}
              onClick={onClose}
            >
              انصراف
            </button>
          </div>
        </form>
      )}
    </Modal>
  );
}
