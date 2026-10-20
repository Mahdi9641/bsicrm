import "@fontsource/vazirmatn/400.css";
import "@fontsource/vazirmatn/500.css";
import "@fontsource/vazirmatn/600.css";
import "@fontsource/vazirmatn/700.css";
import type { Metadata, Viewport } from "next";
import "./globals.css";
import "./workspace-v2.css";
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
    <html lang="fa" dir="rtl">
      <body>{children}</body>
    </html>
  );
}
