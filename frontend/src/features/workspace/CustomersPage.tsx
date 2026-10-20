"use client";
import { Table } from "@/components/ui";
import {useState} from "react";
import {FormModal} from "@/features/admin/ManagementPage";
import { api, Row, date, fa, money } from "@/lib/api";
import { Pencil } from "lucide-react";
import { avatar } from "./shared";
import type { WorkspaceContext } from "./types";
export default function CustomersPage({
  rows,
  user,
  operations,
  page,
  setDialog,
  setPage,
  openProfile,
}: WorkspaceContext) {
  const [account,setAccount]=useState<Row|null>(null);
  return (
    <><section className="panel">
      <Table
        rows={rows}
        columns={[
          {
            key: "name",
            title: "مشتری",
            render: (r) => (
              <div className="person">
                <span className="avatar">{avatar(r.first_name)}</span>
                <div>
                  <b>
                    {r.first_name} {r.last_name}
                  </b>
                  <small>{r.customer_code || `CUS-${r.id}`}</small>
                </div>
              </div>
            ),
          },
          {
            key: "mobile",
            title: "شماره تماس",
            render: (r) => <span dir="ltr">{r.mobile}</span>,
          },
          {
            key: "total_spend",
            title: "مجموع خرید",
            render: (r) => money(r.total_spend),
          },
          {
            key: "points",
            title: "امتیاز باشگاه",
            render: (r) => (
              <span className="gold-text">{fa(r.points)} امتیاز</span>
            ),
          },
          {
            key: "last_visit",
            title: "آخرین مراجعه",
            render: (r) => date(r.last_visit),
          },
          {
            key: "active",
            title: "وضعیت",
            render: (r) => (
              <span
                className={"badge " + (r.active ? "b-paid" : "b-cancelled")}
              >
                {r.status === "BLOCKED" ? "مسدود" : r.active ? "فعال" : "غیرفعال"}
              </span>
            ),
          },
        ]}
        actions={(r) => (
          <>
            <button className="table-button" onClick={() => openProfile(r.id)}>
              پرونده ۳۶۰°
            </button>
            {["ADMIN","MANAGER"].includes(user.role)&&r.active&&<button className="text-button" onClick={()=>setAccount(r)}>ساخت حساب ورود</button>}
            {operations && (
              <button
                className="icon-btn"
                aria-label="ویرایش مشتری"
                onClick={() => setDialog({ type: "customers", row: r })}
              >
                <Pencil size={16} />
              </button>
            )}
          </>
        )}
      />
      <div className="pagination">
        <span>صفحه {fa(page + 1)} · حداکثر ۱۰۰ مشتری در هر صفحه</span>
        <button
          className="secondary"
          disabled={!page}
          onClick={() => setPage(page - 1)}
        >
          قبلی
        </button>
        <button
          className="secondary"
          disabled={rows.length < 100}
          onClick={() => setPage(page + 1)}
        >
          بعدی
        </button>
      </div>
    </section>{account&&<FormModal title={"ساخت حساب ورود برای "+account.first_name} initial={{password:""}} fields={[{key:"password",title:"رمز اولیه (حداقل ۱۲ کاراکتر)",type:"password",required:true}]} onSave={async v=>{await api(`/tenancy/customers/${account.id}/account`,"POST",v)}} onClose={()=>setAccount(null)}/>}</>
  );
}
