import "@fontsource/vazirmatn/400.css";
import "@fontsource/vazirmatn/500.css";
import "@fontsource/vazirmatn/600.css";
import "@fontsource/vazirmatn/700.css";
import type { Metadata, Viewport } from "next";
import "./globals.css";
import "./workspace-v2.css";
import "./themes.css";
import { ThemeProvider } from "@/components/ThemeProvider";
import PersianValidation from "@/components/PersianValidation";
export const metadata: Metadata = {
  title: "BsiCRM | مدیریت هوشمند سالن زیبایی",
  description: "مدیریت ارتباط با مشتری، نوبت‌دهی و رشد سالن زیبایی — BsiCenter",
  manifest: "/manifest.webmanifest",
  appleWebApp: { capable: true, title: "BsiCRM", statusBarStyle: "default" },
};
export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#102b39",
};
export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="fa" dir="rtl" suppressHydrationWarning>
      <head><script dangerouslySetInnerHTML={{ __html: `try{var t=localStorage.getItem('bsicrm-theme');document.documentElement.dataset.theme=t==='dark'||(!t&&matchMedia('(prefers-color-scheme: dark)').matches)?'dark':'light'}catch(e){document.documentElement.dataset.theme='light'}` }}/></head>
      <body><ThemeProvider><PersianValidation />{children}</ThemeProvider></body>
    </html>
  );
}
