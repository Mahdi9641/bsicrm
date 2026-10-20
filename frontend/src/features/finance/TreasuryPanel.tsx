"use client";
import { Editor, field } from "@/components/ui";
import { api, Row, uuid } from "@/lib/api";
import { useState } from "react";
export default function TreasuryPanel({
  accounts,
  done,
}: {
  accounts: Row[];
  done: () => void;
}) {
  const [mode, setMode] = useState(""),
    [key, setKey] = useState("");
  const choices = accounts.map((a) => ({ value: a.id, label: a.name }));
  return (
    <div className="treasury-tools">
      <div>
        <b>گردش شفاف بین حساب‌ها</b>
        <p>
          انتقال، درآمد یا هزینه محسوب نمی‌شود؛ دو ردیف برابر در دفتر ثبت
          می‌شود.
        </p>
      </div>
      <button className="secondary" onClick={() => setMode("account")}>
        حساب و مانده افتتاحیه
      </button>
      <button
        className="primary"
        onClick={() => {
          setKey(uuid());
          setMode("transfer");
        }}
      >
        انتقال بین حساب‌ها
      </button>
      {mode && (
        <Editor
          title={mode === "account" ? "ایجاد حساب" : "انتقال وجه"}
          initial={mode === "account" ? { openingBalance: 0 } : {}}
          fields={
            mode === "account"
              ? [
                  field("name", "نام حساب", "text", true),
                  field(
                    "openingBalance",
                    "مانده افتتاحیه (تومان)",
                    "number",
                    true,
                    { min: 0 },
                  ),
                ]
              : [
                  field("fromAccountId", "حساب مبدا", "select", true, {
                    options: choices,
                  }),
                  field("toAccountId", "حساب مقصد", "select", true, {
                    options: choices,
                  }),
                  field("amount", "مبلغ (تومان)", "number", true, { min: 1 }),
                  field("reason", "شرح انتقال", "textarea", true),
                ]
          }
          onClose={() => setMode("")}
          submit={async (b) => {
            await api(
              mode === "account" ? "/accounts" : "/transfers",
              "POST",
              mode === "account"
                ? b
                : {
                    ...b,
                    fromAccountId: +b.fromAccountId,
                    toAccountId: +b.toAccountId,
                    idempotencyKey: key,
                  },
            );
            done();
          }}
        />
      )}
    </div>
  );
}
