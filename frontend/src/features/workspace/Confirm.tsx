"use client";
import { Modal } from "@/components/ui";
import { useState } from "react";
export default function Confirm({
  title,
  text,
  close,
  confirm,
}: {
  title: string;
  text: string;
  close: () => void;
  confirm: () => Promise<void>;
}) {
  const [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  return (
    <Modal title={title} onClose={close}>
      <p>{text}</p>
      {error && <p className="error">{error}</p>}
      <div className="modal-actions">
        <button
          className="primary"
          disabled={busy}
          onClick={async () => {
            setBusy(true);
            try {
              await confirm();
            } catch (e) {
              setError((e as Error).message);
              setBusy(false);
            }
          }}
        >
          تأیید و ادامه
        </button>
        <button className="secondary" onClick={close}>
          انصراف
        </button>
      </div>
    </Modal>
  );
}
