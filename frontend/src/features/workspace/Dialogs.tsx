"use client";
import {
  BookingDialog,
  EntityDialog,
  InvoiceDetail,
  InvoiceDialog,
  Lookups,
  StaffDialog,
} from "@/components/BusinessDialogs";
import { Editor, field, options } from "@/components/ui";
import GrowthDialog from "@/features/engagement/GrowthDialog";
import { api, Row } from "@/lib/api";
import Confirm from "./Confirm";
import Participants from "./Participants";
import WalkIn from "./WalkIn";
export default function Dialogs({
  dialog: d,
  lookups: l,
  close,
  done,
  user,
  onLogout,
}: {
  dialog: Row;
  lookups: Lookups;
  close: () => void;
  done: () => void;
  user: Row;
  onLogout: () => void;
}) {
  if (d.type === "growth")
    return (
      <GrowthDialog type={d.source} row={d.row} user={user} close={close} />
    );
  if (d.type === "booking")
    return (
      <BookingDialog
        appointmentId={d.appointmentId}
        lookups={l}
        onClose={close}
        onDone={done}
      />
    );
  if (d.type === "staff")
    return (
      <StaffDialog row={d.row} lookups={l} onClose={close} onDone={done} />
    );
  if (d.type === "invoice")
    return (
      <InvoiceDialog
        lookups={l}
        visit={d.visit}
        onClose={close}
        onDone={done}
      />
    );
  if (d.type === "invoiceDetail")
    return (
      <InvoiceDetail id={d.id} lookups={l} onClose={close} onDone={done} />
    );
  if (d.type === "walkin")
    return <WalkIn lookups={l} close={close} done={done} />;
  if (d.type === "participants")
    return <Participants event={d.row} lookups={l} close={close} done={done} />;
  if (d.type === "cancelAppointment")
    return (
      <Editor
        title="تغییر وضعیت نوبت"
        fields={[
          field("status", "وضعیت", "select", true, {
            options: options(["CANCELLED", "NO_SHOW"]),
          }),
          field("reason", "علت"),
        ]}
        initial={{ status: "CANCELLED" }}
        onClose={close}
        submit={async (b) => {
          await api("/appointments/" + d.row.id + "/status", "POST", b);
          done();
        }}
      />
    );
  if (d.type === "voidInvoice")
    return (
      <Editor
        title="ابطال کامل فاکتور و ثبت برگشت وجه"
        fields={[
          field("reason", "علت ابطال", "textarea", true, {
            help: "تمام پرداخت‌ها برگشت داده می‌شوند، موجودی محصول و امتیاز باشگاه اصلاح می‌شود. این عملیات باید با برگشت واقعی وجه هماهنگ باشد.",
          }),
        ]}
        onClose={close}
        submit={async (b) => {
          await api("/invoices/" + d.row.id + "/void", "POST", b);
          done();
        }}
      />
    );
  if (d.type === "settlement")
    return (
      <Editor
        title="تسویه کمیسیون"
        initial={{ accountId: l.accounts[0]?.id }}
        fields={[
          field("accountId", "حساب پرداخت", "select", true, {
            options: l.accounts.map((a) => ({ value: a.id, label: a.name })),
          }),
        ]}
        onClose={close}
        submit={async (b) => {
          await api("/commissions/" + d.row.id + "/status", "POST", {
            status: "SETTLED",
            accountId: +b.accountId,
          });
          done();
        }}
      />
    );
  if (d.type === "visitService")
    return (
      <Editor
        title="افزودن خدمت به مراجعه"
        fields={[
          field("serviceId", "خدمت", "select", true, {
            options: l.services
              .filter((s) => s.active)
              .map((s) => ({ value: s.id, label: s.name })),
          }),
          field("staffId", "متخصص", "select", true, {
            options: l.staff
              .filter((s) => s.active)
              .map((s) => ({ value: s.id, label: s.name })),
          }),
        ]}
        onClose={close}
        submit={async (b) => {
          await api("/visits/" + d.row.id + "/services", "POST", {
            serviceId: +b.serviceId,
            staffId: +b.staffId,
          });
          done();
        }}
      />
    );
  if (d.type === "queueCampaign" || d.type === "deactivateUser")
    return (
      <Confirm
        title={
          d.type === "queueCampaign" ? "آماده‌سازی کمپین" : "غیرفعال‌سازی حساب"
        }
        text={
          d.type === "queueCampaign"
            ? "مخاطبان واجد شرایط به صف اضافه می‌شوند. اگر سرویس پیامک فعال باشد، ارسال در اجرای بعدی آغاز خواهد شد."
            : "دسترسی این کاربر و نشست‌های فعال او متوقف خواهد شد."
        }
        close={close}
        confirm={async () => {
          await api(
            d.type === "queueCampaign"
              ? "/campaigns/" + d.row.id + "/queue"
              : "/users/" + d.row.id + "/deactivate",
            "POST",
          );
          done();
          close();
        }}
      />
    );
  return (
    <EntityDialog
      type={d.type}
      row={d.row}
      lookups={l}
      onClose={close}
      onDone={() => {
        done();
        if (d.type === "password") onLogout();
      }}
    />
  );
}
