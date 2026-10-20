import { notFound } from "next/navigation";
const sections = [
  "dashboard","salons","branches","users","subscription",
  "appointments",
  "visits",
  "customers",
  "services",
  "products",
  "invoices",
  "finance",
  "staff",
  "commissions",
  "loyalty",
  "rfm",
  "campaigns",
  "events",
  "tasks",
  "messages",
  "reports",
  "settings",
  "security",
  "audit",
  "resources",
  "commission-rules",
];
export default async function Page({
  params,
}: {
  params: Promise<{ section: string }>;
}) {
  const { section } = await params;
  if (!sections.includes(section)) notFound();
  return null;
}
