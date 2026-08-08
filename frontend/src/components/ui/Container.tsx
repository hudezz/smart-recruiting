import type { ReactNode } from "react";

/** Centers content at a max page width with consistent horizontal padding. */
export function Container({
  children,
  className = "",
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <div className={`mx-auto max-w-6xl px-6 md:px-10 ${className}`}>
      {children}
    </div>
  );
}
