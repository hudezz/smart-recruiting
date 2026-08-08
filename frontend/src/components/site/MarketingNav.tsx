import Link from "next/link";
import { Container } from "@/components/ui/Container";
import { LinkButton } from "@/components/ui/Button";

/**
 * Header for the public homepage ("/") only. Links to the on-page anchor
 * sections plus both audience entry points: "Sign in" toward the recruiter
 * docket (/admin) and the primary CTA toward the applicant flow (/apply).
 */
export function MarketingNav() {
  return (
    <header className="sticky top-0 z-30 border-b border-line bg-paper/85 backdrop-blur">
      <Container className="flex h-16 items-center justify-between">
        <Link
          href="/"
          className="font-serif text-lg lowercase italic tracking-tight"
        >
          smart recruiting
        </Link>

        <nav className="hidden items-center gap-8 text-sm text-ink-muted md:flex">
          <Link href="/#how-it-works" className="hover:text-ink">
            How it works
          </Link>
          <Link href="/#scoring" className="hover:text-ink">
            Scoring
          </Link>
          <Link href="/#faq" className="hover:text-ink">
            Trust
          </Link>
        </nav>

        <div className="flex items-center gap-3">
          <Link
            href="/admin"
            className="hidden text-sm font-medium text-ink-muted hover:text-ink sm:inline"
          >
            Sign in
          </Link>
          <LinkButton href="/apply" variant="solid" className="text-xs">
            Start an application →
          </LinkButton>
        </div>
      </Container>
    </header>
  );
}
