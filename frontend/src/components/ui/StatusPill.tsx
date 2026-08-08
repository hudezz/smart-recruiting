import type { ApplicationStatus } from "@/lib/api";

// Semantic status colors, kept separate from the brand ink/accent tokens.
const styles: Record<ApplicationStatus, string> = {
  PENDING: "text-warn bg-warn-tint",
  APPROVED: "text-good bg-good-tint",
  REJECTED: "text-closed bg-closed-tint",
};

// Recruiter-facing labels differ slightly from the applicant-facing ones in
// StatusTimeline.tsx — this is an internal tool, so "In review" is enough
// context without the softer phrasing a candidate would need.
const labels: Record<ApplicationStatus, string> = {
  PENDING: "In review",
  APPROVED: "Approved",
  REJECTED: "Not moving forward",
};

/** Small rounded status badge used in the admin docket's applicant table. */
export function StatusPill({ status }: { status: ApplicationStatus }) {
  return (
    <span
      className={`inline-block rounded-full px-2.5 py-1 text-[11px] font-bold uppercase tracking-wide ${styles[status]}`}
    >
      {labels[status]}
    </span>
  );
}
