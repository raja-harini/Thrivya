import { Link } from "react-router-dom";
import "../../styles/career-evidence.css";

export function humanize(value) {
  if (!value) return "Not available";

  return String(value)
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

export function formatDate(value) {
  if (!value) return "Not recorded";

  const date = new Date(value);

  return Number.isNaN(date.getTime())
    ? "Invalid date"
    : date.toLocaleString();
}

export function CareerPage({
  title,
  description,
  backTo = "/student",
  children,
}) {
  return (
    <main className="career-page">
      <div className="career-container">
        <Link className="career-back" to={backTo}>
          ← Back
        </Link>

        <header className="career-heading">
          <p className="career-brand">THRIVYA</p>
          <h1>{title}</h1>
          {description && <p>{description}</p>}
        </header>

        {children}
      </div>
    </main>
  );
}

export function StatusBadge({ value }) {
  const positive = ["SUPPORTED", "ACTIVE"].includes(value);

  const negative = [
    "CONTRADICTED",
    "REVOKED",
    "EXPIRED",
  ].includes(value);

  const className = positive
    ? "career-badge career-badge-positive"
    : negative
    ? "career-badge career-badge-negative"
    : "career-badge career-badge-neutral";

  return <span className={className}>{humanize(value)}</span>;
}

export function Notice({ type = "info", children }) {
  if (!children) return null;

  return (
    <div
      className={`career-notice career-notice-${type}`}
      role={type === "error" ? "alert" : "status"}
    >
      {children}
    </div>
  );
}

export function QueryState({ loading, error }) {
  if (loading) {
    return <Notice>Loading current backend data…</Notice>;
  }

  if (error) {
    return <Notice type="error">{error}</Notice>;
  }

  return null;
}

export function SafeExternalLink({ url, children }) {
  let safeUrl = null;

  try {
    const parsed = new URL(url);

    if (
      parsed.protocol === "https:" &&
      !parsed.username &&
      !parsed.password
    ) {
      safeUrl = parsed.href;
    }
  } catch {
    safeUrl = null;
  }

  if (!safeUrl) {
    return <span>No valid HTTPS reference available.</span>;
  }

  return (
    <a
      href={safeUrl}
      target="_blank"
      rel="noopener noreferrer"
      referrerPolicy="no-referrer"
      className="career-link"
    >
      {children}
    </a>
  );
}