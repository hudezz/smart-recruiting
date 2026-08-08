import type { Metadata } from "next";
import { Fraunces, Inter, IBM_Plex_Mono } from "next/font/google";
import "./globals.css";

// next/font self-hosts these at build time (no request to Google at runtime).
// Each font's `variable` name is deliberately distinct from the Tailwind theme
// token it feeds (--font-serif/--font-sans/--font-mono in globals.css) — reusing
// the same name would make `--font-serif: var(--font-serif)` a circular
// reference that silently resolves to nothing.
const fraunces = Fraunces({
  variable: "--font-fraunces",
  subsets: ["latin"],
  weight: ["400", "500", "600"],
  style: ["normal", "italic"],
  display: "swap",
});

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
  display: "swap",
});

const plexMono = IBM_Plex_Mono({
  variable: "--font-plex-mono",
  subsets: ["latin"],
  weight: ["400", "500"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "Smart Recruiting",
  description:
    "Filter candidates against a listing's real requirements, score every match, and decide in one pass.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    // Font variables are set on <html> so every page/component can reach them
    // via the Tailwind font-serif / font-sans / font-mono utilities.
    <html
      lang="en"
      className={`${fraunces.variable} ${inter.variable} ${plexMono.variable}`}
    >
      <body className="min-h-screen bg-paper text-ink font-sans antialiased">
        {children}
      </body>
    </html>
  );
}
