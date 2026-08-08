import Link from "next/link";
import { ArrowRight, CheckCircle2, XCircle } from "lucide-react";
import { Container } from "@/components/ui/Container";
import { Tag } from "@/components/ui/Tag";
import { LinkButton } from "@/components/ui/Button";
import { MarketingNav } from "@/components/site/MarketingNav";
import { Footer } from "@/components/site/Footer";

// The three real backend endpoints, in the order a recruiter actually uses
// them. Each `endpoint` string is shown verbatim on the page — this is meant
// to read as literally true, not as marketing shorthand.
const PHASES = [
  {
    tag: "01 · FILTER",
    title: "Nothing makes the list uninvited",
    body: "Every applicant is matched against the listing's own minimum years of experience and certification requirement before anyone looks at a score.",
    endpoint: "GET /applications/filter",
  },
  {
    tag: "02 · SCORE",
    title: "Two numbers, shown working",
    body: "70 points for experience measured against the minimum, 30 for certification. No hidden model — recompute it by hand if you want to.",
    endpoint: "GET /match-score",
  },
  {
    tag: "03 · DECIDE",
    title: "Bulk actions that don't half-apply",
    body: "Select a set of applicants, choose approved, rejected, or pending, and apply it as one transaction. Nobody's left half-updated.",
    endpoint: "POST /applications/bulk-status",
  },
];

// Every answer here is a factual claim about RecruitmentService's actual
// behavior (transactional bulk updates, 404-on-missing-listing, etc.) — not
// generic reassurance copy. Keep it that way if this list changes.
const FAQ = [
  {
    q: "How is the match score actually calculated?",
    a: "Seventy points come from years of experience measured against the listing's stated minimum — full credit once you clear it, prorated if you're short. Thirty points come from certification: full credit if it isn't required, or if it's required and held; zero if it's required and missing.",
  },
  {
    q: "What happens if a bulk status update fails partway through?",
    a: "It doesn't leave a partial result. The update runs inside a single transaction — every selected applicant moves to the new status together, or none do.",
  },
  {
    q: "What if I score against a listing that doesn't exist?",
    a: "You get a clear 404, not a stack trace or a silently empty result. The same holds for filtering against a missing listing.",
  },
  {
    q: "Does it read resume text, or just structured fields?",
    a: "Structured fields only, today — years of experience and a certification flag. A text-similarity pass against the job description is scoped but not wired into scoring yet.",
  },
];

export default function HomePage() {
  return (
    <>
      <MarketingNav />

      {/* Hero */}
      <section className="pt-20 pb-16 md:pt-28 md:pb-24">
        <Container>
          <div className="max-w-2xl">
            <h1 className="balance font-serif text-5xl leading-[1.05] tracking-tight md:text-7xl">
              You make the call.
            </h1>
            <p className="mt-6 max-w-xl text-lg leading-relaxed text-ink-muted">
              Smart Recruiting filters every applicant against a listing&rsquo;s
              real requirements, scores the fit that&rsquo;s left, and clears a
              shortlist in one pass — no black-box ranking, no guessing.
            </p>
            <p className="mt-3 max-w-md text-sm leading-relaxed text-ink-faint">
              Half the effort, every decision traced back to two numbers on
              file: years of experience, and whether the certification box is
              checked.
            </p>

            <div className="mt-9 flex flex-wrap items-center gap-5">
              <LinkButton href="/admin">
                Open the docket
                <ArrowRight className="size-4" />
              </LinkButton>
              <LinkButton href="/apply" variant="ghost">
                Book a demo
              </LinkButton>
            </div>

            <ul className="mt-10 flex flex-col gap-2 text-sm text-ink-muted sm:flex-row sm:flex-wrap sm:gap-x-8 sm:gap-y-2">
              <li className="flex items-center gap-2">
                <span className="size-1 rounded-full bg-ink-faint" />
                Requirement-first filtering
              </li>
              <li className="flex items-center gap-2">
                <span className="size-1 rounded-full bg-ink-faint" />
                Explainable, two-part scoring
              </li>
              <li className="flex items-center gap-2">
                <span className="size-1 rounded-full bg-ink-faint" />
                Transactional bulk decisions
              </li>
            </ul>

            <p className="mt-8 text-sm text-ink-faint">
              Applying to a role instead?{" "}
              <Link href="/apply" className="text-ink underline underline-offset-4">
                Start an application →
              </Link>
            </p>
          </div>
        </Container>
      </section>

      {/* Interface showcase */}
      <section id="how-it-works" className="border-t border-line py-24">
        <Container>
          <Tag>THE DOCKET</Tag>
          <h2 className="balance mt-4 max-w-lg font-serif text-3xl md:text-4xl">
            One list. Every requirement already applied.
          </h2>

          <div className="mt-14 grid gap-12 lg:grid-cols-[1.1fr_1fr] lg:items-start">
            {/* Dark app-window mockup. Static illustrative data (not a live fetch) —
                the real, interactive version of this table lives at /admin. Colors
                are hardcoded rather than theme tokens because this panel is always
                dark regardless of the page's own light/dark mode. */}
            <div className="overflow-hidden rounded-2xl border border-black/10 bg-[#111111] text-[#f2f1ee] shadow-2xl shadow-black/20">
              <div className="flex items-center gap-2 border-b border-white/10 px-5 py-4">
                <span className="size-2.5 rounded-full bg-white/20" />
                <span className="size-2.5 rounded-full bg-white/20" />
                <span className="size-2.5 rounded-full bg-white/20" />
                <span className="ml-3 font-mono text-xs text-white/40">
                  senior-backend-engineer · docket
                </span>
              </div>

              <div className="flex flex-wrap gap-2 px-5 pt-5">
                {["min. 4 yrs", "certification required", "status: pending"].map(
                  (chip) => (
                    <span
                      key={chip}
                      className="rounded-full border border-white/15 px-3 py-1 font-mono text-[11px] uppercase tracking-wide text-white/60"
                    >
                      {chip}
                    </span>
                  )
                )}
              </div>

              <div className="mt-5 divide-y divide-white/10 px-5 pb-5">
                {[
                  { name: "Elena Kowalski", exp: "6 / 4 yrs", cert: true, score: 100 },
                  { name: "Priya Nair", exp: "3 / 4 yrs", cert: true, score: 82.5 },
                  { name: "Dmitri Volkov", exp: "7 / 4 yrs", cert: false, score: 70 },
                ].map((row) => (
                  <div
                    key={row.name}
                    className="flex items-center justify-between gap-4 py-3.5 text-sm"
                  >
                    <div>
                      <div className="font-medium">{row.name}</div>
                      <div className="font-mono text-xs text-white/40">
                        {row.exp}
                      </div>
                    </div>
                    {row.cert ? (
                      <CheckCircle2 className="size-4 text-[#4cae7b]" />
                    ) : (
                      <XCircle className="size-4 text-white/25" />
                    )}
                    <div className="flex-1">
                      <div className="h-1 w-full overflow-hidden rounded-full bg-white/10">
                        <div
                          className="h-full rounded-full bg-[#d9a94c]"
                          style={{ width: `${row.score}%` }}
                        />
                      </div>
                    </div>
                    <div className="w-10 text-right font-mono text-xs text-white/70">
                      {row.score}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* tagged feature columns */}
            <div className="flex flex-col gap-10">
              {PHASES.map((phase) => (
                <div key={phase.tag} className="border-t border-line pt-6">
                  <Tag>{phase.tag}</Tag>
                  <h3 className="mt-3 font-serif text-xl">{phase.title}</h3>
                  <p className="mt-2 text-sm leading-relaxed text-ink-muted">
                    {phase.body}
                  </p>
                  <span className="mt-3 inline-block rounded bg-paper-raised px-2 py-1 font-mono text-[11px] text-ink-faint">
                    {phase.endpoint}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </Container>
      </section>

      {/* Scoring transparency */}
      <section id="scoring" className="border-t border-line bg-paper-raised py-24">
        <Container>
          <Tag>THE FORMULA</Tag>
          <h2 className="balance mt-4 max-w-lg font-serif text-3xl md:text-4xl">
            Every score is two measurements, not a guess.
          </h2>
          <p className="mt-4 max-w-xl text-ink-muted">
            There&rsquo;s no model behind the number — just the applicant&rsquo;s
            file measured against the listing&rsquo;s own stated bar.
          </p>

          <div className="mt-12 overflow-x-auto rounded-xl border border-line-strong bg-paper">
            <pre className="whitespace-pre-wrap px-6 py-5 font-mono text-sm leading-8 text-ink">
{`experience    = min(1, applicant.years / listing.minYears) × 70
certification = (!required || applicant.certified) ? 30 : 0
match_score   = experience + certification`}
            </pre>
          </div>
        </Container>
      </section>

      {/* FAQ */}
      <section id="faq" className="py-24">
        <Container>
          <Tag>TRUST</Tag>
          <h2 className="balance mt-4 max-w-lg font-serif text-3xl md:text-4xl">
            Questions recruiters actually ask.
          </h2>

          <dl className="mt-12 max-w-2xl divide-y divide-line border-y border-line">
            {FAQ.map((item) => (
              <div key={item.q} className="py-6">
                <dt className="font-semibold">{item.q}</dt>
                <dd className="mt-2 text-sm leading-relaxed text-ink-muted">
                  {item.a}
                </dd>
              </div>
            ))}
          </dl>
        </Container>
      </section>

      {/* Closing CTA: the one section that always inverts to a dark panel,
          independent of system light/dark mode — a deliberate one-off, so it
          uses literal hex values instead of the paper/ink theme tokens. */}
      <section className="border-t border-line bg-[#111111] py-24 text-center text-[#f7f6f2]">
        <Container>
          <p className="font-mono text-xs uppercase tracking-[0.12em] text-white/50">
            Ready when your listings are
          </p>
          <h2 className="balance mx-auto mt-4 max-w-md font-serif text-3xl italic md:text-4xl">
            Your shortlist is already in the data.
          </h2>
          <div className="mt-9 flex flex-wrap items-center justify-center gap-5">
            <LinkButton
              href="/admin"
              className="!bg-[#f7f6f2] !text-[#111111] hover:!bg-white"
            >
              Open the docket
              <ArrowRight className="size-4" />
            </LinkButton>
            <LinkButton
              href="/apply"
              variant="ghost"
              className="!border-white/25 !text-white hover:!bg-white/10"
            >
              Apply to a role
            </LinkButton>
          </div>
        </Container>
      </section>

      <Footer />
    </>
  );
}
