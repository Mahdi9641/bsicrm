"use client";
import { useEffect } from "react";
export default function PersianValidation() {
  useEffect(() => {
    const field = (e: Event) => e.target instanceof HTMLInputElement || e.target instanceof HTMLSelectElement || e.target instanceof HTMLTextAreaElement ? e.target : null;
    const reset = (e: Event) => field(e)?.setCustomValidity("");
    const invalid = (e: Event) => {
      const target = field(e); if (!target) return; target.setCustomValidity(""); const v = target.validity;
      target.setCustomValidity(v.valueMissing ? "لطفاً این فیلد را تکمیل کنید" : v.typeMismatch ? "قالب مقدار واردشده معتبر نیست" : v.tooShort ? "تعداد نویسه‌ها کمتر از حد مجاز است" : v.tooLong ? "تعداد نویسه‌ها بیشتر از حد مجاز است" : v.rangeUnderflow || v.rangeOverflow ? "مقدار خارج از بازهٔ مجاز است" : v.stepMismatch ? "مقدار با گام مجاز هماهنگ نیست" : v.patternMismatch ? "مقدار را با قالب صحیح وارد کنید" : "مقدار واردشده معتبر نیست");
    };
    document.addEventListener("invalid", invalid, true); document.addEventListener("input", reset, true); document.addEventListener("change", reset, true);
    return () => { document.removeEventListener("invalid", invalid, true); document.removeEventListener("input", reset, true); document.removeEventListener("change", reset, true); };
  }, []); return null;
}
