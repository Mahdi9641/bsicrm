"use client";
import { Lookups } from "@/components/BusinessDialogs";
import CommandPalette from "@/components/CommandPalette";
import { Empty, Loading, Logo } from "@/components/ui";
import Login from "@/features/auth/Login";
import CustomerProfile from "@/features/customers/CustomerProfile";
import { api, date, fa, labels, logout, refreshSession, Row } from "@/lib/api";
import {
  AlertCircle,
  CalendarDays,
  CheckCircle2,
  ChevronLeft,
  Download,
  Flower2,
  LogOut,
  Menu,
  Plus,
  RefreshCw,
  Search,
  Sparkles,
  X,
} from "lucide-react";
import { usePathname, useRouter } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import Dialogs from "./Dialogs";
import { avatar, dayRange, navigation, todayIso } from "./shared";
import WorkspaceRouter from "./WorkspaceRouter";
export default function App() {
  const pathname = usePathname(),
    router = useRouter();
  const routeTab = pathname.split("/").filter(Boolean)[0] || "dashboard";
  const [user, setUser] = useState<Row | null>(null),
    [booting, setBooting] = useState(true),
    [tab, setTab] = useState(
      navigation.some((n) => n.id === routeTab) ? routeTab : "dashboard",
    ),
    [mobileMenu, setMobileMenu] = useState(false),
    [bootstrap, setBootstrap] = useState<Row>({}),
    [data, setData] = useState<any>(null),
    [loading, setLoading] = useState(false),
    [error, setError] = useState(""),
    [toast, setToast] = useState(""),
    [q, setQ] = useState(""),
    [page, setPage] = useState(0),
    [day, setDay] = useState(todayIso()),
    [from, setFrom] = useState(todayIso().slice(0, 7) + "-01"),
    [to, setTo] = useState(todayIso()),
    [lookups, setLookups] = useState<Lookups>({
      customers: [],
      staff: [],
      services: [],
      products: [],
      accounts: [],
    }),
    [dialog, setDialog] = useState<Row | null>(null),
    [profile, setProfile] = useState<Row | null>(null),
    [offline, setOffline] = useState(false),
    [install, setInstall] = useState<any>(null),
    [busyAction, setBusyAction] = useState(false);
  const [commands, setCommands] = useState(false);
  const [branches,setBranches]=useState<Row[]>([]),[subscriptionBlocked,setSubscriptionBlocked]=useState(false);
  const sessionGeneration = useRef(0);
  const seq = useRef(0),
    searchTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const can = (...roles: string[]) => !!user && roles.includes(user.role);
  const operations = can("ADMIN", "MANAGER", "RECEPTION"),
    manager = can("ADMIN", "MANAGER"),
    finance = can("ADMIN", "MANAGER", "FINANCE");
  useEffect(() => {
    refreshSession()
      .then((r) => setUser(r.user))
      .catch(() => {})
      .finally(() => setBooting(false));
    const expired = () => {
      setCommands(false);
      ++seq.current;
      ++sessionGeneration.current;
      setLookups({
        customers: [],
        services: [],
        staff: [],
        products: [],
        accounts: [],
      });
      setUser(null);
      setData(null);
      setProfile(null);
      setDialog(null);
      setBootstrap({});
    };
    const network = () => setOffline(!navigator.onLine);
    const installer = (e: Event) => {
      e.preventDefault();
      setInstall(e);
    };
    window.addEventListener("session-expired", expired);
    window.addEventListener("online", network);
    window.addEventListener("offline", network);
    window.addEventListener("beforeinstallprompt", installer);
    network();
    if ("serviceWorker" in navigator && window.isSecureContext)
      navigator.serviceWorker.register("/sw.js").catch(() => {});
    return () => {
      window.removeEventListener("session-expired", expired);
      window.removeEventListener("online", network);
      window.removeEventListener("offline", network);
      window.removeEventListener("beforeinstallprompt", installer);
    };
  }, []);
  useEffect(() => {
    const key = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "k" && user) {
        e.preventDefault();
        setCommands((v) => !v);
      }
    };
    window.addEventListener("keydown", key);
    return () => window.removeEventListener("keydown", key);
  }, [user]);
  useEffect(() => {
    if (toast) {
      const t = setTimeout(() => setToast(""), 4500);
      return () => clearTimeout(t);
    }
  }, [toast]);
  const init = useCallback(async () => {
    if (!user || user.role==="CUSTOMER") return;
    const generation = sessionGeneration.current;
    const b = await api("/bootstrap");
    if (generation !== sessionGeneration.current) return;
    setBootstrap(b);
    setSubscriptionBlocked(!b.subscriptionActive && user.role!=="ADMIN");
    if (["ADMIN","MANAGER"].includes(user.role)) setBranches(await api("/tenancy/branches"));
    if (!b.subscriptionActive && user.role!=="ADMIN") return;
    const requests = [
      api("/services"),
      api("/staff"),
      can("ADMIN", "MANAGER", "RECEPTION", "MARKETING", "FINANCE")
        ? api("/customers")
        : Promise.resolve([]),
      can("ADMIN", "MANAGER", "RECEPTION", "FINANCE")
        ? api("/products")
        : Promise.resolve([]),
    ];
    const r = await Promise.all(requests);
    if (generation !== sessionGeneration.current) return;
    setLookups({
      services: r[0],
      staff: r[1],
      customers: r[2],
      products: r[3],
      accounts: b.accounts,
    });
  }, [user]);
  useEffect(()=>{
    if(user?.role==="CUSTOMER") router.replace("/portal");
  },[user,router]);
  useEffect(()=>{const block=()=>{setSubscriptionBlocked(true);setData(null);setDialog(null);setProfile(null)};window.addEventListener("subscription-expired",block);return()=>window.removeEventListener("subscription-expired",block)},[]);
  useEffect(() => {
    if (user) init().catch((e) => setError(e.message));
  }, [user, init]);
  useEffect(() => {
    const id = navigation.some((n) => n.id === routeTab)
      ? routeTab
      : "dashboard";
    setTab(id);
    setData(null);
    setQ("");
    setPage(0);
    setError("");
    ++seq.current;
  }, [pathname]);
  const navigate = (id: string) => {
    router.push(id === "dashboard" ? "/" : "/" + id);
    setMobileMenu(false);
  };
  const load = useCallback(async () => {
    if (!user || user.role==="CUSTOMER" || !navigation.find(n=>n.id===tab)?.roles.includes(user.role)) return;
    if (subscriptionBlocked && !["subscription","security"].includes(tab)) return;
    const number = ++seq.current;
    setLoading(true);
    setError("");
    try {
      if (
        ["tasks", "security", "resources", "commission-rules","salons","branches","users","subscription"].includes(tab)
      ) {
        setData({});
        return;
      }
      let path = "/" + tab;
      if (tab === "settings") {
        const users = await api("/users");
        if (seq.current === number) setData(users);
        return;
      }
      if (tab === "appointments") path += dayRange(day);
      if (tab === "customers")
        path += "?q=" + encodeURIComponent(q) + "&page=" + page;
      if (tab === "reports")
        path +=
          "?from=" +
          encodeURIComponent(from + "T00:00:00+03:30") +
          "&to=" +
          encodeURIComponent(
            new Date(
              new Date(to + "T00:00:00+03:30").getTime() + 86400000,
            ).toISOString(),
          );
      const d = await api(path);
      if (seq.current === number) setData(d);
    } catch (e) {
      if (seq.current === number) setError((e as Error).message);
    } finally {
      if (seq.current === number) setLoading(false);
    }
  }, [user, tab, day, q, page, from, to, subscriptionBlocked]);
  useEffect(() => {
    const t = setTimeout(() => load(), tab === "customers" ? 250 : 0);
    return () => clearTimeout(t);
  }, [load]);
  const done = () => {
    setToast("اطلاعات با موفقیت ثبت شد");
    load();
    init().catch((e) => setError(e.message));
  };
  const action = async (path: string, body?: Row) => {
    if (busyAction) return;
    setBusyAction(true);
    try {
      await api(path, "POST", body);
      done();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusyAction(false);
    }
  };
  const openProfile = async (id: number) => {
    try {
      const generation = sessionGeneration.current;
      const next = await api("/customers/" + id);
      if (generation === sessionGeneration.current) setProfile(next);
    } catch (e) {
      setError((e as Error).message);
    }
  };
  if (booting)
    return (
      <div className="boot-screen">
        <Logo />
        <Loading />
      </div>
    );
  if (!user)
    return (
      <Login
        onLogin={(u) => {
          setUser(u);
          if(u.role==="CUSTOMER") router.replace("/portal"); else navigate("dashboard");
        }}
      />
    );
  if(user.role==="CUSTOMER") return <Loading/>;
  const nav = navigation.filter((n) => n.roles.includes(user.role)),
    current = navigation.find((n) => n.id === tab)!;
  const rows: Array<Row> = Array.isArray(data) ? data : [];
  const filtered =
    tab === "customers" || !q
      ? rows
      : rows.filter((r) =>
          JSON.stringify(r).toLowerCase().includes(q.toLowerCase()),
        );
  const titleActions = () => {
    const create = () => setDialog({ type: tab });
    if (tab === "dashboard" && operations)
      return (
        <button
          className="primary"
          onClick={() => setDialog({ type: "booking" })}
        >
          <Plus size={18} />
          نوبت جدید
        </button>
      );
    if (tab === "appointments" && operations)
      return (
        <button
          className="primary"
          onClick={() => setDialog({ type: "booking" })}
        >
          <Plus size={18} />
          ثبت نوبت
        </button>
      );
    if (tab === "invoices" && can("ADMIN", "MANAGER", "RECEPTION", "FINANCE"))
      return (
        <button
          className="primary"
          onClick={() => setDialog({ type: "invoice" })}
        >
          <Plus size={18} />
          فاکتور جدید
        </button>
      );
    if (tab === "visits" && operations)
      return (
        <button
          className="primary"
          onClick={() => setDialog({ type: "walkin" })}
        >
          <Plus size={18} />
          پذیرش بدون نوبت
        </button>
      );
    if (
      (tab === "customers" && operations) ||
      (["services", "products", "staff"].includes(tab) && manager) ||
      ["campaigns", "events"].includes(tab)
    )
      return (
        <button className="primary" onClick={create}>
          <Plus size={18} />
          {
            (
              {
                customers: "مشتری جدید",
                services: "خدمت جدید",
                products: "محصول جدید",
                staff: "عضو جدید",
                campaigns: "کمپین جدید",
                events: "رویداد جدید",
              } as Row
            )[tab]
          }
        </button>
      );
    if (tab === "finance")
      return (
        <button
          className="primary"
          onClick={() => setDialog({ type: "expenses" })}
        >
          <Plus size={18} />
          ثبت هزینه
        </button>
      );
    if (tab === "rfm")
      return (
        <button
          className="primary"
          disabled={busyAction}
          onClick={() => action("/rfm/snapshot")}
        >
          <RefreshCw size={17} />
          ثبت تحلیل امروز
        </button>
      );
    if (tab === "settings")
      return (
        <button
          className="primary"
          onClick={() => setDialog({ type: "users" })}
        >
          <Plus size={18} />
          کاربر جدید
        </button>
      );
  };
  const main = () => {
    if (!current?.roles.includes(user.role)) return <section className="subscription-block"><h2>دسترسی به این صفحه برای نقش شما مجاز نیست</h2><button className="primary" onClick={()=>navigate("dashboard")}>بازگشت به نمای کلی</button></section>;
    if(subscriptionBlocked && !["subscription","security"].includes(tab))return <section className="subscription-block"><h2>دسترسی عملیاتی سالن متوقف شده است</h2><p>اشتراک منقضی شده یا سالن/شعبه غیرفعال است. برای ادامه، وضعیت را بررسی و اشتراک را تمدید کنید.</p>{user.role==="MANAGER"?<button className="primary" onClick={()=>navigate("subscription")}>خرید یا تمدید اشتراک</button>:<p>برای تمدید با مدیر سالن تماس بگیرید.</p>}<button className="secondary" onClick={()=>init().catch(e=>setError(e.message))}>بررسی دوباره</button></section>;
    if (loading && !data) return <Loading />;
    if (!data) return error ? null : <Empty />;
    return (
      <WorkspaceRouter
        {...{
          setQ,
          tab,
          data,
          filtered,
          rows,
          user,
          bootstrap,
          operations,
          manager,
          finance,
          lookups,
          day,
          page,
          q,
          from,
          to,
          busyAction,
          setDialog,
          setProfile,
          setDay,
          setPage,
          setFrom,
          setTo,
          setError,
          setToast,
          navigate,
          openProfile,
          can,
          action,
          done,
          load,
        }}
      />
    );
  };
  return (
    <div className="app-shell">
      {commands && (
        <CommandPalette
          nav={nav}
          customersAllowed={can(
            "ADMIN",
            "MANAGER",
            "RECEPTION",
            "MARKETING",
            "FINANCE",
          )}
          navigate={navigate}
          customer={openProfile}
          close={() => setCommands(false)}
        />
      )}{" "}
      {mobileMenu && (
        <div className="sidebar-scrim" onClick={() => setMobileMenu(false)} />
      )}
      <aside className={"sidebar " + (mobileMenu ? "open" : "")}>
        <div className="sidebar-brand">
          <Logo />
          <button
            className="icon-btn mobile-close"
            onClick={() => setMobileMenu(false)}
            aria-label="بستن منو"
          >
            <X size={20} />
          </button>
        </div>
        <div className="workspace-switch">
          <span className="workspace-icon">
            <Flower2 size={20} />
          </span>
          <div>
            <strong>
              {bootstrap.settings?.organization || "فضای مدیریت سالن"}
            </strong>
            <small>{bootstrap.branch?.name || "شعبه جاری"}</small>
          </div>
          <ChevronLeft size={15} />
        </div>
        {["ADMIN","MANAGER"].includes(user.role)&&<div className="tenant-picker"><label>سالن / شعبهٔ جاری<select aria-label="انتخاب شعبه جاری" value={bootstrap.branch?.id||""} onChange={async e=>{try{await api("/tenancy/switch","POST",{branchId:Number(e.target.value)});++seq.current;++sessionGeneration.current;setData(null);setDialog(null);setProfile(null);setLookups({customers:[],services:[],staff:[],products:[],accounts:[]});setUser(await api("/auth/me"));navigate("dashboard")}catch(e){setError((e as Error).message)}}}><option value="" disabled>انتخاب شعبه</option>{branches.map(b=><option key={b.id} value={b.id}>{b.salon_name} / {b.name}{b.active?"":" (غیرفعال)"}</option>)}</select></label></div>}
        <nav>
          {nav.map((n) => (
            <div key={n.id}>
              {n.group && <small className="nav-group">{n.group}</small>}
              <button
                className={tab === n.id ? "active" : ""}
                onClick={() => navigate(n.id)}
              >
                <n.icon size={19} />
                <span>{n.title}</span>
                {tab === n.id && <i />}
              </button>
            </div>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="license-tag">
            <Sparkles size={16} />
            <span>نسخه {labels[bootstrap.settings?.plan] || "پیشرفته"}</span>
            <span className="tiny-dot" />
          </div>
          <div className="sidebar-signature">
            <b dir="ltr">BsiCenter</b>
            <small>هوشمندی در مسیر رشد شما</small>
          </div>
        </div>
      </aside>
      <div className="workspace">
        <header className="topbar">
          <div className="breadcrumb">
            <button
              className="icon-btn menu-toggle"
              aria-label="باز کردن منو"
              onClick={() => setMobileMenu(true)}
            >
              <Menu size={23} />
            </button>
            <span>فضای مدیریت</span>
            <ChevronLeft size={13} />
            <b>{current?.title}</b>
          </div>
          <div className="topbar-right">
            <button className="global-search" onClick={() => setCommands(true)}>
              <Search size={17} />
              <span>جستجو در سامانه</span>
              <kbd>Ctrl K</kbd>
            </button>
            <span className="today">
              <CalendarDays size={16} />
              {date(new Date().toISOString())}
            </span>
            {install && (
              <button
                className="icon-btn"
                title="نصب اپلیکیشن"
                onClick={async () => {
                  await install.prompt();
                  setInstall(null);
                }}
              >
                <Download size={19} />
              </button>
            )}
            <span className="topbar-divider" />
            <div className="user-chip">
              <span className="avatar small">{avatar(user.name)}</span>
              <div>
                <strong>{user.name}</strong>
                <small>{labels[user.role]}</small>
              </div>
            </div>
            <button
              className="icon-btn logout"
              aria-label="خروج"
              onClick={async () => {
                try {
                  await logout();
                  ++seq.current;
                  ++sessionGeneration.current;
                  setCommands(false);
                  setLookups({
                    customers: [],
                    services: [],
                    staff: [],
                    products: [],
                    accounts: [],
                  });
                  setUser(null);
                  setData(null);
                  setProfile(null);
                  setDialog(null);
                  setBootstrap({});
                } catch (e) {
                  setError((e as Error).message);
                }
              }}
            >
              <LogOut size={18} />
            </button>
          </div>
        </header>
        {offline && (
          <div className="offline-banner">
            اتصال شبکه قطع شده است. ثبت عملیات نیازمند اتصال به سرور است.
          </div>
        )}
        <main className="main-content">
          <div className="page-heading">
            <div>
              <div className="page-kicker">BSICENTER / {tab.toUpperCase()}</div>
              <h1>{current?.title}</h1>
              <p>
                {(
                  {
                    dashboard: "جزئیات امروز، چشم‌انداز فردا",
                    customers: "هر مشتری، یک داستان و یک فرصت برای ارتباط بهتر",
                    appointments: "زمان‌بندی دقیق برای یک تجربه بی‌نقص",
                    visits: "از لحظه ورود تا پایان یک تجربه خوب",
                    invoices: "فروش شفاف، پرداخت منظم، حساب آسوده",
                    rfm: "از داده‌های واقعی به شناخت عمیق‌تر",
                    loyalty: "قدردانی از اعتماد مشتریان شما",
                    reports: "تصمیم‌های بهتر از دل اعداد روشن",
                  } as Row
                )[tab] || "همه اطلاعات موردنیاز، در یک فضای یکپارچه"}
              </p>
            </div>
            <div className="heading-actions">
              <button
                className="icon-btn refresh"
                aria-label="به‌روزرسانی"
                disabled={loading}
                onClick={() => load()}
              >
                <RefreshCw size={18} className={loading ? "spin" : ""} />
              </button>
              {!subscriptionBlocked && current?.roles.includes(user.role) && titleActions()}
            </div>
          </div>
          {![
            "dashboard","salons","branches","users","subscription",
            "settings",
            "reports",
            "finance",
            "tasks",
            "security",
            "resources",
            "commission-rules",
          ].includes(tab) && (
            <div className="list-tools">
              <div className="search-field">
                <Search size={18} />
                <input
                  placeholder={
                    tab === "customers"
                      ? "نام، موبایل، کد ملی یا کد مشتری…"
                      : "جستجو در این صفحه…"
                  }
                  aria-label="جستجو"
                  value={q}
                  onChange={(e) => {
                    setQ(e.target.value);
                    setPage(0);
                  }}
                />
                {q && (
                  <button
                    className="icon-btn"
                    aria-label="پاک کردن جستجو"
                    onClick={() => setQ("")}
                  >
                    <X size={15} />
                  </button>
                )}
              </div>
              <span className="record-count">
                {fa(filtered.length)} مورد در این صفحه
              </span>
            </div>
          )}
          {error && (
            <div role="alert" className="error page-error">
              <AlertCircle size={19} />
              {error}
              <button className="text-button" onClick={() => load()}>
                تلاش دوباره
              </button>
            </div>
          )}
          {main()}
          <footer className="main-footer">
            <span>
              کلینیک کسب و کار و هوشمندسازی تجاری <b dir="ltr">BsiCenter</b>
            </span>
            <span>
              همراه شما در مسیر رشد و شکوفایی{" "}
              <span className="gold-text">✦</span>
            </span>
          </footer>
        </main>
      </div>
      {toast && (
        <div className="toast" role="status">
          <CheckCircle2 size={20} />
          {toast}
        </div>
      )}
      {dialog && (
        <Dialogs
          dialog={dialog}
          lookups={lookups}
          close={() => setDialog(null)}
          done={done}
          user={user}
          onLogout={() => setUser(null)}
        />
      )}
      {profile && (
        <CustomerProfile {...{ profile, setProfile, operations, setDialog }} />
      )}
    </div>
  );
}
