"use client";
import {canSee} from "@/components/Permissions";
import { fa, Row } from "@/lib/api";
import {
  ArrowUpLeft,
  CalendarDays,
  CheckCircle2,
  ChevronLeft,
  Clock,
  Flower2,
  Plus,
  Receipt,
  Scissors,
  ShoppingBag,
  Sparkles,
  Users,
  Wallet,
} from "lucide-react";
import { navigation } from "./shared";
import type { WorkspaceContext } from "./types";
import { PanelHead, RevenueChart, Schedule, Segments, Stat } from "./widgets";
export default function DashboardPage({
  data,
  rows,
  user,
  bootstrap,
  operations,
  setDialog,
  navigate,
  action,
}: WorkspaceContext) {
  return (
    <>
      <section className="welcome">
        <div>
          <span className="eyebrow">هر روز، فرصتی برای درخشش بیشتر</span>
          <h2>
            {user.name}، روزتان زیبا <span>✦</span>
          </h2>
          <p>نبض سالن شما، در یک نگاه. امروز را با ارتباط‌های بهتر بسازید.</p>
          <div className="welcome-meta">
            <span>
              <span className="status-dot" /> فضای مدیریت شما آماده است
            </span>
            <span>{bootstrap.branch?.name}</span>
          </div>
        </div>
        <div className="welcome-art">
          <Flower2 size={106} strokeWidth={0.75} />
          <span>GROW WITH BEAUTY</span>
        </div>
      </section>
      <div className="daily-focus">
        {data.followUps && (
          <button onClick={() => navigate("tasks")}>
            <span className="focus-icon">
              <CheckCircle2 size={23} />
            </span>
            <span>
              <b>{fa(data.followUps.today)} پیگیری برای امروز</b>
              <small>
                {fa(data.followUps.overdue)} مورد از موعد گذشته؛ برای مشاهده
                کلیک کنید
              </small>
            </span>
            <ArrowUpLeft size={20} />
          </button>
        )}
        {data.lowStock && (
          <button onClick={() => navigate("products")}>
            <span className="focus-icon gold">
              <ShoppingBag size={23} />
            </span>
            <span>
              <b>
                {data.lowStock.length
                  ? "موجودی نیازمند توجه"
                  : "موجودی در وضعیت مناسب"}
              </b>
              <small>
                {data.lowStock.length
                  ? data.lowStock
                      .slice(0, 2)
                      .map((r: Row) => r.name)
                      .join("، ")
                  : "کالایی زیر حداقل موجودی ثبت نشده است"}
              </small>
            </span>
            <ArrowUpLeft size={20} />
          </button>
        )}
        {operations && (
          <button onClick={() => setDialog({ type: "booking" })}>
            <span className="focus-icon purple">
              <CalendarDays size={23} />
            </span>
            <span>
              <b>نوبت تازه، در چند قدم</b>
              <small>انتخاب خدمت و پیشنهاد ساعت آزاد</small>
            </span>
            <Plus size={20} />
          </button>
        )}
      </div>
      <div className="stats-grid">
        <Stat
          title="نوبت‌های امروز"
          value={fa(data.appointments)}
          icon={CalendarDays}
          hint="برنامه‌ریزی دقیق، تجربه بهتر"
          color="teal"
        />
        <Stat
          title={
            data.revenue !== undefined
              ? "فروش امروز"
              : data.myVisits !== undefined
                ? "مراجعه‌های امروز من"
                : "مشتریان فعال"
          }
          value={
            data.revenue !== undefined
              ? fa(data.revenue)
              : fa(data.myVisits ?? data.customers)
          }
          unit={data.revenue !== undefined ? "تومان" : ""}
          icon={data.revenue !== undefined ? Wallet : Users}
          hint={
            data.revenue !== undefined
              ? "مجموع فاکتورهای معتبر امروز"
              : "اعتمادهایی که ساخته‌اید"
          }
          color="gold"
        />
        <Stat
          title="صف انتظار"
          value={fa(data.waiting)}
          icon={Clock}
          hint="مشتریان منتظر شروع خدمت"
          color="purple"
        />
        <Stat
          title={
            data.outstanding !== undefined
              ? "مطالبات باز"
              : "نوبت‌های برنامه‌ریزی‌شده"
          }
          value={
            data.outstanding !== undefined
              ? fa(data.outstanding)
              : fa(data.appointments)
          }
          unit={data.outstanding !== undefined ? "تومان" : ""}
          icon={data.outstanding !== undefined ? Receipt : CheckCircle2}
          hint={
            data.outstanding !== undefined
              ? "مانده فاکتورهای تسویه‌نشده"
              : "با توجه به دسترسی حساب شما"
          }
          color="rose"
        />
      </div>
      <div className="dashboard-grid">
        <section className="panel revenue-panel">
          <PanelHead
            title={
              data.trend
                ? "مسیر فروش سالن"
                : data.canSchedule
                  ? "برنامه امروز"
                  : "پیگیری‌های تیم"
            }
            subtitle={
              data.trend
                ? "فروش معتبر در هفت روز گذشته · تومان"
                : "نوبت‌های مربوط به نقش شما"
            }
            action={
              data.trend ? (
                <span className="subtle-chip">۷ روز اخیر</span>
              ) : null
            }
          />
          {data.trend ? (
            <RevenueChart rows={data.trend} />
          ) : data.canSchedule ? (
            <Schedule
              rows={data.schedule || []}
              onOpen={() => navigate("appointments")}
            />
          ) : (
            <div className="quiet-art">
              <CheckCircle2 size={55} />
              <h3>{fa(data.followUps?.today)} پیگیری امروز</h3>
              <p>ارتباط با مشتریان را به موقع ادامه دهید.</p>
              <button className="secondary" onClick={() => navigate("tasks")}>
                مشاهده برد پیگیری
              </button>
            </div>
          )}
        </section>
        <section className="panel">
          <PanelHead
            title="رابطه‌هایی که رشد می‌کنند"
            subtitle="شناخت بهتر، تجربه شخصی‌تر"
          />
          {data.segments ? (
            <Segments data={data.segments} />
          ) : (
            <div className="quiet-art">
              <Flower2 size={70} />
              <h3>همه چیز برای یک روز منظم</h3>
              <p>نوبت‌ها و عملیات مرتبط با نقش خود را از منو دنبال کنید.</p>
            </div>
          )}
        </section>
      </div>
      <div className="dashboard-grid bottom">
        {data.canSchedule && (
          <section className="panel">
            <PanelHead
              title="نوبت‌های امروز"
              subtitle={`${fa(data.schedule?.length)} نوبت در برنامه شما`}
              action={
                <button
                  className="text-button"
                  onClick={() => navigate("appointments")}
                >
                  مشاهده تقویم
                  <ChevronLeft size={15} />
                </button>
              }
            />
            <Schedule
              rows={data.schedule || []}
              onOpen={() => navigate("appointments")}
            />
          </section>
        )}
        <section className="panel">
          <PanelHead
            title={data.topServices ? "خدمات محبوب" : "دسترسی سریع"}
            subtitle={
              data.topServices
                ? "بر اساس فروش ۳۰ روز اخیر"
                : "کارهای روزانه، در چند قدم"
            }
          />
          {data.topServices?.length ? (
            <div className="top-services">
              {data.topServices.map((s: Row, i: number) => (
                <div key={i}>
                  <span className={"service-icon shade-" + i}>
                    <Scissors size={19} />
                  </span>
                  <div>
                    <strong>{s.name}</strong>
                    <small>{fa(s.count)} ردیف فروش</small>
                  </div>
                  <b>
                    {fa(s.total)}
                    <small>تومان</small>
                  </b>
                </div>
              ))}
            </div>
          ) : (
            <div className="quick-links">
              {navigation
                .filter((n) => canSee(user,n.id))
                .slice(1, 5)
                .map((n) => (
                  <button key={n.id} onClick={() => navigate(n.id)}>
                    <n.icon size={21} />
                    {n.title}
                    <ArrowUpLeft size={17} />
                  </button>
                ))}
            </div>
          )}
          <div className="growth-note">
            <Sparkles size={20} />
            <p>هر تجربه خوب، آغاز یک رابطه ماندگار است.</p>
          </div>
        </section>
      </div>
    </>
  );
}
