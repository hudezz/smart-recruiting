import { AppNav } from "@/components/site/AppNav";
import { AdminDocket } from "@/components/admin/AdminDocket";
import type { JobListing } from "@/lib/api";

const BACKEND_ORIGIN = process.env.BACKEND_ORIGIN ?? "http://localhost:8080";

// Runs on the server, so it calls the backend directly instead of through the
// /api/recruitment proxy (that rewrite exists for browser fetches; a server-side
// fetch has no CORS restriction to route around). Fetched fresh on every request
// since this is a live admin tool, not something that should ever be cached.
async function getListings(): Promise<JobListing[]> {
  try {
    const res = await fetch(`${BACKEND_ORIGIN}/api/recruitment/job-listings`, {
      cache: "no-store",
    });
    if (!res.ok) return [];
    return res.json();
  } catch {
    return [];
  }
}

// Server Component: fetches the initial listing set, then hands off to the
// AdminDocket client component for all the interactive filtering/scoring/bulk actions.
export default async function AdminPage() {
  const listings = await getListings();

  return (
    <>
      <AppNav
        eyebrow="Recruiter view"
        switchHref="/apply"
        switchLabel="Applicant view →"
      />
      {listings.length === 0 ? (
        <div className="mx-auto max-w-md px-6 py-24 text-center text-sm text-ink-muted">
          Couldn&rsquo;t reach the Smart Recruiting API. Make sure the Spring
          Boot backend is running on port 8080.
        </div>
      ) : (
        <AdminDocket initialListings={listings} />
      )}
    </>
  );
}
