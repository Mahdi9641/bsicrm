"use client";
import {Can} from "@/components/Permissions";
import { api, date, fa, labels, Row } from "@/lib/api";
import { localDateTime } from "@/lib/dates";
import {
  ArrowLeft,
  CheckCircle2,
  Clock,
  Flag,
  LayoutGrid,
  List,
  MessageSquare,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  UserRound,
} from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { CustomerPicker } from "./BusinessDialogs";
import JalaliInput from "./JalaliInput";
import { Badge, Loading, Modal, Table } from "./ui";

const active = (r: Row) => ["OPEN", "IN_PROGRESS"].includes(r.status);
const overdue = (r: Row) =>
  active(r) && new Date(r.due_at).getTime() < Date.now();

export default function TasksWorkspace({ user }: { user: Row }) {
  const [data, setData] = useState<Row>({
    content: [],
    totalElements: 0,
    totalPages: 0,
  });
  const [summary, setSummary] = useState<Row>({});
  const [assignees, setAssignees] = useState<Row[]>([]);
  const [q, setQ] = useState(""),
    [status, setStatus] = useState("ACTIVE"),
    [mine, setMine] = useState(false),
    [late, setLate] = useState(false),
    [page, setPage] = useState(0);
  const [view, setView] = useState("board"),
    [editor, setEditor] = useState<Row | null>(null),
    [detail, setDetail] = useState<number | null>(null);
  const [busy, setBusy] = useState(false),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(""),
    [revision, setRevision] = useState(0);
  const reload = () => setRevision((v) => v + 1);
  useEffect(() => {
    let live = true;
    setLoading(true);
    setError("");
    const t = setTimeout(() => {
      Promise.all([
        api(
          `/tasks?q=${encodeURIComponent(q)}&status=${status}&mine=${mine}&overdue=${late}&page=${page}&size=30`,
        ),
        api("/tasks/summary"),
        api("/tasks/assignees"),
      ])
        .then(([rows, s, a]) => {
          if (live) {
            setData(rows);
            setSummary(s);
            setAssignees(a);
          }
        })
        .catch((e) => {
          if (live) setError(e.message);
        })
        .finally(() => {
          if (live) setLoading(false);
        });
    }, 180);
    return () => {
      live = false;
      clearTimeout(t);
    };
  }, [q, status, mine, late, page, revision]);
  const change = async (row: Row, next: string) => {
    setBusy(true);
    setError("");
    try {
      await api(`/tasks/${row.id}/status`, "POST", {
        version: row.version,
        status: next,
      });
      reload();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  };
  return (
    <>
      <div className="module-hero">
        <div>
          <span className="eyebrow">ارتباطی که ادامه پیدا می‌کند</span>
          <h2>هیچ پیگیری‌ای از قلم نیفتد.</h2>
          <p>مسئول مشخص، زمان روشن و سابقهٔ هر گفتگو؛ همه در یک برد.</p>
        </div>
        <Can permission="tasks.write"><button className="primary" onClick={() => setEditor({})}>
          <Plus size={18} />
          پیگیری جدید
        </button></Can>
      </div>
      <div className="task-metrics">
        {[
          ["open", "برای انجام", Flag],
          ["in_progress", "در حال پیگیری", Clock],
          ["overdue", "عقب‌افتاده", RefreshCw],
          ["done", "تکمیل‌شده", CheckCircle2],
        ].map(([key, title, Icon]: any) => (
          <button
            key={key}
            className={key === "overdue" ? "urgent" : ""}
            onClick={() => {
              setPage(0);
              setLate(key === "overdue");
              setStatus(
                key === "overdue"
                  ? "ACTIVE"
                  : key === "open"
                    ? "OPEN"
                    : key === "done"
                      ? "DONE"
                      : "IN_PROGRESS",
              );
            }}
          >
            <Icon size={20} />
            <div>
              <b>{fa(summary[key])}</b>
              <span>{title}</span>
            </div>
          </button>
        ))}
      </div>
      <div className="workspace-tools">
        <div className="search-field">
          <Search size={18} />
          <input
            aria-label="جستجوی پیگیری"
            value={q}
            placeholder="عنوان پیگیری یا نام مشتری…"
            onChange={(e) => {
              setQ(e.target.value);
              setPage(0);
            }}
          />
        </div>
        <select
          aria-label="فیلتر وضعیت"
          value={status}
          onChange={(e) => {
            setStatus(e.target.value);
            setPage(0);
          }}
        >
          {["ACTIVE", "ALL", "OPEN", "IN_PROGRESS", "DONE", "CANCELLED"].map(
            (x) => (
              <option key={x} value={x}>
                {x === "ALL"
                  ? "همه وضعیت‌ها"
                  : x === "ACTIVE"
                    ? "پیگیری‌های باز"
                    : labels[x] || x}
              </option>
            ),
          )}
        </select>
        <label className="inline-check">
          <input
            type="checkbox"
            checked={mine}
            onChange={(e) => {
              setMine(e.target.checked);
              setPage(0);
            }}
          />
          کارهای من
        </label>
        <label className="inline-check">
          <input
            type="checkbox"
            checked={late}
            onChange={(e) => {
              setLate(e.target.checked);
              setPage(0);
            }}
          />
          عقب‌افتاده
        </label>
        <div className="segmented">
          <button
            className={view === "board" ? "selected" : ""}
            aria-label="نمای برد"
            onClick={() => setView("board")}
          >
            <LayoutGrid size={17} />
          </button>
          <button
            className={view === "list" ? "selected" : ""}
            aria-label="نمای فهرست"
            onClick={() => setView("list")}
          >
            <List size={17} />
          </button>
        </div>
      </div>
      {error && (
        <div role="alert" className="error">
          {error}
          <button className="text-button" onClick={reload}>
            تلاش دوباره
          </button>
        </div>
      )}
      {loading ? (
        <Loading />
      ) : view === "board" ? (
        <div className="task-board">
          {(status === "CANCELLED"
            ? ["CANCELLED"]
            : [
                "OPEN",
                "IN_PROGRESS",
                "DONE",
                ...(status === "ALL" ? ["CANCELLED"] : []),
              ]
          ).map((column) => {
            const rows = data.content.filter((r: Row) => r.status === column);
            return (
              <section
                className={"task-column column-" + column.toLowerCase()}
                key={column}
              >
                <header>
                  <span className="column-dot" />
                  <h3>{labels[column]}</h3>
                  <span>{fa(rows.length)}</span>
                </header>
                <div className="task-column-body">
                  {!rows.length ? (
                    <p className="column-empty">در این صفحه موردی نیست</p>
                  ) : (
                    rows.map((r: Row) => (
                      <article
                        className={"task-card " + (overdue(r) ? "overdue" : "")}
                        key={r.id}
                      >
                        <div className="task-card-meta">
                          <Badge value={r.priority} />
                          <span dir="ltr">#{r.id}</span>
                        </div>
                        <button
                          className="task-title"
                          onClick={() => setDetail(r.id)}
                        >
                          {r.title}
                        </button>
                        <p>{r.description || "بدون یادداشت"}</p>
                        {r.customer_name && (
                          <div className="task-customer">
                            <UserRound size={14} />
                            {r.customer_name}
                          </div>
                        )}
                        <div className="task-deadline">
                          <Clock size={14} />
                          {date(r.due_at, true)}
                          {overdue(r) && <b>عقب‌افتاده</b>}
                        </div>
                        <footer>
                          <span className="person">
                            <span className="avatar tiny">
                              {r.assignee_name?.charAt(0)}
                            </span>
                            <small>{r.assignee_name}</small>
                          </span>
                          <button
                            className="icon-btn"
                            aria-label="جزئیات و یادداشت‌ها"
                            onClick={() => setDetail(r.id)}
                          >
                            <MessageSquare size={16} />
                          </button>
                        </footer>
                        {active(r) && (
                          <Can permission="tasks.write"><button
                            disabled={busy}
                            className="task-next"
                            onClick={() =>
                              change(
                                r,
                                r.status === "OPEN" ? "IN_PROGRESS" : "DONE",
                              )
                            }
                          >
                            {r.status === "OPEN"
                              ? "شروع پیگیری"
                              : "تکمیل پیگیری"}
                            <ArrowLeft size={15} />
                          </button></Can>
                        )}
                      </article>
                    ))
                  )}
                </div>
              </section>
            );
          })}
        </div>
      ) : (
        <section className="panel">
          <Table
            rows={data.content}
            columns={[
              { key: "title", title: "عنوان" },
              { key: "customer_name", title: "مشتری" },
              { key: "assignee_name", title: "مسئول" },
              {
                key: "due_at",
                title: "موعد",
                render: (r) => (
                  <span className={overdue(r) ? "danger" : ""}>
                    {date(r.due_at, true)}
                  </span>
                ),
              },
              {
                key: "priority",
                title: "اولویت",
                render: (r) => <Badge value={r.priority} />,
              },
              {
                key: "status",
                title: "وضعیت",
                render: (r) => <Badge value={r.status} />,
              },
            ]}
            actions={(r) => (
              <button className="table-button" onClick={() => setDetail(r.id)}>
                جزئیات
              </button>
            )}
          />
        </section>
      )}
      <div className="pagination">
        <span>
          {fa(data.totalElements)} پیگیری · صفحه {fa(page + 1)} از{" "}
          {fa(Math.max(1, data.totalPages))}
        </span>
        <button
          className="secondary"
          disabled={loading || page === 0}
          onClick={() => setPage(page - 1)}
        >
          قبلی
        </button>
        <button
          className="secondary"
          disabled={loading || page + 1 >= data.totalPages}
          onClick={() => setPage(page + 1)}
        >
          بعدی
        </button>
      </div>
      {editor && (
        <TaskEditor
          row={editor}
          user={user}
          assignees={assignees}
          close={() => setEditor(null)}
          done={reload}
        />
      )}
      {detail !== null && (
        <TaskDetail
          id={detail}
          close={() => setDetail(null)}
          edit={(row) => {
            setDetail(null);
            setEditor(row);
          }}
          done={reload}
        />
      )}
    </>
  );
}

function TaskEditor({
  row,
  user,
  assignees,
  close,
  done,
}: {
  row: Row;
  user: Row;
  assignees: Row[];
  close: () => void;
  done: () => void;
}) {
  const [form, setForm] = useState<Row>({
    title: row.title || "",
    description: row.description || "",
    customerId: row.customer_id || 0,
    assignedTo: row.assigned_to || user.id,
    dueAt: row.due_at
      ? localDateTime(row.due_at)
      : localDateTime(new Date(Date.now() + 3600000).toISOString()),
    priority: row.priority || "NORMAL",
  });
  const [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  const change = (key: string, value: unknown) =>
    setForm((f) => ({ ...f, [key]: value }));
  return (
    <Modal
      title={row.id ? "ویرایش پیگیری" : "پیگیری جدید"}
      onClose={close}
      wide
    >
      <Can permission="tasks.write"><form
        onSubmit={async (e) => {
          e.preventDefault();
          setBusy(true);
          setError("");
          try {
            await api(
              "/tasks" + (row.id ? "/" + row.id : ""),
              row.id ? "PUT" : "POST",
              {
                ...form,
                customerId: form.customerId || null,
                assignedTo: +form.assignedTo,
                dueAt: form.dueAt + ":00+03:30",
                version: row.version,
              },
            );
            done();
            close();
          } catch (e) {
            setError((e as Error).message);
          } finally {
            setBusy(false);
          }
        }}
      >
        <div className="form-grid">
          <label className="full">
            <span>عنوان پیگیری</span>
            <input
              required
              autoFocus
              maxLength={160}
              value={form.title}
              onChange={(e) => change("title", e.target.value)}
              placeholder="مثلاً تماس برای رضایت‌سنجی پس از مراجعه"
            />
          </label>
          <label>
            <span>مسئول پیگیری</span>
            <select
              required
              value={form.assignedTo}
              onChange={(e) => change("assignedTo", +e.target.value)}
            >
              {assignees.map((a) => (
                <option value={a.id} key={a.id}>
                  {a.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            <span>اولویت</span>
            <select
              value={form.priority}
              onChange={(e) => change("priority", e.target.value)}
            >
              {["LOW", "NORMAL", "HIGH", "URGENT"].map((v) => (
                <option value={v} key={v}>
                  {labels[v]}
                </option>
              ))}
            </select>
          </label>
          <label>
            <span>موعد · ساعت تهران</span>
            <JalaliInput
              value={form.dueAt}
              onChange={(v) => change("dueAt", v)}
              time
              required
            />
          </label>
          <div>
            <label>
              <span>مشتری مرتبط · اختیاری</span>
            </label>
            <CustomerPicker
              optional
              value={form.customerId}
              onChange={(id) => change("customerId", id)}
            />
            {form.customerId > 0 && (
              <Can permission="tasks.write"><button
                type="button"
                className="text-button"
                onClick={() => change("customerId", 0)}
              >
                حذف ارتباط با مشتری
              </button></Can>
            )}
          </div>
          <label className="full">
            <span>جزئیات</span>
            <textarea
              maxLength={2000}
              value={form.description}
              onChange={(e) => change("description", e.target.value)}
              placeholder="اقدام مورد انتظار و نکات مهم را بنویسید…"
            />
          </label>
        </div>
        {error && (
          <p className="error" role="alert">
            {error}
          </p>
        )}
        <div className="modal-actions">
          <button className="primary" disabled={busy}>
            {busy ? "در حال ذخیره…" : "ذخیره پیگیری"}
          </button>
          <button
            type="button"
            className="secondary"
            disabled={busy}
            onClick={close}
          >
            انصراف
          </button>
        </div>
      </form></Can>
    </Modal>
  );
}

function TaskDetail({
  id,
  close,
  edit,
  done,
}: {
  id: number;
  close: () => void;
  edit: (r: Row) => void;
  done: () => void;
}) {
  const [data, setData] = useState<Row | null>(null),
    [note, setNote] = useState(""),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  const load = useCallback(
    () =>
      api("/tasks/" + id)
        .then(setData)
        .catch((e) => setError(e.message)),
    [id],
  );
  useEffect(() => {
    load();
  }, [load]);
  const status = async (next: string) => {
    if (!data) return;
    setBusy(true);
    setError("");
    try {
      await api(`/tasks/${id}/status`, "POST", {
        version: data.task.version,
        status: next,
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
    <Modal title="جزئیات پیگیری" onClose={close} wide>
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
      {!data ? (
        <Loading />
      ) : (
        <>
          <div className="task-detail-heading">
            <div>
              <Badge value={data.task.priority} />
              <h2>{data.task.title}</h2>
              <p>{data.task.description}</p>
            </div>
            <Badge value={data.task.status} />
          </div>
          <div className="detail-chips">
            <span>
              <UserRound size={15} />
              {data.task.assignee_name}
            </span>
            <span>
              <Clock size={15} />
              {date(data.task.due_at, true)}
            </span>
            {data.task.customer_name && <span>{data.task.customer_name}</span>}
          </div>
          <div className="detail-actions">
            {active(data.task) ? (
              <>
                <Can permission="tasks.write"><button
                  disabled={busy}
                  className="primary"
                  onClick={() =>
                    status(data.task.status === "OPEN" ? "IN_PROGRESS" : "DONE")
                  }
                >
                  {data.task.status === "OPEN" ? "شروع پیگیری" : "ثبت تکمیل"}
                </button></Can>
                <Can permission="tasks.write"><button
                  disabled={busy}
                  className="secondary"
                  onClick={() => edit(data.task)}
                >
                  <Pencil size={15} />
                  ویرایش
                </button></Can>
                <Can permission="tasks.write"><button
                  disabled={busy}
                  className="text-button danger"
                  onClick={() => status("CANCELLED")}
                >
                  بستن بدون انجام
                </button></Can>
              </>
            ) : (
              <Can permission="tasks.write"><button
                disabled={busy}
                className="secondary"
                onClick={() => status("OPEN")}
              >
                بازگشایی پیگیری
              </button></Can>
            )}
          </div>
          <h3 className="section-label">روند پیگیری و یادداشت‌ها</h3>
          <div className="activity-timeline">
            {data.events.map((e: Row) => (
              <div className="timeline-entry" key={e.id}>
                <span className="timeline-dot" />
                <div>
                  <header>
                    <b>{e.author}</b>
                    <small>{date(e.created_at, true)}</small>
                  </header>
                  <p>
                    {e.kind === "STATUS"
                      ? "وضعیت به «" + labels[e.body] + "» تغییر کرد"
                      : e.body}
                  </p>
                </div>
              </div>
            ))}
          </div>
          <Can permission="tasks.write"><form
            className="comment-form"
            onSubmit={async (e) => {
              e.preventDefault();
              setBusy(true);
              setError("");
              try {
                await api(`/tasks/${id}/comments`, "POST", { body: note });
                setNote("");
                await load();
              } catch (e) {
                setError((e as Error).message);
              } finally {
                setBusy(false);
              }
            }}
          >
            <label>
              <span>نتیجه تماس یا یادداشت جدید</span>
              <textarea
                required
                maxLength={2000}
                value={note}
                onChange={(e) => setNote(e.target.value)}
                placeholder="نتیجه پیگیری را برای همکاران ثبت کنید…"
              />
            </label>
            <Can permission="tasks.write"><button disabled={busy || !note.trim()} className="primary">
              <MessageSquare size={16} />
              ثبت یادداشت
            </button></Can>
          </form></Can>
        </>
      )}
    </Modal>
  );
}
