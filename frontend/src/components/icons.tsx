// Small stroke icons (currentColor) used across the UI in place of emoji.

type IconProps = { className?: string };

const base = {
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 2,
  strokeLinecap: 'round' as const,
  strokeLinejoin: 'round' as const,
};

/** Traffic light: a housing with three lamps. */
export function TrafficLightIcon({ className }: IconProps) {
  return (
    <svg {...base} className={className} width="18" height="18">
      <rect x="8" y="2" width="8" height="20" rx="3" />
      <circle cx="12" cy="7" r="1.3" />
      <circle cx="12" cy="12" r="1.3" />
      <circle cx="12" cy="17" r="1.3" />
    </svg>
  );
}

/** No-entry sign — closing a street. */
export function BlockIcon({ className }: IconProps) {
  return (
    <svg {...base} className={className} width="18" height="18">
      <circle cx="12" cy="12" r="9" />
      <line x1="7" y1="12" x2="17" y2="12" />
    </svg>
  );
}

/** Check inside a circle — reopening a street. */
export function ReopenIcon({ className }: IconProps) {
  return (
    <svg {...base} className={className} width="18" height="18">
      <circle cx="12" cy="12" r="9" />
      <path d="M8.5 12l2.5 2.5 4.5-5" />
    </svg>
  );
}

/** Plus in a circle — marking a street as a traffic source. */
export function SourceIcon({ className }: IconProps) {
  return (
    <svg {...base} className={className} width="18" height="18">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 8v8M8 12h8" />
    </svg>
  );
}

/** Minus in a circle — removing a traffic source. */
export function RemoveSourceIcon({ className }: IconProps) {
  return (
    <svg {...base} className={className} width="18" height="18">
      <circle cx="12" cy="12" r="9" />
      <path d="M8 12h8" />
    </svg>
  );
}

/** Single arrow — a one-way street. */
export function ArrowRightIcon({ className }: IconProps) {
  return (
    <svg {...base} className={className} width="16" height="16">
      <path d="M4 12h15M13 6l6 6-6 6" />
    </svg>
  );
}

/** Double-headed arrow — a two-way street. */
export function ArrowBothIcon({ className }: IconProps) {
  return (
    <svg {...base} className={className} width="16" height="16">
      <path d="M7 7l-4 5 4 5M17 7l4 5-4 5M3 12h18" />
    </svg>
  );
}
