"use client";
import { Editor, field, Loading, Modal, Table } from "@/components/ui";
import { api, fa, money, Row } from "@/lib/api";
import { useEffect, useState } from "react";
export default function GrowthDialog({
  type,
  row,
  user,
  close,
}: {
  type: string;
  row: Row;
  user: Row;
  close: () => void;
}) {
  const [data, setData] = useState<Row | null>(null),
    [error, setError] = useState(""),
    [mode, setMode] = useState("");
  const path = "/growth/" + type + "/" + row.id;
  const load = () =>
    api(path)
      .then(setData)
      .catch((e) => setError(e.message));
  useEffect(() => {
    load();
  }, [path]);
  return (
    <Modal title={"نتیجه و بازده · " + row.name} onClose={close} wide>
      {error && <p className="error">{error}</p>}
      {!data ? (
        <Loading />
      ) : (
        <>
          <p className="info">
            انتساب فروش با مدرک ثبت می‌شود. خرید پس از پیام به‌تنهایی دلیل
            انتساب نیست. هر فاکتور فقط به یک کمپین یا رویداد منتسب می‌شود.
          </p>
          <div className="stats-grid">
            {[
              ["فروش منتسب", money(data.revenue)],
              ["هزینه واقعی", money(data.actualCost)],
              ["سود مشارکتی پیش از هزینه جذب", money(data.contribution)],
              [
                "بازده هزینه جذب",
                data.roiPercent == null
                  ? "هزینه ثبت نشده"
                  : fa(data.roiPercent) + "٪",
              ],
            ].map(([label, value]) => (
              <div className="stat" key={label}>
                <small>{label}</small>
                <h3>{value}</h3>
              </div>
            ))}
          </div>
          <p>
            مشتریان تبدیل‌شده: {fa(data.convertedCustomers)} از{" "}
            {fa(data.targets)} · نرخ تبدیل:{" "}
            {data.conversionPercent == null
              ? "—"
              : fa(data.conversionPercent) + "٪"}
          </p>
          <div className="modal-actions">
            {["ADMIN", "MANAGER", "MARKETING"].includes(user.role) && (
              <button
                className="primary"
                onClick={() => setMode("attributions")}
              >
                انتساب فاکتور
              </button>
            )}
            {["ADMIN", "MANAGER", "FINANCE"].includes(user.role) && (
              <button className="secondary" onClick={() => setMode("costs")}>
                انتساب هزینه ثبت‌شده
              </button>
            )}
          </div>
          <h3>فاکتورهای منتسب</h3>
          <Table
            rows={data.invoices}
            columns={[
              { key: "id", title: "فاکتور" },
              { key: "total", title: "مبلغ", render: (r) => money(r.total) },
              {
                key: "collected",
                title: "وصول‌شده",
                render: (r) => money(r.collected),
              },
              { key: "evidence", title: "مدرک انتساب" },
            ]}
          />
          <h3>هزینه‌های واقعی</h3>
          <Table
            rows={data.costs}
            columns={[
              { key: "id", title: "شناسه هزینه" },
              { key: "title", title: "شرح" },
              { key: "amount", title: "مبلغ", render: (r) => money(r.amount) },
            ]}
          />
        </>
      )}
      {mode && (
        <Editor
          title={mode === "costs" ? "انتساب هزینه" : "انتساب فروش"}
          fields={
            mode === "costs"
              ? [
                  field(
                    "expenseId",
                    "شناسه هزینه در بخش مالی",
                    "number",
                    true,
                    { min: 1 },
                  ),
                ]
              : [
                  field(
                    "invoiceId",
                    "شناسه فاکتور مشتری عضو کمپین یا رویداد",
                    "number",
                    true,
                    { min: 1 },
                  ),
                  field(
                    "evidence",
                    "مدرک؛ مانند کد پیشنهاد یا تأیید مشتری",
                    "textarea",
                    true,
                  ),
                ]
          }
          onClose={() => setMode("")}
          submit={async (b) => {
            await api(path + "/" + mode, "POST", b);
            await load();
          }}
        />
      )}
    </Modal>
  );
}
