"use client";

import { useState, type FormEvent, type ReactNode } from "react";
import { ArrowRight } from "lucide-react";
import {
  ApiError,
  Applicant,
  JobListing,
  createApplicant,
  getApplicant,
} from "@/lib/api";
import { Button } from "@/components/ui/Button";
import { StatusTimeline } from "@/components/apply/StatusTimeline";

// The three-step candidate flow. Modeled as a discriminated union rather than
// separate booleans so each step only carries the data it actually needs
// (the form step needs a listing, confirmation needs the created applicant)
// and TypeScript can enforce that at the call site.
type ViewState =
  | { step: "browse" }
  | { step: "form"; listing: JobListing }
  | { step: "confirmation"; applicant: Applicant };

/**
 * Public application flow: browse open roles, submit an application, land on
 * a confirmation screen — plus an always-visible "check your status" lookup
 * for candidates returning later. Deliberately shows no match scores or
 * comparisons to other candidates; that's recruiter-only data (see AdminDocket.tsx).
 */
export function ApplyFlow({ listings }: { listings: JobListing[] }) {
  const [view, setView] = useState<ViewState>({ step: "browse" });

  const [lookupId, setLookupId] = useState("");
  const [lookupResult, setLookupResult] = useState<Applicant | null>(null);
  const [lookupError, setLookupError] = useState<string | null>(null);
  const [lookupLoading, setLookupLoading] = useState(false);

  // Looks up an existing application by its numeric ID — the same ID shown on
  // the Confirmation screen after a successful submission. This is the only
  // "auth" a candidate has: there's no login, just knowing your own ID.
  async function handleLookup(e: FormEvent) {
    e.preventDefault();
    const id = Number(lookupId);
    if (!id || id <= 0) {
      setLookupError("Enter the numeric ID from your confirmation.");
      return;
    }
    setLookupLoading(true);
    setLookupError(null);
    setLookupResult(null);
    try {
      const applicant = await getApplicant(id);
      setLookupResult(applicant);
    } catch (err) {
      setLookupError(
        err instanceof ApiError && err.status === 404
          ? "We couldn't find an application with that ID."
          : "Something went wrong looking that up."
      );
    } finally {
      setLookupLoading(false);
    }
  }

  return (
    <div className="flex flex-col gap-20 py-14">
      {/* status lookup */}
      <section className="mx-auto w-full max-w-lg px-6">
        <h2 className="font-serif text-2xl italic">Already applied?</h2>
        <p className="mt-2 text-sm text-ink-muted">
          Enter the application ID from your confirmation to see where things
          stand.
        </p>
        <form onSubmit={handleLookup} className="mt-4 flex gap-2">
          <input
            value={lookupId}
            onChange={(e) => setLookupId(e.target.value)}
            placeholder="e.g. 4"
            inputMode="numeric"
            className="w-full rounded-md border border-line-strong bg-transparent px-3 py-2 text-sm outline-none placeholder:text-ink-faint focus:border-ink"
          />
          <Button type="submit" variant="ghost" disabled={lookupLoading}>
            {lookupLoading ? "Checking…" : "Check status"}
          </Button>
        </form>
        {lookupError && (
          <p className="mt-3 text-sm text-warn">{lookupError}</p>
        )}
        {lookupResult && (
          <div className="mt-6 rounded-lg border border-line-strong p-5">
            <p className="text-sm text-ink-muted">
              {lookupResult.firstName} {lookupResult.lastName} ·{" "}
              {lookupResult.jobListing.jobName}
            </p>
            <div className="mt-4">
              <StatusTimeline status={lookupResult.status} />
            </div>
          </div>
        )}
      </section>

      <div className="border-t border-line" />

      {/* main flow */}
      <section className="mx-auto w-full max-w-2xl px-6">
        {view.step === "browse" && (
          <BrowseRoles
            listings={listings}
            onApply={(listing) => setView({ step: "form", listing })}
          />
        )}
        {view.step === "form" && (
          <ApplicationForm
            listing={view.listing}
            onBack={() => setView({ step: "browse" })}
            onSubmitted={(applicant) =>
              setView({ step: "confirmation", applicant })
            }
          />
        )}
        {view.step === "confirmation" && (
          <Confirmation applicant={view.applicant} />
        )}
      </section>
    </div>
  );
}

/** Step 1: the list of open roles a candidate can apply to. */
function BrowseRoles({
  listings,
  onApply,
}: {
  listings: JobListing[];
  onApply: (listing: JobListing) => void;
}) {
  return (
    <div>
      <h2 className="font-serif text-3xl">Open roles</h2>
      <p className="mt-2 text-ink-muted">
        Pick a role to see what it&rsquo;s actually looking for, then apply in
        under a minute.
      </p>

      <ul className="mt-8 flex flex-col divide-y divide-line border-y border-line">
        {listings.map((listing) => (
          <li key={listing.id} className="flex items-center justify-between gap-6 py-6">
            <div>
              <h3 className="font-serif text-xl">{listing.jobName}</h3>
              <p className="mt-1 max-w-md text-sm text-ink-muted">
                {listing.jobDescription}
              </p>
              <p className="mt-2 font-mono text-xs uppercase tracking-wide text-ink-faint">
                {listing.minimumWorkExperience}+ yrs experience
                {listing.requiredCertification ? " · certification required" : ""}
              </p>
            </div>
            <Button variant="ghost" onClick={() => onApply(listing)}>
              Apply
              <ArrowRight className="size-4" />
            </Button>
          </li>
        ))}
      </ul>
    </div>
  );
}

/**
 * Step 2: the application form for one specific listing. Deliberately asks
 * only for fields the public should be trusted to set — no password or
 * national ID, unlike the full Applicant entity — matching the scope of
 * CreateApplicantRequest on the backend.
 */
function ApplicationForm({
  listing,
  onBack,
  onSubmitted,
}: {
  listing: JobListing;
  onBack: () => void;
  onSubmitted: (applicant: Applicant) => void;
}) {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [yearsOfExperience, setYearsOfExperience] = useState("");
  const [hasCertification, setHasCertification] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const applicant = await createApplicant({
        firstName,
        lastName,
        email,
        phoneNumber,
        yearsOfExperience: Number(yearsOfExperience),
        hasCertification,
        jobListingId: listing.id,
      });
      onSubmitted(applicant);
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : "Something went wrong submitting your application."
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <button
        onClick={onBack}
        className="text-sm text-ink-muted underline underline-offset-4 hover:text-ink"
      >
        ← All roles
      </button>
      <h2 className="mt-4 font-serif text-3xl">{listing.jobName}</h2>
      <p className="mt-1 font-mono text-xs uppercase tracking-wide text-ink-faint">
        {listing.minimumWorkExperience}+ yrs experience
        {listing.requiredCertification ? " · certification required" : ""}
      </p>

      <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-5">
        <div className="grid grid-cols-2 gap-4">
          <Field label="First name">
            <input
              required
              value={firstName}
              onChange={(e) => setFirstName(e.target.value)}
              className="input"
            />
          </Field>
          <Field label="Last name">
            <input
              required
              value={lastName}
              onChange={(e) => setLastName(e.target.value)}
              className="input"
            />
          </Field>
        </div>

        <Field label="Email">
          <input
            required
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="input"
          />
        </Field>

        <Field label="Phone">
          <input
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            className="input"
          />
        </Field>

        <Field label="Years of experience">
          <input
            required
            type="number"
            min={0}
            value={yearsOfExperience}
            onChange={(e) => setYearsOfExperience(e.target.value)}
            className="input"
          />
        </Field>

        <label className="flex items-center gap-2 text-sm">
          <input
            type="checkbox"
            checked={hasCertification}
            onChange={(e) => setHasCertification(e.target.checked)}
            className="size-4 accent-ink"
          />
          I hold the certification listed for this role
          {!listing.requiredCertification && (
            <span className="text-ink-faint">(not required for this one)</span>
          )}
        </label>

        {error && (
          <p className="rounded-md bg-warn-tint px-3 py-2 text-sm text-warn">
            {error}
          </p>
        )}

        <Button type="submit" disabled={submitting} className="w-fit">
          {submitting ? "Submitting…" : "Submit application"}
        </Button>
      </form>
    </div>
  );
}

/** Label + input wrapper shared by every field in ApplicationForm. */
function Field({
  label,
  children,
}: {
  label: string;
  children: ReactNode;
}) {
  return (
    <label className="block text-sm">
      <span className="mb-1.5 block font-medium text-ink-muted">{label}</span>
      {children}
    </label>
  );
}

/** Step 3: shows the new applicant's ID (their lookup key) and current status. */
function Confirmation({ applicant }: { applicant: Applicant }) {
  return (
    <div>
      <h2 className="font-serif text-3xl">Application received.</h2>
      <p className="mt-2 max-w-md text-ink-muted">
        Save this ID — you can check back anytime without waiting for an
        email.
      </p>
      <p className="mt-4 inline-block rounded-md bg-paper-raised px-4 py-2 font-mono text-lg">
        #{applicant.id}
      </p>

      <div className="mt-10">
        <StatusTimeline status={applicant.status} />
      </div>
    </div>
  );
}
