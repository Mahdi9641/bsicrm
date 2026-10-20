"use client";
import { CustomerPicker, Lookups } from "@/components/BusinessDialogs";
import { Badge, Modal, Table } from "@/components/ui";
import { api, Row } from "@/lib/api";
import { useEffect, useState } from "react";
export default function Participants({
  event,
  lookups: l,
  close,
  done,
}: {
  event: Row;
  lookups: Lookups;
  close: () => void;
  done: () => void;
}) {
  const [rows, setRows] = useState<Row[]>([]),
    [customer, setCustomer] = useState(0),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  const load = () =>
    api("/events/" + event.id + "/participants")
      .then(setRows)
      .catch((e) => setError(e.message));
  useEffect(() => {
    load();
  }, []);
  const change = async (id: number, status: string) => {
    setBusy(true);
    try {
      await api("/events/" + event.id + "/participants", "POST", {
        customerId: id,
        status,
      });
      await load();
      done();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  };
  return (
    <Modal title={"مهمانان · " + event.name} onClose={close} wide>
      <form
        onSubmit={async (e) => {
          e.preventDefault();
          await change(customer, "REGISTERED");
        }}
      >
        <div className="line-row">
          <CustomerPicker
            value={customer}
            onChange={setCustomer}
            initial={l.customers}
          />
          <button className="primary" disabled={busy}>
            ثبت مهمان
          </button>
        </div>
      </form>
      {error && <p className="error">{error}</p>}
      <Table
        rows={rows}
        columns={[
          { key: "name", title: "مهمان" },
          {
            key: "status",
            title: "وضعیت",
            render: (r) => <Badge value={r.status} />,
          },
        ]}
        actions={(r) => (
          <>
            {r.status === "REGISTERED" && (
              <button
                className="table-button"
                disabled={busy}
                onClick={() => change(r.customer_id, "ATTENDED")}
              >
                ثبت حضور
              </button>
            )}
            {r.status !== "CANCELLED" && (
              <button
                className="text-button danger"
                disabled={busy}
                onClick={() => change(r.customer_id, "CANCELLED")}
              >
                لغو
              </button>
            )}
          </>
        )}
      />
    </Modal>
  );
}
