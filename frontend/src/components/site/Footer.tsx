import { Container } from "@/components/ui/Container";

/** Minimal shared footer for the marketing and applicant pages. */
export function Footer() {
  return (
    <footer className="border-t border-line py-10">
      <Container className="flex flex-col items-start justify-between gap-3 text-xs text-ink-faint sm:flex-row sm:items-center">
        <span className="font-serif lowercase italic text-ink-muted">
          smart recruiting
        </span>
        <span>Spring Boot service · rule-based scoring, not a black box</span>
      </Container>
    </footer>
  );
}
