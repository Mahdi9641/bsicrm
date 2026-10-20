"use client";
import { api, labels, money, Row, uuid } from "@/lib/api";
import { useEffect, useState } from "react";
import { Badge, Editor, field, Modal, options, Table } from "../ui";
import type { Lookups } from "./types";
const n = (v: unknown) => (v === "" || v == null ? null : Number(v));
export function InvoiceDetail({
  id,
  lookups: l,
  onClose,
  onDone,
}: {
  id: number;
  lookups: Lookups;
  onClose: () => void;
  onDone: () => void;
}) {
  const [smsBusy, setSmsBusy] = useState(false),
    [smsQueued, setSmsQueued] = useState(false),
    [smsKey] = useState(() => uuid());
  const [data, setData] = useState<Row | null>(null),
    [error, setError] = useState(""),
    [payment, setPayment] = useState(false),
    [link, setLink] = useState("");
  const load = () =>
    api("/invoices/" + id)
      .then(setData)
      .catch((e) => setError(e.message));
  useEffect(() => {
    load();
  }, [id]);
  if (!data)
    return (
      <Modal title="فاکتور" onClose={onClose}>
        <p>{error || "در حال دریافت…"}</p>
      </Modal>
    );
  const paid = data.payments
      .filter((p: Row) => !p.reversed)
      .reduce((s: number, p: Row) => s + Number(p.amount), 0),
    remaining = Number(data.invoice.total) - paid;
  return (
    <Modal title={"فاکتور BS-" + id} onClose={onClose} wide>
      <div className="invoice-print">
        <div className="profile-heading">
          <div>
            <h3>{data.invoice.customer_name}</h3>
            <small dir="ltr">{data.invoice.mobile}</small>
          </div>
          <Badge value={data.invoice.status} />
        </div>
        <Table
          rows={data.items}
          columns={[
            { key: "description", title: "شرح" },
            { key: "quantity", title: "تعداد" },
            { key: "total", title: "مبلغ", render: (r) => money(r.total) },
          ]}
        />
        <div className="invoice-total">
          <div>
            <small>مالیات</small>
            <strong>{money(data.invoice.tax)}</strong>
          </div>
          <div>
            <small>مبلغ کل</small>
            <strong>{money(data.invoice.total)}</strong>
          </div>
          <div>
            <small>مانده پرداخت</small>
            <strong>
              {money(data.invoice.status === "VOID" ? 0 : remaining)}
            </strong>
          </div>
        </div>
        <h4>پرداخت‌ها</h4>
        <Table
          rows={data.payments}
          columns={[
            { key: "amount", title: "مبلغ", render: (r) => money(r.amount) },
            { key: "method", title: "روش", render: (r) => labels[r.method] },
            { key: "tracking_code", title: "کد پیگیری" },
            {
              key: "reversed",
              title: "وضعیت",
              render: (r) => (r.reversed ? "برگشت شده" : "ثبت شده"),
            },
          ]}
        />
      </div>
      {error && <p className="error">{error}</p>}
      {link && (
        <div className="info">
          <span>لینک فاکتور تا ۲۴ ساعت معتبر است.</span>
          <input
            readOnly
            value={link}
            dir="ltr"
            onFocus={(e) => e.target.select()}
          />
        </div>
      )}
      <div className="modal-actions">
        {data.invoice.status !== "VOID" && (
          <button
            className="secondary"
            disabled={smsBusy || smsQueued}
            onClick={async () => {
              setSmsBusy(true);
              try {
                await api("/invoices/" + id + "/sms", "POST", {
                  idempotencyKey: smsKey,
                });
                setSmsQueued(true);
              } catch (e) {
                setError((e as Error).message);
              } finally {
                setSmsBusy(false);
              }
            }}
          >
            {smsQueued ? "پیامک در صف قرار گرفت" : "ارسال لینک با پیامک"}
          </button>
        )}
        {remaining > 0 && data.invoice.status !== "VOID" && (
          <button className="primary" onClick={() => setPayment(true)}>
            ثبت پرداخت
          </button>
        )}
        <button className="secondary" onClick={() => window.print()}>
          چاپ فاکتور
        </button>
        <button
          className="secondary"
          onClick={async () => {
            try {
              const r = await api("/invoices/" + id + "/link", "POST");
              setLink(location.origin + r.path);
            } catch (e) {
              setError((e as Error).message);
            }
          }}
        >
          لینک امن فاکتور
        </button>
      </div>
      {payment && (
        <Editor
          title="ثبت پرداخت"
          initial={{
            amount: remaining,
            method: "CARD",
            accountId: l.accounts[0]?.id,
            trackingCode: "",
            idempotencyKey: uuid(),
          }}
          fields={[
            field("amount", "مبلغ (تومان)", "number", true, {
              min: 1,
              max: remaining,
            }),
            field("method", "روش پرداخت", "select", true, {
              options: options(["CASH", "CARD", "TRANSFER"]),
            }),
            field("accountId", "حساب دریافت", "select", true, {
              options: l.accounts.map((a) => ({ value: a.id, label: a.name })),
            }),
            field("trackingCode", "شماره پیگیری", "text", false),
          ]}
          onClose={() => setPayment(false)}
          submit={async (d) => {
            await api("/invoices/" + id + "/payments", "POST", {
              ...d,
              accountId: +d.accountId,
            });
            await load();
            onDone();
          }}
        />
      )}
    </Modal>
  );
}
