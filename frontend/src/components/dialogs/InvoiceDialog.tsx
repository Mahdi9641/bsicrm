"use client";
import { api, fa, money, Row } from "@/lib/api";
import { Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import { Modal } from "../ui";
import { CustomerPicker } from "./CustomerPicker";
import type { Lookups } from "./types";
const n = (v: unknown) => (v === "" || v == null ? null : Number(v));
export function InvoiceDialog({
  lookups: l,
  visit,
  onClose,
  onDone,
}: {
  lookups: Lookups;
  visit?: Row;
  onClose: () => void;
  onDone: () => void;
}) {
  const [customer, setCustomer] = useState(visit?.customer_id || 0),
    [tax, setTax] = useState(0),
    [items, setItems] = useState<Row[]>(
      visit
        ? visit.items
            .filter((i: Row) => i.status === "COMPLETED")
            .map((i: Row) => ({
              kind: "SERVICE",
              itemId: i.service_id,
              staffId: i.staff_id,
              quantity: 1,
              discount: 0,
            }))
        : [
            {
              kind: "SERVICE",
              itemId: "",
              staffId: "",
              quantity: 1,
              discount: 0,
            },
          ],
    ),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  const update = (i: number, key: string, val: any) =>
    setItems(
      items.map((r, j) =>
        j === i
          ? {
              ...r,
              [key]: val,
              ...(key === "kind" ? { itemId: "", staffId: "" } : {}),
            }
          : r,
      ),
    );
  const catalog = (i: Row) => (i.kind === "SERVICE" ? l.services : l.products);
  const price = (i: Row) =>
    Number(catalog(i).find((s) => s.id === +i.itemId)?.price || 0);
  return (
    <Modal title="صدور فاکتور جدید" onClose={onClose} wide>
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          setBusy(true);
          try {
            await api("/invoices", "POST", {
              customerId: customer,
              visitId: visit?.id || null,
              tax,
              items: items.map((i) => ({
                serviceId: i.kind === "SERVICE" ? +i.itemId : null,
                productId: i.kind === "PRODUCT" ? +i.itemId : null,
                staffId: i.kind === "SERVICE" ? +i.staffId : null,
                quantity: +i.quantity,
                discount: +i.discount,
              })),
            });
            onDone();
            onClose();
          } catch (e) {
            setError((e as Error).message);
          } finally {
            setBusy(false);
          }
        }}
      >
        {visit ? (
          <div className="info">
            مراجعه {fa(visit.id)} · {visit.customer_name}
          </div>
        ) : (
          <label>
            <span>مشتری</span>
            <CustomerPicker
              value={customer}
              onChange={setCustomer}
              initial={l.customers}
            />
          </label>
        )}
        <div className="invoice-editor">
          {items.map((item, index) => (
            <div className="invoice-line" key={index}>
              <div className="line-row">
                <select
                  aria-label="نوع ردیف"
                  value={item.kind}
                  onChange={(e) => update(index, "kind", e.target.value)}
                >
                  <option value="SERVICE">خدمت</option>
                  <option value="PRODUCT">محصول</option>
                </select>
                <select
                  aria-label="انتخاب خدمت یا محصول"
                  required
                  value={item.itemId}
                  onChange={(e) => update(index, "itemId", e.target.value)}
                >
                  <option value="">انتخاب مورد</option>
                  {catalog(item)
                    .filter((s) => s.active)
                    .map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name}
                      </option>
                    ))}
                </select>
                {item.kind === "SERVICE" && (
                  <select
                    aria-label="پرسنل ارائه‌دهنده"
                    required
                    value={item.staffId}
                    onChange={(e) => update(index, "staffId", e.target.value)}
                  >
                    <option value="">متخصص</option>
                    {l.staff
                      .filter(
                        (s) =>
                          s.active && s.service_ids?.includes(+item.itemId),
                      )
                      .map((s) => (
                        <option key={s.id} value={s.id}>
                          {s.name}
                        </option>
                      ))}
                  </select>
                )}
                <button
                  type="button"
                  className="icon-btn danger"
                  aria-label="حذف ردیف"
                  disabled={items.length === 1}
                  onClick={() => setItems(items.filter((_, j) => j !== index))}
                >
                  <Trash2 size={16} />
                </button>
              </div>
              <div className="line-row">
                <label>
                  <span>تعداد</span>
                  <input
                    aria-label="تعداد"
                    type="number"
                    min={1}
                    max={1000}
                    required
                    value={item.quantity}
                    onChange={(e) => update(index, "quantity", +e.target.value)}
                  />
                </label>
                <label>
                  <span>تخفیف ردیف (تومان)</span>
                  <input
                    aria-label="تخفیف"
                    type="number"
                    min={0}
                    max={price(item) * item.quantity}
                    required
                    value={item.discount}
                    onChange={(e) => update(index, "discount", +e.target.value)}
                  />
                </label>
                <div className="line-price">
                  <small>مبلغ ردیف</small>
                  <strong>
                    {money(price(item) * item.quantity - item.discount)}
                  </strong>
                </div>
              </div>
            </div>
          ))}
        </div>
        <button
          type="button"
          className="text-button"
          onClick={() =>
            setItems([
              ...items,
              {
                kind: "SERVICE",
                itemId: "",
                staffId: "",
                quantity: 1,
                discount: 0,
              },
            ])
          }
        >
          <Plus size={16} />
          افزودن ردیف
        </button>
        <div className="invoice-total">
          <label>
            <span>مالیات (تومان)</span>
            <input
              type="number"
              min={0}
              value={tax}
              onChange={(e) => setTax(+e.target.value)}
            />
          </label>
          <div>
            <small>جمع قابل پرداخت</small>
            <strong>
              {money(
                items.reduce(
                  (sum, i) => sum + price(i) * i.quantity - i.discount,
                  tax,
                ),
              )}
            </strong>
          </div>
        </div>
        {error && <p className="error">{error}</p>}
        <div className="modal-actions">
          <button className="primary" disabled={busy}>
            صدور و ثبت فاکتور
          </button>
        </div>
      </form>
    </Modal>
  );
}
