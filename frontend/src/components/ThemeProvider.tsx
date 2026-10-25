"use client";
import { createContext, useContext, useEffect, useState } from "react";
import { Moon, Sun } from "lucide-react";
type Theme = "light" | "dark";
const ThemeContext = createContext<{ theme: Theme; change: (theme: Theme) => void }>({ theme: "light", change: () => {} });
export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [theme, setTheme] = useState<Theme>("light");
  const apply = (value: Theme) => { document.documentElement.dataset.theme = value; setTheme(value); };
  useEffect(() => {
    apply(document.documentElement.dataset.theme === "dark" ? "dark" : "light");
    const storage = (e: StorageEvent) => { if(e.key === "bsicrm-theme") apply(e.newValue === "dark" ? "dark" : "light"); };
    window.addEventListener("storage", storage); return () => window.removeEventListener("storage", storage);
  }, []);
  return <ThemeContext.Provider value={{ theme, change: value => { apply(value); try { localStorage.setItem("bsicrm-theme", value); } catch {} } }}>{children}</ThemeContext.Provider>;
}
export function ThemeToggle() {
  const { theme, change } = useContext(ThemeContext);
  return <div className="theme-toggle" role="group" aria-label="انتخاب تم">
    <button type="button" aria-pressed={theme === "light"} onClick={() => change("light")}><Sun size={16}/>روز</button>
    <button type="button" aria-pressed={theme === "dark"} onClick={() => change("dark")}><Moon size={16}/>شب</button>
  </div>;
}
