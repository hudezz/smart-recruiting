import Link from "next/link";
import type { ButtonHTMLAttributes, ReactNode } from "react";

// solid = primary CTA (charcoal fill), ghost = secondary CTA (outlined),
// text = lowest-emphasis inline action (underlined).
type Variant = "solid" | "ghost" | "text";

const base =
  "inline-flex items-center gap-2 font-semibold text-sm rounded-full transition-colors disabled:opacity-40 disabled:cursor-not-allowed";

const variants: Record<Variant, string> = {
  solid: "bg-ink text-paper px-5 py-2.5 hover:bg-ink/85",
  ghost:
    "border border-line-strong text-ink px-5 py-2.5 hover:bg-paper-raised",
  text: "text-ink underline decoration-line-strong underline-offset-4 hover:decoration-ink",
};

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  children: ReactNode;
}

/** A clickable <button> styled per the shared variants — for actions, not navigation. */
export function Button({
  variant = "solid",
  className = "",
  children,
  ...rest
}: ButtonProps) {
  return (
    <button className={`${base} ${variants[variant]} ${className}`} {...rest}>
      {children}
    </button>
  );
}

interface LinkButtonProps {
  href: string;
  variant?: Variant;
  className?: string;
  children: ReactNode;
}

/** Same visual styling as Button, but renders a next/link <a> for actual navigation. */
export function LinkButton({
  href,
  variant = "solid",
  className = "",
  children,
}: LinkButtonProps) {
  return (
    <Link href={href} className={`${base} ${variants[variant]} ${className}`}>
      {children}
    </Link>
  );
}
