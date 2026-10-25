"use client";
export default function GlobalError({reset}:{reset:()=>void}){return <html lang="fa" dir="rtl"><body><main role="alert"><h1>خطایی در نمایش سامانه رخ داد</h1><button onClick={reset}>تلاش دوباره</button></main></body></html>}
