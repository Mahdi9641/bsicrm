"use client";
import { Row } from "@/lib/api";
import {
  CalendarDays,
  CalendarHeart,
  ChartNoAxesCombined,
  CheckCircle2,
  ClipboardList,
  Gift,
  LayoutDashboard,
  LockKeyhole,
  Mail,
  Megaphone,
  Receipt,
  Scissors,
  Settings,
  ShieldCheck,
  ShoppingBag,
  TrendingUp,
  UserRound,
  Users,
  Wallet,
} from "lucide-react";
export const all = [
  "ADMIN",
  "MANAGER",
  "RECEPTION",
  "STYLIST",
  "MARKETING",
  "FINANCE",
];
export const navigation = [
  {
    id: "dashboard",
    title: "نمای کلی",
    icon: LayoutDashboard,
    roles: all,
    group: "مدیریت سالن",
  },
  {
    id: "appointments",
    title: "تقویم و نوبت‌ها",
    icon: CalendarDays,
    roles: ["ADMIN", "MANAGER", "RECEPTION", "STYLIST"],
  },
  {
    id: "visits",
    title: "پذیرش و عملیات",
    icon: ClipboardList,
    roles: ["ADMIN", "MANAGER", "RECEPTION", "STYLIST"],
  },
  {
    id: "customers",
    title: "پرونده مشتریان",
    icon: Users,
    roles: ["ADMIN", "MANAGER", "RECEPTION", "MARKETING", "FINANCE"],
  },
  {
    id: "services",
    title: "خدمات و قیمت‌ها",
    icon: Scissors,
    roles: ["ADMIN", "MANAGER", "RECEPTION"],
  },
  {
    id: "resources",
    title: "فضا و تجهیزات",
    icon: Scissors,
    roles: ["ADMIN", "MANAGER"],
  },
  {
    id: "products",
    title: "محصولات و موجودی",
    icon: ShoppingBag,
    roles: ["ADMIN", "MANAGER", "RECEPTION"],
  },
  {
    id: "invoices",
    title: "فاکتورها و فروش",
    icon: Receipt,
    roles: ["ADMIN", "MANAGER", "RECEPTION", "FINANCE"],
    group: "مالی و تیم",
  },
  {
    id: "finance",
    title: "حساب‌ها و هزینه‌ها",
    icon: Wallet,
    roles: ["ADMIN", "MANAGER", "FINANCE"],
  },
  {
    id: "staff",
    title: "تیم زیبایی",
    icon: UserRound,
    roles: ["ADMIN", "MANAGER"],
  },
  {
    id: "commissions",
    title: "کمیسیون پرسنل",
    icon: TrendingUp,
    roles: ["ADMIN", "MANAGER", "FINANCE", "STYLIST"],
  },
  {
    id: "commission-rules",
    title: "قواعد کمیسیون",
    icon: TrendingUp,
    roles: ["ADMIN", "MANAGER", "FINANCE"],
  },
  {
    id: "loyalty",
    title: "باشگاه مشتریان",
    icon: Gift,
    roles: ["ADMIN", "MANAGER", "RECEPTION", "MARKETING"],
    group: "ارتباط و رشد",
  },
  {
    id: "rfm",
    title: "شناخت مشتریان · RFM",
    icon: ChartNoAxesCombined,
    roles: ["ADMIN", "MANAGER", "MARKETING"],
  },
  {
    id: "campaigns",
    title: "کمپین‌های بازاریابی",
    icon: Megaphone,
    roles: ["ADMIN", "MANAGER", "MARKETING"],
  },
  {
    id: "events",
    title: "رویدادهای سالن",
    icon: CalendarHeart,
    roles: ["ADMIN", "MANAGER", "MARKETING"],
  },
  {
    id: "tasks",
    title: "پیگیری مشتریان",
    icon: CheckCircle2,
    roles: ["ADMIN", "MANAGER", "RECEPTION", "MARKETING"],
  },
  {
    id: "messages",
    title: "پیام‌ها",
    icon: Mail,
    roles: ["ADMIN", "MANAGER", "MARKETING"],
  },
  {
    id: "reports",
    title: "گزارش‌های عملکرد",
    icon: ChartNoAxesCombined,
    roles: ["ADMIN", "MANAGER", "FINANCE"],
  },
  {
    id: "settings",
    title: "تنظیمات سامانه",
    icon: Settings,
    roles: ["ADMIN"],
    group: "سامانه",
  },
  {id:"salons",title:"مدیریت سالن‌ها",icon:Settings,roles:["ADMIN"]},
  {id:"branches",title:"مدیریت شعب",icon:Settings,roles:["ADMIN","MANAGER"]},
  {id:"roles",title:"مدیریت نقش‌ها",icon:ShieldCheck,roles:["ADMIN","MANAGER"]},
  {id:"users",title:"مدیریت کاربران",icon:Users,roles:["ADMIN","MANAGER"]},
  {id:"subscription",title:"خرید و تمدید اشتراک",icon:ShoppingBag,roles:["ADMIN","MANAGER"]},
  { id: "security", title: "امنیت حساب من", icon: LockKeyhole, roles: all },
  {
    id: "audit",
    title: "تاریخچه فعالیت‌ها",
    icon: ShieldCheck,
    roles: ["ADMIN", "MANAGER"],
  },
];
export const todayIso = () =>
  new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Tehran",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(new Date());
export const dayRange = (s: string) => {
  const from = s + "T00:00:00+03:30";
  return (
    "?from=" +
    encodeURIComponent(from) +
    "&to=" +
    encodeURIComponent(
      new Date(new Date(from).getTime() + 86400000).toISOString(),
    )
  );
};
export const avatar = (name: string) => name?.trim().charAt(0) || "ب";
export const segmentColors: Record<string, string> = {
  LOYAL: "#158a78",
  GROWING: "#8d74c4",
  LOST: "#89939d",
  AT_RISK: "#d6767b",
};
export function exportCsv(rows: Row[], name: string) {
  const cols = Object.keys(rows[0] || {}).filter(
    (k) => typeof rows[0][k] !== "object",
  );
  const safe = (v: any) => {
    let s = String(v ?? "");
    if (/^[=+@\-\t\r]/.test(s)) s = "'" + s;
    return '"' + s.replaceAll('"', '""') + '"';
  };
  const text =
    "\uFEFF" +
    [
      cols.map(safe).join(","),
      ...rows.map((r) => cols.map((k) => safe(r[k])).join(",")),
    ].join("\r\n");
  const u = URL.createObjectURL(
    new Blob([text], { type: "text/csv;charset=utf-8" }),
  );
  const a = document.createElement("a");
  a.href = u;
  a.download = name + ".csv";
  a.click();
  URL.revokeObjectURL(u);
}
