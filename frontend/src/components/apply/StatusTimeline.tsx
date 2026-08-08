import { CheckCircle2, Circle } from "lucide-react";
import type { ApplicationStatus } from "@/lib/api";

// Softer than the recruiter-facing labels in StatusPill.tsx — this is what a
// candidate reads, so it avoids terse internal terminology.
const DECISION_LABEL: Record<ApplicationStatus, string> = {
  PENDING: "Awaiting decision",
  APPROVED: "Approved — moving to next step",
  REJECTED: "Not moving forward this time",
};

/**
 * Three-step progress display for an applicant checking their own status.
 * The backend only tracks PENDING/APPROVED/REJECTED — no separate "received"
 * vs "in review" state — so both of those steps are shown as complete as soon
 * as a record exists at all; only the final decision step depends on status.
 */
export function StatusTimeline({ status }: { status: ApplicationStatus }) {
  const resolved = status !== "PENDING";

  const steps = [
    { label: "Application received", done: true },
    { label: "In review", done: true },
    { label: DECISION_LABEL[status], done: resolved },
  ];

  return (
    <ol className="flex flex-col gap-0">
      {steps.map((step, i) => (
        <li key={step.label} className="flex gap-4">
          <div className="flex flex-col items-center">
            {step.done ? (
              <CheckCircle2 className="size-5 text-good" />
            ) : (
              <Circle className="size-5 text-ink-faint" />
            )}
            {i < steps.length - 1 && (
              <span
                className={`mt-1 h-8 w-px ${
                  step.done ? "bg-good" : "bg-line-strong"
                }`}
              />
            )}
          </div>
          <div
            className={`pb-8 text-sm ${
              step.done ? "text-ink" : "text-ink-faint"
            }`}
          >
            {step.label}
          </div>
        </li>
      ))}
    </ol>
  );
}
