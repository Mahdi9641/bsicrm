"use client";
import { Badge, Logo, Table } from "@/components/ui";
import { api, date, money, Row } from "@/lib/api";
import { use, useEffect, useState } from "react";
export default function Invoice({
  params,
}: {
  params: Promise<{ token: string }>;
}) {
  const { token } = use(params);
  const [data, setData] = useState<Row | null>(null),
    [error, setError] = useState("");
  useEffect(() => {
    api(
      "/public/invoices/" + encodeURIComponent(token),
      "GET",
      undefined,
      false,
    )
      .then(setData)
      .catch((e) => setError(e.message));
  }, [token]);
  return (
    <main className="public-invoice">
      <Logo />
      {error ? (
        <p className="error">این لینک معتبر نیست یا منقضی شده است.</p>
      ) : !data ? (
        <p>در حال دریافت فاکتور…</p>
      ) : (
        <>
          <div className="profile-heading" style={{ marginTop: 30 }}>
            <div>
              <h2>{data.organization}</h2>
              <p>
                فاکتور BS-{data.invoice.id} · {date(data.invoice.created_at)}
              </p>
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
            <span>مبلغ کل با مالیات</span>
            <strong>{money(data.invoice.total)}</strong>
          </div>
          <button
            className="primary"
            style={{ marginTop: 20 }}
            onClick={() => window.print()}
          >
            چاپ فاکتور
          </button>
        </>
      )}
    </main>
  );
}
