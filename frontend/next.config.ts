import type { NextConfig } from "next";

// Where the Spring Boot backend lives. Overridable via env var for non-local
// deployments; defaults to the local dev port.
const BACKEND_ORIGIN = process.env.BACKEND_ORIGIN ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  // Proxies /api/recruitment/* to the Spring Boot API server-side. This is what
  // lets the browser call a same-origin path instead of localhost:8080 directly —
  // no CORS configuration needed on the Java side, because from the browser's
  // point of view it never left localhost:3000.
  async rewrites() {
    return [
      {
        source: "/api/recruitment/:path*",
        destination: `${BACKEND_ORIGIN}/api/recruitment/:path*`,
      },
    ];
  },
};

export default nextConfig;
