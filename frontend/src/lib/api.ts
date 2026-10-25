export type Row = Record<string, any>;
let accessToken = "";
let refreshing: Promise<Row> | null = null;
const reads = new Map<string, Promise<any>>();
const cooldowns = new Map<string, number>();
export class ApiRequestError extends Error {
  constructor(message: string, public status: number, public retryAfterSeconds = 0) { super(message); }
}
const persian = (value: unknown, fallback: string) => typeof value === "string" && /[\u0600-\u06ff]/.test(value) ? value : fallback;
function group(path: string, method: string) {
  if (["/auth/login", "/auth/customer/login", "/auth/customer/register"].includes(path)) return "auth";
  if (path.startsWith("/auth/captcha")) return "captcha";
  if (["/auth/refresh", "/auth/logout"].includes(path)) return "session";
  return method === "GET" ? "read" : "write";
}
async function request(path: string, method: string, body?: unknown, token = ""): Promise<Row> {
  const key = group(path, method);
  const remaining = Math.ceil(((cooldowns.get(key) || 0) - Date.now()) / 1000);
  if (remaining > 0) throw new ApiRequestError(`تعداد درخواست‌ها زیاد است؛ ${remaining.toLocaleString("fa-IR")} ثانیه دیگر تلاش کنید`, 429, remaining);
  let response: Response;
  try {
    response = await fetch("/api" + path, { method, credentials: "include", cache: "no-store",
      headers: { "Content-Type": "application/json", ...(token ? { Authorization: `Bearer ${token}` } : {}) },
      ...(body !== undefined ? { body: JSON.stringify(body) } : {}), });
  } catch { throw new ApiRequestError("ارتباط با سرور برقرار نشد؛ اتصال اینترنت را بررسی کنید", 0); }
  if (response.status === 204) return {};
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    if (response.status === 402) window.dispatchEvent(new Event("subscription-expired"));
    let delay = 0;
    if (response.status === 429) {
      const header = response.headers.get("Retry-After");
      const seconds = header && /^\d+$/.test(header) ? Number(header) : header ? Math.ceil((Date.parse(header) - Date.now()) / 1000) : Number(data.retryAfterSeconds);
      delay = Number.isFinite(seconds) ? Math.max(1, Math.min(seconds, 3600)) : 5;
      cooldowns.set(key, Date.now() + delay * 1000);
    }
    const fallback = response.status === 429 ? "تعداد درخواست‌ها زیاد است؛ کمی صبر کنید" : response.status === 403 ? "اجازهٔ انجام این عملیات را ندارید" : "درخواست انجام نشد؛ لطفاً دوباره تلاش کنید";
    const fields = Array.isArray(data.fields) ? " — " + data.fields.map((f: Row) => persian(f.message, "مقدار واردشده معتبر نیست")).join("، ") : "";
    throw new ApiRequestError(persian(data.message, fallback) + fields +
      (delay ? ` · تلاش دوباره پس از ${delay.toLocaleString("fa-IR")} ثانیه` : "") +
      (response.status >= 500 && data.requestId ? " · شناسه پیگیری: " + data.requestId : ""), response.status, delay);
  }
  return data;
}
export async function refreshSession(): Promise<Row> {
  if (!refreshing) refreshing = request("/auth/refresh", "POST")
    .then(body => { accessToken = body.accessToken; return body; }).finally(() => { refreshing = null; });
  return refreshing;
}
export async function api(path: string, method = "GET", body?: unknown, retry = true): Promise<any> {
  const publicAuth = path.startsWith("/auth/captcha") || path === "/auth/salons" || [
    "/auth/login", "/auth/customer/login", "/auth/customer/register", "/auth/refresh", "/auth/logout"].includes(path);
  const token = publicAuth ? "" : accessToken;
  const key = path + ":" + token;
  const run = async (): Promise<any> => {
    try {
      const data = await request(path, method, body, token);
      if (data.accessToken) { accessToken = data.accessToken; if(publicAuth && path !== '/auth/refresh') {cooldowns.delete('read');cooldowns.delete('write');} }
      return data;
    } catch (e) {
      if (e instanceof ApiRequestError && e.status === 401 && !publicAuth) {
        if(!retry) { expireSession(); throw e; }
        try { await refreshSession(); }
        catch (refreshError) {
          // Network failure or rate limiting must not log the user out.
          if (refreshError instanceof ApiRequestError && refreshError.status === 401) expireSession();
          throw refreshError;
        }
        return api(path, method, body, false);
      }
      throw e;
    }
  };
  // Share only simultaneous GETs, never cache data or replay a write after 429.
  if (method !== "GET" || !retry) return run();
  if (!reads.has(key)) reads.set(key, run().finally(() => reads.delete(key)));
  return reads.get(key);
}
export function expireSession() { accessToken = ""; reads.clear(); window.dispatchEvent(new Event("session-expired")); }
export async function logout() {
  if (refreshing) await refreshing.catch(() => {});
  await api("/auth/logout", "POST"); accessToken = ""; reads.clear();
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
  SMS: "پیامک",
  WHATSAPP: "واتساپ",
  INACTIVE: "غیرفعال",
  BLOCKED: "مسدود",
  DELIVERED: "تحویل‌شده",
  UNDELIVERABLE: "تحویل ناموفق",
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

export const auditLabels: Record<string,string> = {
  "LOGIN": "ورود",
  "UPDATE": "ویرایش",
  "CREATE": "ایجاد",
  "SAVE": "ذخیره",
  "DELETE": "حذف",
  "LICENSE": "تمدید مجوز",
  "ATTRIBUTION": "انتساب فروش",
  "ATTRIBUTE_COST": "انتساب هزینه",
  "POINTS": "تغییر امتیاز",
  "QUEUE": "قرار دادن در صف",
  "ISSUE": "صدور",
  "PAYMENT": "پرداخت",
  "SHARE_LINK": "ساخت لینک اشتراک",
  "PASSWORD_CHANGED": "تغییر رمز",
  "PASSWORD_RESET": "بازنشانی رمز",
  "ORDER": "ثبت سفارش",
  "PAYMENT_REFERENCE": "ثبت رسید",
  "REJECT": "رد درخواست",
  "DEACTIVATE": "غیرفعال‌سازی",
  "RESCHEDULE": "جابه‌جایی نوبت",
  "QUEUE_SMS": "صف پیامک",
  "CANCEL": "لغو",
  "CHECK_IN": "پذیرش",
  "COMPLETE": "تکمیل",
  "STOCK": "تغییر موجودی",
  "SWITCH": "تغییر شعبه",
  "REVOKE_SESSIONS": "لغو نشست‌ها",
  "CREATE_ACCOUNT": "ایجاد حساب",
  "USER": "کاربر",
  "SALON": "سالن",
  "PLAN": "طرح اشتراک",
  "SUBSCRIPTION": "اشتراک",
  "INVOICE": "فاکتور",
  "CUSTOMER": "مشتری",
  "COMMISSION": "کمیسیون",
  "COMMISSION_RULE": "قاعده کمیسیون",
  "APPOINTMENT": "نوبت",
  "TASK": "پیگیری",
  "MESSAGE_TEMPLATE": "قالب پیام",
  "MESSAGE": "پیام",
  "VISIT": "مراجعه",
  "SERVICE": "خدمت",
  "STAFF": "پرسنل",
  "PRODUCT": "محصول",
  "SETTINGS": "تنظیمات",
  "RESOURCE": "منبع",
  "BRANCH": "شعبه",
  "CAMPAIGN": "کمپین",
  "EVENT": "رویداد",
  "CAMPAIGNS": "کمپین‌ها",
  "EVENTS": "رویدادها"
};
