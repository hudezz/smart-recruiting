import { AppNav } from "@/components/site/AppNav";
import { ApplyFlow } from "@/components/apply/ApplyFlow";
import { Footer } from "@/components/site/Footer";
import type { JobListing } from "@/lib/api";

const BACKEND_ORIGIN = process.env.BACKEND_ORIGIN ?? "http://localhost:8080";

// Same direct server-side fetch pattern as app/admin/page.tsx — see the comment
// there for why this bypasses the client-side /api/recruitment proxy.
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

// Server Component: fetches open listings, then hands off to the ApplyFlow
// client component for browsing roles, submitting an application, and checking status.
export default async function ApplyPage() {
  const listings = await getListings();

  return (
    <>
      <AppNav
        eyebrow="Applicant view"
        switchHref="/admin"
        switchLabel="Recruiter view →"
      />
      {listings.length === 0 ? (
        <div className="mx-auto max-w-md px-6 py-24 text-center text-sm text-ink-muted">
          Couldn&rsquo;t reach the Smart Recruiting API. Make sure the Spring
          Boot backend is running on port 8080.
        </div>
      ) : (
        <ApplyFlow listings={listings} />
      )}
      <Footer />
    </>
  );
}
