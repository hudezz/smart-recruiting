/**
 * Typed client for the Smart Recruiting REST API.
 *
 * Every call goes through the relative path `/api/recruitment/...`, which
 * `next.config.ts` rewrites server-side to the Spring Boot backend. That
 * means the browser only ever talks to the same origin it's already on —
 * no CORS configuration needed anywhere.
 */

export type ApplicationStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface JobListing {
  id: number;
  jobName: string;
  jobDescription: string;
  minimumWorkExperience: number;
  requiredCertification: boolean;
}

export interface Applicant {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string | null;
  nationalId: string | null;
  yearsOfExperience: number;
  hasCertification: boolean;
  status: ApplicationStatus;
  jobListing: JobListing;
}

/** Body for POST /applicants. Mirrors the backend's CreateApplicantRequest. */
export interface CreateApplicantPayload {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  yearsOfExperience: number;
  hasCertification: boolean;
  jobListingId: number;
}

/**
 * Thrown for any non-2xx response. `status` lets callers branch on specific
 * codes (e.g. treat 404 as "not found" rather than a generic failure), and
 * `message` carries the plain-text body the backend's GlobalExceptionHandler
 * returns (e.g. "No job listing found with id 5").
 */
export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
    this.name = "ApiError";
  }
}

/** Shared fetch wrapper: prefixes the API base path and normalizes errors into ApiError. */
async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`/api/recruitment${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init,
  });

  if (!res.ok) {
    const message = await res.text().catch(() => res.statusText);
    throw new ApiError(res.status, message || res.statusText);
  }

  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

/** GET /job-listings — every open role, used by both the admin rail and the apply page. */
export function listJobListings(): Promise<JobListing[]> {
  return request<JobListing[]>("/job-listings");
}

/**
 * GET /applications/filter — applicants for a listing who meet the given
 * minimum experience and (optionally) hold the required certification.
 */
export function filterApplications(
  jobListingId: number,
  minExperience: number,
  requiresCertification: boolean
): Promise<Applicant[]> {
  const params = new URLSearchParams({
    jobListingId: String(jobListingId),
    minExperience: String(minExperience),
    requiresCertification: String(requiresCertification),
  });
  return request<Applicant[]>(`/applications/filter?${params}`);
}

/** GET /match-score — the 0-100 weighted score for one applicant against one listing. */
export function getMatchScore(
  applicantId: number,
  jobListingId: number
): Promise<number> {
  const params = new URLSearchParams({
    applicantId: String(applicantId),
    jobListingId: String(jobListingId),
  });
  return request<number>(`/match-score?${params}`);
}

/** POST /applications/bulk-status — applies one status to a set of applicants transactionally. */
export function bulkUpdateStatuses(
  applicantIds: number[],
  newStatus: ApplicationStatus
): Promise<void> {
  return request<void>("/applications/bulk-status", {
    method: "POST",
    body: JSON.stringify({ applicantIds, newStatus }),
  });
}

/** POST /applicants — a candidate's new application. Always created as PENDING. */
export function createApplicant(
  payload: CreateApplicantPayload
): Promise<Applicant> {
  return request<Applicant>("/applicants", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

/** GET /applicants/{id} — used by the "check your status" lookup on the apply page. */
export function getApplicant(applicantId: number): Promise<Applicant> {
  return request<Applicant>(`/applicants/${applicantId}`);
}
