export const todayIso = () =>
  new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Tehran",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(new Date());
export const localDateTime = (value: string) =>
  new Date(new Date(value).getTime() + 210 * 60 * 1000)
    .toISOString()
    .slice(0, 16);
export function addDays(day: string, n: number) {
  const d = new Date(day + "T12:00:00Z");
  d.setUTCDate(d.getUTCDate() + n);
  return d.toISOString().slice(0, 10);
}
