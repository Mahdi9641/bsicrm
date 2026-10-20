"use client";
import Captcha from "./Captcha";
import { Logo } from "@/components/ui";
import { api, Row } from "@/lib/api";
import {
  ArrowUpLeft,
  CalendarDays,
  ChartNoAxesCombined,
  Eye,
  Flower2,
  ShieldCheck,
  Sparkles,
  Users,
} from "lucide-react";
import { useEffect, useState } from "react";
export default function Login({ onLogin, initialMode="staff" }: { onLogin: (u: Row) => void; initialMode?:"staff"|"customer"|"register" }) {
  const [captcha,setCaptcha]=useState({id:"",answer:""}),[revision,setRevision]=useState(0);
  const [mode,setMode]=useState<"staff"|"customer"|"register">(initialMode),[salons,setSalons]=useState<Row[]>([]),[branchId,setBranchId]=useState(""),[firstName,setFirstName]=useState(""),[lastName,setLastName]=useState("");
  const [username,setUsername]=useState(""),[password,setPassword]=useState(""),[busy,setBusy]=useState(false),[error,setError]=useState(""),[show,setShow]=useState(false);
  const loadSalons=()=>api("/auth/salons").then(setSalons).catch(e=>setError(e.message));
  useEffect(()=>{loadSalons()},[]);
  return (
    <main className="login-shell">
      <section className="login-story">
        <Logo />
        <div className="login-art">
          <div className="orb orb-one" />
          <div className="orb orb-two" />
          <Flower2 size={140} strokeWidth={0.6} />
          <span className="art-tag">
            <Sparkles size={16} />
            هر ارتباط، فرصتی برای درخشیدن
          </span>
        </div>
        <h1>
          زیبایی کسب‌وکارتان،
          <br />
          <em>از اینجا آغاز می‌شود.</em>
        </h1>
        <p>
          از اولین مراجعه تا یک رابطه ماندگار؛
          <br />
          همه چیز برای رشد سالن شما، در یک فضای یکپارچه.
        </p>
        <div className="login-features">
          <span>
            <CalendarDays size={17} />
            مدیریت نوبت‌ها
          </span>
          <span>
            <Users size={17} />
            شناخت مشتریان
          </span>
          <span>
            <ChartNoAxesCombined size={17} />
            رشد هوشمند
          </span>
        </div>
        <small>کلینیک کسب و کار و هوشمندسازی تجاری BsiCenter</small>
      </section>
      <section className="login-form">
        <div className="eyebrow">خوش آمدید به فضای مدیریت شما</div>
        <h2>{mode==="register"?"ساخت حساب مشتری":"ورود به BsiCRM"}</h2>
        <div className="auth-tabs" role="tablist" aria-label="نوع ورود">{[["staff","مدیریت و کارکنان"],["customer","ورود مشتری"],["register","ثبت‌نام مشتری"]].map(([id,label])=><button key={id} type="button" role="tab" aria-selected={mode===id} onClick={()=>{setMode(id as typeof mode);setError("");setPassword("");setRevision(v=>v+1)}}>{label}</button>)}</div>
        <p>برای شروع یک روز منظم و پربار، وارد حساب خود شوید.</p>
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            setBusy(true);
            setError("");
            try {
              const endpoint=mode==="staff"?"/auth/login":mode==="register"?"/auth/customer/register":"/auth/customer/login";
              const r = await api(endpoint,"POST",{...(mode==="staff"?{username}:{mobile:username,branchId:Number(branchId)}),...(mode==="register"?{firstName,lastName}:{}),password,captchaId:captcha.id,captchaAnswer:captcha.answer});
              onLogin(r.user);
            } catch (e) {
              setError((e as Error).message); setRevision(v=>v+1);
            } finally {
              setBusy(false);
            }
          }}
        >
          {mode!=="staff"&&<label><span>سالن و شعبه</span><select aria-label="سالن و شعبه" value={branchId} onChange={e=>setBranchId(e.target.value)} required><option value="">سالن خود را انتخاب کنید</option>{salons.map(s=><option key={s.branch_id} value={s.branch_id}>{s.salon_name} — {s.branch_name}</option>)}</select>{salons.length===0&&<button type="button" onClick={loadSalons}>دریافت دوبارهٔ سالن‌ها</button>}</label>}
          {mode==="register"&&<div className="auth-names"><label>نام<input value={firstName} onChange={e=>setFirstName(e.target.value)} maxLength={100} required/></label><label>نام خانوادگی<input value={lastName} onChange={e=>setLastName(e.target.value)} maxLength={100} required/></label></div>}
          <label>
            <span>{mode==="staff"?"نام کاربری":"شماره موبایل"}</span>
            <input
              autoComplete="username"
              autoFocus
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              placeholder={mode==="staff"?"نام کاربری خود را وارد کنید":"09123456789"}
            />
          </label>
          <label>
            <span>رمز عبور</span>
            <div className="password-field">
              <input
                autoComplete={mode==="register"?"new-password":"current-password"}
                minLength={mode==="register"?12:undefined}
                maxLength={72}
                type={show ? "text" : "password"}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                placeholder="رمز عبور"
              />
              <button
                type="button"
                aria-label="نمایش رمز"
                onClick={() => setShow(!show)}
              >
                <Eye size={18} />
              </button>
            </div>
          </label>
          {mode==="register"&&<small>رمز عبور باید حداقل ۱۲ کاراکتر باشد. اگر پرونده دارید، از مدیر سالن بخواهید حساب شما را فعال کند.</small>}
          <Captcha value={captcha} onChange={setCaptcha} revision={revision}/>
          {error && (
            <div role="alert" className="error">
              {error}
            </div>
          )}
          <button className="primary login-submit" disabled={busy||!captcha.id||captcha.answer===""}>
            {busy ? "در حال بررسی…" : mode==="register"?"ثبت‌نام و ورود":mode==="customer"?"ورود به پنل مشتری":"ورود به فضای مدیریت"}
            <ArrowUpLeft size={20} />
          </button>
        </form>
        <div className="secure-note">
          <ShieldCheck size={18} />
          ورود امن · دسترسی متناسب با نقش شما
        </div>
        <p className="login-help">
          برای دریافت یا بازیابی حساب، با مدیر سامانه سالن تماس بگیرید.
        </p>
      </section>
    </main>
  );
}
