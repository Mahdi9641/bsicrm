"use client";
import { Can } from "@/components/Permissions";
import { Badge, Modal, Table } from "@/components/ui";
import { avatar } from "@/features/workspace/shared";
import CustomerRelations from "./CustomerRelations";
import { date, fa, money, labels, Row } from "@/lib/api";
export default function CustomerProfile({
  profile,
  setProfile,
  operations,
  setDialog,
}: {
  profile: Row;
  setProfile: (v: Row | null) => void;
  operations: boolean;
  setDialog: (v: Row | null) => void;
}) {
  return (
    <Modal title="پرونده ۳۶۰ درجه مشتری" onClose={() => setProfile(null)} wide>
      <div className="profile-heading">
        <div className="person">
          <span className="avatar large">
            {avatar(profile.customer.first_name)}
          </span>
          <div>
            <h2>
              {profile.customer.first_name} {profile.customer.last_name}
            </h2>
            <small dir="ltr">{profile.customer.mobile}</small>
          </div>
        </div>
        <span className="subtle-chip">
          {profile.customer.customer_code || `CUS-${profile.customer.id}`}
        </span>
      </div>
      <dl className="detail-list">
        <dt>کد ملی</dt>
        <dd dir="ltr">{profile.customer.national_code || "—"}</dd>
        <dt>وضعیت</dt>
        <dd>
          {profile.customer.status === "BLOCKED"
            ? "مسدود"
            : profile.customer.active
              ? "فعال"
              : "غیرفعال"}
        </dd>
        <dt>روش تماس</dt>
        <dd>
          {
            ({ SMS: "پیامک", PHONE: "تلفن", WHATSAPP: "واتساپ" } as Row)[
              profile.customer.contact_channel
            ]
          }
        </dd>
        <dt>شهر</dt>
        <dd>{profile.customer.city || "—"}</dd>
        <dt>تولد</dt>
        <dd>{date(profile.customer.birth_date)}</dd>
        <dt>رضایت تبلیغاتی</dt>
        <dd>{profile.customer.marketing_consent ? "ثبت شده" : "ندارد"}</dd>
        <dt>یادداشت</dt>
        <dd>{profile.customer.notes || "—"}</dd>
      </dl>
      {profile.summary && (
        <>
          <h3>خلاصهٔ ارتباط و ارزش مشتری</h3>
          <dl className="detail-list">
            <dt>مراجعات تکمیل‌شده</dt>
            <dd>{fa(profile.summary.visits)}</dd>
            <dt>اولین مراجعه</dt>
            <dd>{date(profile.summary.first_visit)}</dd>
            <dt>آخرین مراجعه</dt>
            <dd>{date(profile.summary.last_visit)}</dd>
            <Can permission="invoices.read"><dt>مجموع خرید</dt>
            <dd>{money(profile.summary.total_spend)}</dd>
            <dt>خرید به ازای مراجعه</dt>
            <dd>{money(profile.summary.average_spend)}</dd>
            <dt>مانده بدهی</dt>
            <dd>{money(profile.summary.debt)}</dd>
            </Can><Can permission="finance.read"><dt>سود مشارکتی</dt>
            <dd>{money(profile.summary.contribution)}</dd>
            </Can><Can permission="loyalty.read"><dt>امتیاز و سطح</dt>
            <dd>
              {fa(profile.summary.points)} ·{" "}
              {labels[profile.summary.tier] || profile.summary.tier}
            </dd>
            </Can><Can permission="rfm.read"><dt>خوشه RFM</dt>
            <dd>
              {labels[profile.summary.segment] || profile.summary.segment}
            </dd>
            </Can><Can permission="appointments.read"><dt>لغو / عدم حضور</dt>
            <dd>
              {fa(profile.summary.cancelled)} / {fa(profile.summary.no_shows)}
            </dd>
            <dt>نوبت بعدی</dt>
            <dd>{date(profile.summary.next_appointment, true)}</dd></Can>
          </dl>
        </>
      )}
      <CustomerRelations
        customerId={profile.customer.id}
        initial={profile.relations || []}
        canEdit={operations}
      />
      <div className="profile-tabs">
        <h3>علایق و مناسبت‌ها</h3>
        {operations && (
          <>
            <button
              className="text-button"
              onClick={() => {
                setDialog({ type: "preference", row: profile.customer });
                setProfile(null);
              }}
            >
              + ترجیح
            </button>
            <button
              className="text-button"
              onClick={() => {
                setDialog({ type: "occasion", row: profile.customer });
                setProfile(null);
              }}
            >
              + مناسبت
            </button>
          </>
        )}
      </div>
      <div className="choice-row">
        {profile.preferences.map((p: Row) => (
          <span className="subtle-chip" key={p.id}>
            {p.category}: {p.value}
          </span>
        ))}
        {profile.occasions.map((o: Row) => (
          <span className="subtle-chip" key={o.id}>
            {o.title}: {date(o.occasion_date)}
          </span>
        ))}
      </div>
      <Can permission="appointments.read"><h3>سوابق نوبت</h3>
      <Table
        rows={profile.appointments}
        columns={[
          {
            key: "starts_at",
            title: "تاریخ و ساعت",
            render: (r) => date(r.starts_at, true),
          },
          {
            key: "status",
            title: "وضعیت",
            render: (r) => <Badge value={r.status} />,
          },
        ]}
      />
      </Can><Can permission="invoices.read"><h3>سوابق فاکتور</h3>
      <Table
        rows={profile.invoices}
        columns={[
          { key: "id", title: "شماره" },
          {
            key: "created_at",
            title: "تاریخ",
            render: (r) => date(r.created_at),
          },
          { key: "total", title: "مبلغ", render: (r) => money(r.total) },
          {
            key: "status",
            title: "وضعیت",
            render: (r) => <Badge value={r.status} />,
          },
        ]}
      />
      </Can><Can permission="loyalty.read"><h3>گردش امتیاز</h3>
      <Table
        rows={profile.loyalty}
        columns={[
          { key: "points", title: "امتیاز", render: (r) => fa(r.points) },
          { key: "reason", title: "شرح" },
          {
            key: "created_at",
            title: "تاریخ",
            render: (r) => date(r.created_at),
          },
        ]}
      />
    </Can></Modal>
  );
}
