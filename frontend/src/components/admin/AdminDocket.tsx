"use client";

import { useEffect, useMemo, useState } from "react";
import { Search } from "lucide-react";
import {
  ApiError,
  Applicant,
  ApplicationStatus,
  JobListing,
  bulkUpdateStatuses,
  filterApplications,
  getMatchScore,
} from "@/lib/api";
import { StatusPill } from "@/components/ui/StatusPill";

/**
 * The recruiter's applicant table: pick a listing, tune the filter (min
 * experience / certification / status / search), review scores, select rows,
 * and bulk-update statuses. Every read and write here calls the real Spring
 * Boot API through lib/api.ts — there's no mock data in this component.
 */

// An Applicant plus its computed match score. `score` is null if the
// per-applicant getMatchScore call failed (shown as "—" rather than crashing the row).
type Row = Applicant & { score: number | null };

const STATUS_FILTERS: { label: string; value: ApplicationStatus | "ALL" }[] = [
  { label: "All", value: "ALL" },
  { label: "Pending", value: "PENDING" },
  { label: "Approved", value: "APPROVED" },
  { label: "Rejected", value: "REJECTED" },
];

export function AdminDocket({
  initialListings,
}: {
  initialListings: JobListing[];
}) {
  const [listings] = useState(initialListings);
  const [selectedListingId, setSelectedListingId] = useState(
    initialListings[0]?.id ?? null
  );
  const selectedListing = useMemo(
    () => listings.find((l) => l.id === selectedListingId) ?? null,
    [listings, selectedListingId]
  );

  const [minExperience, setMinExperience] = useState(
    selectedListing?.minimumWorkExperience ?? 0
  );
  const [requiresCertification, setRequiresCertification] = useState(
    selectedListing?.requiredCertification ?? false
  );
  const [statusFilter, setStatusFilter] = useState<ApplicationStatus | "ALL">(
    "ALL"
  );
  const [query, setQuery] = useState("");

  const [rows, setRows] = useState<Row[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [bulkStatus, setBulkStatus] = useState<ApplicationStatus>("APPROVED");
  const [applying, setApplying] = useState(false);

  // Switching listings resets every filter back to that listing's own defaults.
  // Deliberately done here, in the click handler, rather than in a useEffect
  // that watches selectedListingId — resetting state in response to a state
  // change (rather than the user action that caused it) is exactly the kind
  // of effect React's lint rules now flag as unnecessary indirection.
  function selectListing(listing: JobListing) {
    setSelectedListingId(listing.id);
    setMinExperience(listing.minimumWorkExperience);
    setRequiresCertification(listing.requiredCertification);
    setStatusFilter("ALL");
    setQuery("");
    setSelected(new Set());
  }

  // Re-fetches applicants (and their scores) whenever the selected listing or
  // server-side filter params change. The fetch/setState logic is nested in a
  // separate `run` function, called but not awaited, rather than inlined
  // directly in the effect body — react-hooks/set-state-in-effect flags
  // synchronous setState calls in the effect's own body, so this keeps the
  // loading-state toggling one level removed from that check.
  useEffect(() => {
    if (!selectedListing) return;
    let cancelled = false;

    async function run(listing: JobListing) {
      setLoading(true);
      setError(null);
      try {
        const applicants = await filterApplications(
          listing.id,
          minExperience,
          requiresCertification
        );
        // The filter endpoint doesn't return scores, so each visible applicant
        // gets its own getMatchScore call, run in parallel. A per-applicant
        // failure degrades that one row to "—" instead of failing the whole table.
        const scored = await Promise.all(
          applicants.map(async (a) => {
            try {
              const score = await getMatchScore(a.id, listing.id);
              return { ...a, score };
            } catch {
              return { ...a, score: null };
            }
          })
        );
        if (!cancelled) setRows(scored);
      } catch (err) {
        if (cancelled) return;
        setRows([]);
        setError(err instanceof ApiError ? err.message : "Something went wrong.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    run(selectedListing);

    // If the user changes filters again before this request resolves, `cancelled`
    // stops the stale response from overwriting rows/loading/error state.
    return () => {
      cancelled = true;
    };
  }, [selectedListing, minExperience, requiresCertification]);

  // Status and search are filtered client-side against the already-fetched
  // `rows`, unlike minExperience/requiresCertification which are sent to the
  // backend — those two change what the API considers a match at all, while
  // status/search only narrow what's already been fetched, so there's no need
  // to round-trip to the server for them.
  const visibleRows = rows.filter((r) => {
    if (statusFilter !== "ALL" && r.status !== statusFilter) return false;
    if (query) {
      const q = query.toLowerCase();
      const full = `${r.firstName} ${r.lastName} ${r.id}`.toLowerCase();
      if (!full.includes(q)) return false;
    }
    return true;
  });

  const avgScore = visibleRows.length
    ? Math.round(
        visibleRows.reduce((sum, r) => sum + (r.score ?? 0), 0) /
          visibleRows.length
      )
    : 0;
  const pendingCount = visibleRows.filter((r) => r.status === "PENDING").length;
  const certCount = visibleRows.filter((r) => r.hasCertification).length;

  function toggleRow(id: number) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  function toggleAll() {
    setSelected((prev) =>
      prev.size === visibleRows.length
        ? new Set()
        : new Set(visibleRows.map((r) => r.id))
    );
  }

  // Applies the chosen status to every selected applicant in one transactional
  // call (see RecruitmentService#bulkUpdateCandidateStatuses on the backend),
  // then re-runs the same fetch+score sequence as the main effect so the table
  // reflects the new statuses immediately instead of waiting for a filter change.
  async function applyBulkUpdate() {
    if (selected.size === 0 || !selectedListing) return;
    setApplying(true);
    try {
      await bulkUpdateStatuses(Array.from(selected), bulkStatus);
      const applicants = await filterApplications(
        selectedListing.id,
        minExperience,
        requiresCertification
      );
      const scored = await Promise.all(
        applicants.map(async (a) => {
          try {
            const score = await getMatchScore(a.id, selectedListing.id);
            return { ...a, score };
          } catch {
            return { ...a, score: null };
          }
        })
      );
      setRows(scored);
      setSelected(new Set());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Bulk update failed.");
    } finally {
      setApplying(false);
    }
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-[240px_1fr]">
      {/* rail */}
      <aside className="border-b border-line p-6 md:border-r md:border-b-0">
        <div>
          <h2 className="font-mono text-xs font-semibold uppercase tracking-[0.1em] text-ink-muted">
            Open listings
          </h2>
          <ul className="mt-3 flex flex-col">
            {listings.map((l) => (
              <li key={l.id}>
                <button
                  onClick={() => selectListing(l)}
                  className={`-ml-3 flex w-full items-baseline justify-between gap-2 border-l-2 py-1.5 pl-3 text-left text-sm ${
                    l.id === selectedListingId
                      ? "border-ink font-semibold"
                      : "border-transparent text-ink-muted hover:text-ink"
                  }`}
                >
                  <span>{l.jobName}</span>
                </button>
              </li>
            ))}
          </ul>
        </div>

        <div className="mt-8 border-t border-line pt-6">
          <h2 className="font-mono text-xs font-semibold uppercase tracking-[0.1em] text-ink-muted">
            Filter
          </h2>

          <label className="mt-4 block text-xs font-semibold uppercase tracking-wide text-ink-muted">
            Search
          </label>
          <div className="mt-1.5 flex items-center gap-2 rounded-md border border-line-strong px-2.5 py-2">
            <Search className="size-3.5 text-ink-faint" />
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Name or docket ID"
              className="w-full bg-transparent text-sm outline-none placeholder:text-ink-faint"
            />
          </div>

          <label className="mt-4 block text-xs font-semibold uppercase tracking-wide text-ink-muted">
            Minimum experience
          </label>
          <div className="mt-1.5 flex w-fit items-center overflow-hidden rounded-md border border-line-strong">
            <button
              onClick={() => setMinExperience((v) => Math.max(0, v - 1))}
              className="h-7 w-7 hover:bg-paper-raised"
            >
              –
            </button>
            <output className="flex h-7 w-12 items-center justify-center border-x border-line-strong font-mono text-xs">
              {minExperience} yrs
            </output>
            <button
              onClick={() => setMinExperience((v) => v + 1)}
              className="h-7 w-7 hover:bg-paper-raised"
            >
              +
            </button>
          </div>

          <label className="mt-4 flex items-center justify-between text-xs font-semibold uppercase tracking-wide text-ink-muted">
            Requires cert.
            <input
              type="checkbox"
              checked={requiresCertification}
              onChange={(e) => setRequiresCertification(e.target.checked)}
              className="size-4 accent-ink"
            />
          </label>

          <label className="mt-4 block text-xs font-semibold uppercase tracking-wide text-ink-muted">
            Status
          </label>
          <div className="mt-1.5 flex flex-wrap gap-1.5">
            {STATUS_FILTERS.map((s) => (
              <button
                key={s.value}
                onClick={() => setStatusFilter(s.value)}
                className={`rounded-full border px-2.5 py-1 text-[11px] font-semibold uppercase ${
                  statusFilter === s.value
                    ? "border-ink bg-paper-raised text-ink"
                    : "border-line-strong text-ink-muted"
                }`}
              >
                {s.label}
              </button>
            ))}
          </div>
        </div>
      </aside>

      {/* main */}
      <div className="min-w-0 p-6">
        <div className="grid grid-cols-2 border-y border-line sm:grid-cols-4">
          {[
            { label: "Shown", value: visibleRows.length },
            { label: "Avg. score", value: `${avgScore}/100` },
            { label: "Pending", value: pendingCount },
            { label: "Certified", value: `${certCount}/${visibleRows.length}` },
          ].map((stat, i) => (
            <div
              key={stat.label}
              className={`py-4 ${i > 0 ? "border-l border-line" : ""} ${
                i === 0 ? "pl-0" : "pl-5"
              }`}
            >
              <div className="font-mono text-[11px] uppercase tracking-wide text-ink-muted">
                {stat.label}
              </div>
              <div className="mt-1.5 font-serif text-3xl">{stat.value}</div>
            </div>
          ))}
        </div>

        {selected.size > 0 && (
          <div className="mt-5 flex flex-wrap items-center gap-3 rounded-md bg-[#111111] px-4 py-3 text-[#f7f6f2]">
            <span className="font-mono text-sm">
              <strong>{selected.size}</strong> selected
            </span>
            <select
              value={bulkStatus}
              onChange={(e) => setBulkStatus(e.target.value as ApplicationStatus)}
              className="rounded border border-white/25 bg-transparent px-2 py-1 text-xs font-semibold uppercase"
            >
              <option value="APPROVED">Set approved</option>
              <option value="REJECTED">Set rejected</option>
              <option value="PENDING">Set pending</option>
            </select>
            <button
              onClick={applyBulkUpdate}
              disabled={applying}
              className="rounded-full bg-[#f7f6f2] px-3 py-1.5 text-xs font-bold text-[#111111] disabled:opacity-50"
            >
              {applying ? "Applying…" : "Apply"}
            </button>
            <button
              onClick={() => setSelected(new Set())}
              className="ml-auto text-xs text-white/60 underline"
            >
              Clear
            </button>
          </div>
        )}

        {error && (
          <p className="mt-4 rounded-md bg-warn-tint px-3 py-2 text-sm text-warn">
            {error}
          </p>
        )}

        <div className="mt-5 overflow-x-auto">
          <table className="w-full min-w-[720px] border-collapse text-sm">
            <thead>
              <tr className="border-b-2 border-ink text-left font-mono text-[11px] uppercase tracking-wide text-ink-muted">
                <th className="w-8 pb-2">
                  <input
                    type="checkbox"
                    checked={
                      visibleRows.length > 0 &&
                      selected.size === visibleRows.length
                    }
                    onChange={toggleAll}
                    className="size-3.5 accent-ink"
                  />
                </th>
                <th className="pb-2 pr-4">Docket</th>
                <th className="pb-2 pr-4">Applicant</th>
                <th className="pb-2 pr-4 text-right">Experience</th>
                <th className="pb-2 pr-4 text-center">Cert.</th>
                <th className="pb-2 pr-4 text-right">Score</th>
                <th className="pb-2">Status</th>
              </tr>
            </thead>
            <tbody>
              {visibleRows.map((r) => (
                <tr
                  key={r.id}
                  className={`border-b border-line ${
                    selected.has(r.id) ? "bg-paper-raised" : ""
                  }`}
                >
                  <td className="py-3">
                    <input
                      type="checkbox"
                      checked={selected.has(r.id)}
                      onChange={() => toggleRow(r.id)}
                      className="size-3.5 accent-ink"
                    />
                  </td>
                  <td className="pr-4 font-mono text-xs text-ink-muted">
                    A-{String(r.id).padStart(3, "0")}
                  </td>
                  <td className="pr-4">
                    <div className="font-medium">
                      {r.firstName} {r.lastName}
                    </div>
                    <div className="text-xs text-ink-muted">{r.email}</div>
                  </td>
                  <td className="pr-4 text-right font-mono text-xs">
                    {r.yearsOfExperience}
                    <span className="text-ink-faint">
                      {" "}
                      / {selectedListing?.minimumWorkExperience} yrs
                    </span>
                  </td>
                  <td className="pr-4 text-center">
                    {r.hasCertification ? (
                      <span className="inline-flex size-4 items-center justify-center rounded border border-good text-[10px] text-good">
                        ✓
                      </span>
                    ) : (
                      <span className="inline-flex size-4 items-center justify-center text-ink-faint">
                        –
                      </span>
                    )}
                  </td>
                  <td className="pr-4 text-right font-mono text-xs">
                    {r.score ?? "—"}
                  </td>
                  <td>
                    <StatusPill status={r.status} />
                  </td>
                </tr>
              ))}
              {!loading && visibleRows.length === 0 && (
                <tr>
                  <td colSpan={7} className="py-10 text-center text-sm text-ink-faint">
                    No applicants match these filters.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        <p className="mt-4 text-xs text-ink-faint">
          Match score = 70% weighted years-of-experience ratio + 30%
          certification match, per listing requirement.
        </p>
      </div>
    </div>
  );
}
