import Link from "next/link";
import { Container } from "@/components/ui/Container";

/**
 * Lightweight header used inside the two app-like routes (/admin, /apply) —
 * no marketing anchors, just an identity label and a way to switch to the
 * other audience's view or back out to the marketing site.
 */
export function AppNav({
  eyebrow,
  switchHref,
  switchLabel,
}: {
  eyebrow: string;
  switchHref: string;
  switchLabel: string;
}) {
  return (
    <header className="border-b border-line bg-paper">
      <Container className="flex h-16 items-center justify-between">
        <div className="flex items-baseline gap-3">
          <Link href="/" className="font-serif text-lg lowercase italic">
            smart recruiting
          </Link>
          <span className="hidden font-mono text-xs uppercase tracking-[0.12em] text-ink-faint sm:inline">
            {eyebrow}
          </span>
        </div>
        <div className="flex items-center gap-4 text-sm">
          <Link href={switchHref} className="text-ink-muted hover:text-ink">
            {switchLabel}
          </Link>
          <Link href="/" className="text-ink-muted hover:text-ink">
            ← Site
          </Link>
        </div>
      </Container>
    </header>
  );
}
