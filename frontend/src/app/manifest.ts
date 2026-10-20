import type { MetadataRoute } from "next";
export default function manifest(): MetadataRoute.Manifest {
  return {
    name: "BsiCRM — مدیریت سالن زیبایی",
    short_name: "BsiCRM",
    description: "کلینیک کسب و کار و هوشمندسازی تجاری BsiCenter",
    start_url: "/",
    scope: "/",
    display: "standalone",
    background_color: "#f6f7f9",
    theme_color: "#102b39",
    lang: "fa",
    dir: "rtl",
    icons: [
      {
        src: "/icon-192.png",
        sizes: "192x192",
        type: "image/png",
        purpose: "any",
      },
      {
        src: "/icon-512.png",
        sizes: "512x512",
        type: "image/png",
        purpose: "maskable",
      },
    ],
  };
}
