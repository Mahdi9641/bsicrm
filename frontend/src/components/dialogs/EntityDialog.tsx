"use client";
import { api, Row } from "@/lib/api";
import { Editor, field, options } from "../ui";
import type { Lookups } from "./types";
const n = (v: unknown) => (v === "" || v == null ? null : Number(v));
export function EntityDialog({
  type,
  row = {},
  lookups: l,
  onClose,
  onDone,
}: {
  type: string;
  row?: Row;
  lookups: Lookups;
  onClose: () => void;
  onDone: () => void;
}) {
  const customerOptions = l.customers.map((c) => ({
      value: c.id,
      label: c.first_name + " " + c.last_name,
    })),
    staffOptions = l.staff.map((s) => ({ value: s.id, label: s.name }));
  let fields: any[] = [],
    initial: Row = {},
    path = "",
    title = "",
    transform = (d: Row): Row => d;
  const base = { ...row };
  if (type === "customers") {
    title = row.id ? "ویرایش پرونده مشتری" : "مشتری جدید";
    path = "/customers";
    initial = {
      firstName: row.first_name || "",
      lastName: row.last_name || "",
      mobile: row.mobile || "",
      alternateMobile: row.alternate_mobile || "",
      nationalCode: row.national_code || "",
      status: row.status || (row.active === false ? "INACTIVE" : "ACTIVE"),
      contactChannel: row.contact_channel || "SMS",
      birthDate: row.birth_date?.slice(0, 10) || "",
      gender: row.gender || "",
      maritalStatus: row.marital_status || "",
      education: row.education || "",
      occupation: row.occupation || "",
      address: row.address || "",
      city: row.city || "",
      acquisitionChannel: row.acquisition_channel || "OTHER",
      referrerId: row.referrer_id || "",
      preferredStaffId: row.preferred_staff_id || "",
      marketingConsent: row.marketing_consent || false,
      notes: row.notes || "",
      active: row.active ?? true,
    };
    fields = [
      field("firstName", "نام"),
      field("lastName", "نام خانوادگی"),
      field("mobile", "شماره موبایل", "tel"),
      field("nationalCode", "کد ملی", "text", false, { max: 10 }),
      field("status", "وضعیت پرونده", "select", true, {
        options: [
          { value: "ACTIVE", label: "فعال" },
          { value: "INACTIVE", label: "غیرفعال" },
          { value: "BLOCKED", label: "مسدود" },
        ],
      }),
      field("contactChannel", "روش تماس ترجیحی", "select", true, {
        options: [
          { value: "SMS", label: "پیامک" },
          { value: "PHONE", label: "تماس تلفنی" },
          { value: "WHATSAPP", label: "واتساپ" },
        ],
      }),
      field("alternateMobile", "موبایل جایگزین", "tel", false),
      field("birthDate", "تاریخ تولد", "date", false),
      field("gender", "جنسیت", "select", false, {
        options: options(["زن", "مرد", "سایر"]),
      }),
      field("maritalStatus", "وضعیت تأهل", "select", false, {
        options: options(["مجرد", "متأهل"]),
      }),
      field("occupation", "شغل", "text", false),
      field("education", "تحصیلات", "text", false),
      field("city", "شهر", "text", false),
      field("acquisitionChannel", "کانال آشنایی", "select", true, {
        options: options(["INSTAGRAM", "REFERRAL", "OTHER"]),
      }),
      field("preferredStaffId", "متخصص مورد علاقه", "select", false, {
        options: staffOptions,
      }),
      field("referrerId", "مشتری معرف", "select", false, {
        options: customerOptions.filter((c) => c.value !== row.id),
      }),
      field("address", "نشانی", "text", false, { wide: true, max: 500 }),
      field("notes", "یادداشت پرونده", "textarea", false, {
        wide: true,
        max: 2000,
      }),
      field(
        "marketingConsent",
        "رضایت دریافت پیام تبلیغاتی",
        "checkbox",
        false,
      ),
    ];
    transform = (d) => ({
      ...d,
      referrerId: n(d.referrerId),
      preferredStaffId: n(d.preferredStaffId),
      birthDate: d.birthDate || null,
      active: d.status === "ACTIVE",
    });
  } else if (type === "services") {
    path = "/services";
    title = "اطلاعات خدمت";
    initial = {
      name: row.name || "",
      category: row.category || "زیبایی",
      description: row.description || "",
      price: row.price || 0,
      materialCost: row.material_cost || 0,
      durationMinutes: row.duration_minutes || 60,
      bufferMinutes: row.buffer_minutes || 0,
      active: row.active ?? true,
    };
    fields = [
      field("name", "نام خدمت"),
      field("category", "دسته‌بندی"),
      field("price", "قیمت (تومان)", "number", true, { min: 0 }),
      field("materialCost", "هزینه مواد (تومان)", "number", true, { min: 0 }),
      field("durationMinutes", "مدت خدمت (دقیقه)", "number", true, {
        min: 5,
        max: 480,
      }),
      field("bufferMinutes", "فاصله پس از خدمت (دقیقه)", "number", true, {
        min: 0,
        max: 120,
      }),
      field("description", "توضیحات", "textarea", false, {
        wide: true,
        max: 500,
      }),
      field("active", "خدمت فعال", "checkbox", false),
    ];
  } else if (type === "products") {
    path = "/products";
    title = "اطلاعات محصول";
    initial = {
      name: row.name || "",
      category: row.category || "مراقبت زیبایی",
      price: row.price || 0,
      cost: row.cost || 0,
      minimumStock: row.minimum_stock ?? 3,
      supplier: row.supplier || "",
      active: row.active ?? true,
    };
    fields = [
      field("name", "نام محصول"),
      field("category", "دسته‌بندی"),
      field("price", "قیمت فروش (تومان)", "number", true, { min: 0 }),
      field("cost", "بهای خرید (تومان)", "number", true, { min: 0 }),
      field("minimumStock", "حداقل موجودی", "number", true, { min: 0 }),
      field("supplier", "تأمین‌کننده", "text", false),
      field("active", "محصول فعال", "checkbox", false),
    ];
  } else if (type === "stock") {
    path = "/products/" + row.id + "/stock";
    title = "اصلاح موجودی · " + row.name;
    initial = { quantity: 1, reason: "" };
    fields = [
      field("quantity", "تغییر تعداد (+ ورود، − خروج)", "number"),
      field("reason", "علت تغییر"),
    ];
  } else if (type === "expenses") {
    path = "/expenses";
    title = "ثبت هزینه";
    initial = {
      amount: 0,
      category: "هزینه عملیاتی",
      accountId: l.accounts[0]?.id || "",
    };
    fields = [
      field("title", "شرح هزینه"),
      field("category", "دسته‌بندی"),
      field("amount", "مبلغ (تومان)", "number", true, { min: 1 }),
      field("accountId", "حساب پرداخت", "select", true, {
        options: l.accounts.map((a) => ({ value: a.id, label: a.name })),
      }),
    ];
    transform = (d) => ({ ...d, accountId: Number(d.accountId) });
  } else if (type === "campaigns") {
    path = "/campaigns";
    title = "کمپین جدید";
    initial = { segment: "AT_RISK", budget: 0 };
    fields = [
      field("name", "نام کمپین"),
      field("segment", "گروه مخاطبان", "select", true, {
        options: options(["ALL", "LOYAL", "GROWING", "AT_RISK", "LOST"]),
      }),
      field("objective", "هدف کمپین", "text", false),
      field("budget", "بودجه (تومان)", "number", true, { min: 0 }),
      field("body", "متن پیام", "textarea", true, {
        wide: true,
        help: "فقط مشتریان فعال با رضایت بازاریابی به صف اضافه می‌شوند.",
      }),
    ];
  } else if (type === "events") {
    path = "/events";
    title = "رویداد جدید";
    initial = { capacity: 30, budget: 0 };
    fields = [
      field("name", "عنوان رویداد"),
      field("venue", "محل برگزاری"),
      field("startsAt", "شروع (ساعت تهران)", "datetime"),
      field("endsAt", "پایان (ساعت تهران)", "datetime"),
      field("capacity", "ظرفیت", "number", true, { min: 1 }),
      field("budget", "بودجه (تومان)", "number", true, { min: 0 }),
      field("description", "توضیحات", "textarea", false, { wide: true }),
    ];
    transform = (d) => ({
      ...d,
      startsAt: d.startsAt + ":00+03:30",
      endsAt: d.endsAt + ":00+03:30",
    });
  } else if (type === "points") {
    path = "/loyalty/" + row.id + "/points";
    title = "امتیاز باشگاه · " + row.name;
    initial = { points: 1, reason: "" };
    fields = [
      field("points", "تغییر امتیاز (+ یا −)", "number"),
      field("reason", "علت تغییر"),
    ];
  } else if (type === "settings") {
    path = "/settings";
    title = "تنظیمات سازمان";
    initial = {
      organization: row.organization,
      plan: row.plan,
      expiresOn: row.expires_on || "",
      pointUnit: row.point_unit,
      riskDays: row.risk_days,
      loyalVisits: row.loyal_visits,
    };
    fields = [
      field("organization", "نام سازمان"),
      field("plan", "سطح امکانات", "select", true, {
        options: options(["BASIC", "STANDARD", "ADVANCED", "SMART"]),
      }),
      field("expiresOn", "انقضای لایسنس", "date", false),
      field("pointUnit", "تومان لازم برای هر امتیاز", "number", true, {
        min: 1,
      }),
      field("riskDays", "آستانه ریزش (روز)", "number", true, {
        min: 1,
        max: 730,
      }),
      field("loyalVisits", "حداقل مراجعه مشتری وفادار", "number", true, {
        min: 2,
        max: 100,
      }),
    ];
    transform = (d) => ({ ...d, expiresOn: d.expiresOn || null });
  } else if (type === "users") {
    path = "/users";
    title = "کاربر جدید";
    initial = { role: "RECEPTION", branchId: 1 };
    fields = [
      field("name", "نام و نام خانوادگی"),
      field("username", "نام کاربری"),
      field("password", "رمز (حداقل ۱۲ کاراکتر)", "password"),
      field("role", "نقش", "select", true, {
        options: options([
          "ADMIN",
          "MANAGER",
          "RECEPTION",
          "STYLIST",
          "MARKETING",
          "FINANCE",
        ]),
      }),
      field("branchId", "شناسه شعبه", "number", true, { min: 1 }),
      field("staffId", "پرسنل مرتبط (برای آرایشگر)", "select", false, {
        options: staffOptions,
      }),
    ];
    transform = (d) => ({
      ...d,
      staffId: n(d.staffId),
      branchId: Number(d.branchId),
    });
  } else if (type === "password") {
    path = "/auth/password";
    title = "تغییر رمز عبور";
    fields = [
      field("currentPassword", "رمز فعلی", "password"),
      field("newPassword", "رمز جدید (حداقل ۱۲ کاراکتر)", "password"),
    ];
  } else if (type === "userPassword") {
    path = "/users/" + row.id + "/password";
    title = "تغییر رمز · " + (row.name || row.username || "کاربر");
    fields = [field("password", "رمز جدید (حداقل ۱۲ کاراکتر)", "password")];
  } else if (type === "leave") {
    path = "/staff/" + row.id + "/leave";
    title = "ثبت مرخصی · " + row.name;
    fields = [
      field("startsAt", "شروع مرخصی", "datetime"),
      field("endsAt", "پایان مرخصی", "datetime"),
      field("reason", "توضیحات", "text", false),
    ];
    transform = (d) => ({
      ...d,
      startsAt: d.startsAt + ":00+03:30",
      endsAt: d.endsAt + ":00+03:30",
    });
  } else if (type === "preference") {
    path = "/customers/" + row.id + "/preferences";
    title = "ثبت علاقه یا ترجیح مشتری";
    fields = [field("category", "نوع ترجیح"), field("value", "مقدار")];
  } else if (type === "occasion") {
    path = "/customers/" + row.id + "/occasions";
    title = "ثبت مناسبت مشتری";
    fields = [field("title", "عنوان مناسبت"), field("date", "تاریخ", "date")];
  } else if (type === "branches") {
    path = "/branches";
    title = "شعبه جدید";
    fields = [field("name", "نام شعبه")];
  }
  return (
    <Editor
      title={title}
      fields={fields}
      initial={initial}
      onClose={onClose}
      submit={async (d) => {
        const edit =
          row.id && ["customers", "services", "products"].includes(type);
        await api(
          path + (edit ? "/" + row.id : ""),
          edit || type === "settings" ? "PUT" : "POST",
          transform(d),
        );
        onDone();
      }}
    />
  );
}
