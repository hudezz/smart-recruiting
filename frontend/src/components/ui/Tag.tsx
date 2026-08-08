/** Bracket-wrapped uppercase section label, e.g. "[ THE DOCKET ]" — the spec's tagged-section motif. */
export function Tag({ children }: { children: string }) {
  return (
    <span className="font-mono text-xs font-medium tracking-[0.12em] uppercase text-ink-muted">
      [&nbsp;{children}&nbsp;]
    </span>
  );
}
