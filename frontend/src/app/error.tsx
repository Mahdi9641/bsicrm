"use client";
export default function ErrorPage({reset}:{reset:()=>void}){return <main className="public-invoice" role="alert"><h1>نمایش این صفحه با مشکل روبه‌رو شد</h1><p>لطفاً دوباره تلاش کنید؛ اگر مشکل ادامه داشت با پشتیبانی تماس بگیرید.</p><button className="primary" onClick={reset}>تلاش دوباره</button></main>}
