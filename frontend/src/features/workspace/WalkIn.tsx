"use client";
import { CustomerPicker, Lookups } from "@/components/BusinessDialogs";
import { Modal } from "@/components/ui";
import { api } from "@/lib/api";
import { useState } from "react";
export default function WalkIn({
  lookups: l,
  close,
  done,
}: {
  lookups: Lookups;
  close: () => void;
  done: () => void;
}) {
  const [customer, setCustomer] = useState(0),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  return (
    <Modal title="پذیرش بدون نوبت" onClose={close}>
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          setBusy(true);
          try {
            await api("/visits", "POST", {
              customerId: customer,
              priority: "NORMAL",
            });
            done();
            close();
          } catch (e) {
            setError((e as Error).message);
            setBusy(false);
          }
        }}
      >
        <CustomerPicker
          value={customer}
          onChange={setCustomer}
          initial={l.customers}
        />
        {error && <p className="error">{error}</p>}
        <div className="modal-actions">
          <button className="primary" disabled={busy}>
            ثبت ورود مشتری
          </button>
        </div>
      </form>
    </Modal>
  );
}
