export type Row = Record<string, any>;
let accessToken = "";
let refreshing: Promise<Row> | null = null;
export async function refreshSession(): Promise<Row> {
  if (!refreshing)
    refreshing = fetch("/api/auth/refresh", {
      method: "POST",
      credentials: "include",
      cache: "no-store",
    })
      .then(async (r) => {
        const body = await r.json();
        if (!r.ok) throw new Error(body.message || "دوباره وارد شوید");
        accessToken = body.accessToken;
        return body;
      })
      .finally(() => {
        refreshing = null;
      });
  return refreshing;
}
export async function api(
  path: string,
  method = "GET",
  body?: unknown,
  retry = true,
): Promise<any> {
  const publicAuth = path.startsWith("/auth/captcha") || path === "/auth/salons" || [
    "/auth/login",
    "/auth/customer/login",
    "/auth/customer/register",
    "/auth/refresh",
    "/auth/logout",
  ].includes(path);
  const r = await fetch("/api" + path, {
    method,
    credentials: "include",
    cache: "no-store",
    headers: {
      "Content-Type": "application/json",
      ...(accessToken && !publicAuth
        ? { Authorization: `Bearer ${accessToken}` }
        : {}),
    },
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  });
  if (r.status === 401 && retry && !path.startsWith("/auth/")) {
    try {
      await refreshSession();
      return api(path, method, body, false);
    } catch {
      expireSession();
      throw new Error("نشست پایان یافته؛ دوباره وارد شوید");
    }
  }
  const data = await r
    .json()
    .catch(() => ({ message: "ارتباط با سرور برقرار نشد" }));
  if (r.status===402) window.dispatchEvent(new Event("subscription-expired"));
  if (!r.ok)
    throw new Error(
      (data.message || "درخواست انجام نشد؛ لطفاً دوباره تلاش کنید") +
        (data.fields
          ? " — " + data.fields.map((f: Row) => f.message).join("، ")
          : "") +
        (r.status >= 500 && data.requestId
          ? " · شناسه پیگیری: " + data.requestId
          : ""),
    );
  if (data.accessToken) accessToken = data.accessToken;
  return data;
}
export function expireSession() {
  accessToken = "";
  window.dispatchEvent(new Event("session-expired"));
}
export async function logout() {
  if (refreshing) await refreshing.catch(() => {});
  await api("/auth/logout", "POST");
  accessToken = "";
}
export const fa = (n: unknown) => Number(n || 0).toLocaleString("fa-IR");
export const money = (n: unknown) => fa(n) + " تومان";
export const date = (value: unknown, withTime = false) =>
  value
    ? new Intl.DateTimeFormat("fa-IR", {
        timeZone: "Asia/Tehran",
        year: "numeric",
        month: "short",
        day: "numeric",
        ...(withTime ? ({ hour: "2-digit", minute: "2-digit" } as const) : {}),
      }).format(new Date(String(value)))
    : "—";
export const clock = (value: unknown) =>
  value
    ? new Intl.DateTimeFormat("fa-IR", {
        timeZone: "Asia/Tehran",
        hour: "2-digit",
        minute: "2-digit",
      }).format(new Date(String(value)))
    : "—";
export const labels: Record<string, string> = {
  ACTIVE: "فعال",
  LOW: "کم",
  HIGH: "بالا",
  URGENT: "فوری",
  IN_PROGRESS: "در حال پیگیری",
  DONE: "انجام‌شده",
  ADMIN: "مدیر سامانه",
  CUSTOMER: "مشتری",
  MANAGER: "مدیر سالن",
  RECEPTION: "پذیرش",
  STYLIST: "آرایشگر",
  MARKETING: "بازاریابی",
  FINANCE: "امور مالی",
  BOOKED: "رزرو شده",
  CONFIRMED: "تأیید شده",
  ARRIVED: "پذیرش شده",
  COMPLETED: "تکمیل شده",
  CANCELLED: "لغو شده",
  NO_SHOW: "عدم حضور",
  WAITING: "در انتظار",
  IN_SERVICE: "در حال خدمت",
  PLANNED: "برنامه‌ریزی شده",
  ISSUED: "صادر شده",
  PARTIAL: "پرداخت جزئی",
  PAID: "تسویه شده",
  VOID: "باطل شده",
  CALCULATED: "محاسبه شده",
  APPROVED: "تأیید شده",
  SETTLED: "تسویه شده",
  LOYAL: "مشتریان وفادار",
  GROWING: "در حال رشد",
  LOST: "غیرفعال یا ازدست‌رفته",
  TRANSFER_IN: "انتقال ورودی",
  TRANSFER_OUT: "انتقال خروجی",
  OPENING: "مانده افتتاحیه",
  NEW: "مشتریان جدید",
  AT_RISK: "در معرض ریزش",
  BRONZE: "برنزی",
  SILVER: "نقره‌ای",
  GOLD: "طلایی",
  ALL: "همه مشتریان",
  DRAFT: "پیش‌نویس",
  QUEUED: "در صف ارسال",
  SENDING: "در حال ارسال",
  SENT: "ارسال شده",
  FAILED: "ناموفق",
  OPEN: "باز",
  REGISTERED: "ثبت‌نام شده",
  ATTENDED: "حاضر",
  BASIC: "پایه",
  STANDARD: "استاندارد",
  ADVANCED: "پیشرفته",
  SMART: "هوشمند",
  CASH: "نقدی",
  CARD: "کارتخوان",
  TRANSFER: "انتقال بانکی",
  RECEIPT: "دریافت",
  EXPENSE: "هزینه",
  REFUND: "برگشت وجه",
  COMMISSION: "کمیسیون",
  NORMAL: "عادی",
  VIP: "ویژه",
  INSTAGRAM: "اینستاگرام",
  REFERRAL: "معرفی",
  OTHER: "سایر",
  PHONE: "تلفن",
  ONLINE: "آنلاین",
};

export function uuid() {
  const bytes = crypto.getRandomValues(new Uint8Array(16));
  bytes[6] = (bytes[6] & 15) | 64;
  bytes[8] = (bytes[8] & 63) | 128;
  const h = Array.from(bytes, (b) => b.toString(16).padStart(2, "0")).join("");
  return `${h.slice(0, 8)}-${h.slice(8, 12)}-${h.slice(12, 16)}-${h.slice(16, 20)}-${h.slice(20)}`;
}
