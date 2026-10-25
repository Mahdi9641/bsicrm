import type { NextConfig } from "next";
const config: NextConfig = {
  output: "standalone",
  allowedDevOrigins: ["localhost", "127.0.0.1", "172.31.13.30"],
  async rewrites() {
    return [
      ...[
        "/swagger-ui.html",
        "/swagger-ui/:path*",
        "/v3/api-docs/:path*",
        "/v3/api-docs.yaml",
      ].map((source) => ({
        source,
        destination: `${process.env.API_INTERNAL_URL || "http://localhost:9090"}${source}`,
      })),
      {
        source: "/api/:path*",
        destination: `${process.env.API_INTERNAL_URL || "http://localhost:9090"}/api/:path*`,
      },
    ];
  },
  async headers() {
    return [
      {
        source: "/:path*",
        headers: [
          { key: "X-Content-Type-Options", value: "nosniff" },
          { key: "X-Frame-Options", value: "DENY" },
          { key: "Referrer-Policy", value: "same-origin" },
          {
            key: "Permissions-Policy",
            value: "camera=(), microphone=(), geolocation=()",
          },
        ],
      },
      {
        source: "/sw.js",
        headers: [{ key: "Cache-Control", value: "no-store" }],
      },
    ];
  },
};
export default config;
