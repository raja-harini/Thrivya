import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { getMyProjects } from "../../api/studentApi";

function getSafeWebUrl(value) {
  if (typeof value !== "string" || !value.trim()) {
    return null;
  }

  try {
    const url = new URL(value);

    if (url.protocol !== "https:" && url.protocol !== "http:") {
      return null;
    }

    return url.href;
  } catch {
    return null;
  }
}

export default function MyProjects() {
  const navigate = useNavigate();

  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let active = true;

    async function loadProjects() {
      setLoading(true);
      setError("");

      try {
        const result = await getMyProjects();

        if (!active) {
          return;
        }

        if (!Array.isArray(result)) {
          throw new Error(
            "Unexpected projects response. Expected a list of projects."
          );
        }

        setProjects(result);
      } catch (requestError) {
        if (!active) {
          return;
        }

        if (requestError.response?.status === 401) {
          localStorage.removeItem("thrivyaToken");
          localStorage.removeItem("thrivyaUser");

          navigate("/login", { replace: true });
          return;
        }

        setError(
          requestError.response?.data?.message ||
            requestError.message ||
            "Unable to load your projects."
        );
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadProjects();

    return () => {
      active = false;
    };
  }, [navigate, reloadKey]);

  function handleRetry() {
    setReloadKey((previous) => previous + 1);
  }

  function handleOpenEvidence(projectId) {
    const id = Number(projectId);

    if (!Number.isSafeInteger(id) || id <= 0) {
      setError(
        "This project does not have a valid project ID. Please refresh the page."
      );
      return;
    }

    navigate(`/student/projects/${id}/evidence`);
  }

  return (
    <main style={styles.page}>
      <section style={styles.container}>
        <header style={styles.header}>
          <div>
            <p style={styles.brand}>Thrivya</p>
            <h1 style={styles.heading}>
              My project-based learning
            </h1>
          </div>

          <button
            type="button"
            onClick={() => navigate("/student")}
            style={styles.button}
          >
            Back to dashboard
          </button>
        </header>

        <p style={styles.description}>
          Track selected projects and record your actual work.
          Selecting a project does not mean you have completed
          the project or verified its skills.
        </p>

        {loading && (
          <section style={styles.card} aria-busy="true">
            <p role="status">Loading your projects...</p>
          </section>
        )}

        {!loading && error && (
          <section style={styles.card}>
            <p role="alert" style={styles.error}>
              {error}
            </p>

            <button
              type="button"
              onClick={handleRetry}
              style={styles.button}
            >
              Try again
            </button>
          </section>
        )}

        {!loading && !error && projects.length === 0 && (
          <section style={styles.card}>
            <h2>No projects selected yet</h2>

            <p style={styles.description}>
              Open your skill-gap analysis and choose a
              suggested project to start building practical
              evidence for your skills.
            </p>

            <button
              type="button"
              onClick={() =>
                navigate("/student/skill-gaps")
              }
              style={styles.primaryButton}
            >
              View skill gaps
            </button>
          </section>
        )}

        {!loading &&
          !error &&
          projects.map((project) => {
            const projectId = Number(
              project.studentProjectId
            );

            const validProjectId =
              Number.isSafeInteger(projectId) &&
              projectId > 0;

            const githubUrl = getSafeWebUrl(
              project.githubUrl
            );

            const liveDemoUrl = getSafeWebUrl(
              project.liveDemoUrl
            );

            const status = String(
              project.status || "SELECTED"
            )
              .split("_")
              .join(" ");

            const skills = Array.isArray(project.skills)
              ? project.skills
              : [];

            return (
              <article
                key={project.studentProjectId}
                style={styles.card}
              >
                <div style={styles.projectHeader}>
                  <h2 style={styles.projectTitle}>
                    {project.title || "Untitled project"}
                  </h2>

                  <span style={styles.statusBadge}>
                    {status}
                  </span>
                </div>

                <p style={styles.detail}>
                  Target role:{" "}
                  <span style={styles.detailValue}>
                    {project.roleTitle || "Not specified"}
                  </span>
                </p>

                <section>
                  <h3 style={styles.subheading}>
                    Skills covered
                  </h3>

                  {skills.length > 0 ? (
                    <div style={styles.skillList}>
                      {skills.map((skill, index) => (
                        <span
                          key={`${skill}-${index}`}
                          style={styles.skillBadge}
                        >
                          {skill}
                        </span>
                      ))}
                    </div>
                  ) : (
                    <p style={styles.description}>
                      No skills recorded yet.
                    </p>
                  )}
                </section>

                {project.studentDescription && (
                  <section>
                    <h3 style={styles.subheading}>
                      Your project description
                    </h3>

                    <p style={styles.text}>
                      {project.studentDescription}
                    </p>
                  </section>
                )}

                {project.evidenceText && (
                  <section style={styles.evidenceBox}>
                    <h3 style={styles.subheading}>
                      Submitted evidence
                    </h3>

                    <p style={styles.text}>
                      {project.evidenceText}
                    </p>

                    <p style={styles.hint}>
                      Student-provided evidence is not
                      automatically independently verified.
                    </p>
                  </section>
                )}

                {(githubUrl || liveDemoUrl) && (
                  <div style={styles.links}>
                    {githubUrl && (
                      <a
                        href={githubUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        style={styles.link}
                      >
                        Open GitHub repository
                      </a>
                    )}

                    {liveDemoUrl && (
                      <a
                        href={liveDemoUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        style={styles.link}
                      >
                        Open live demo
                      </a>
                    )}
                  </div>
                )}

                <button
                  type="button"
                  onClick={() =>
                    handleOpenEvidence(
                      project.studentProjectId
                    )
                  }
                  disabled={!validProjectId}
                  style={{
                    ...styles.primaryButton,
                    ...(!validProjectId
                      ? styles.disabledButton
                      : {}),
                  }}
                >
                  Add or update project evidence
                </button>

                {!validProjectId && (
                  <p style={styles.hint}>
                    A valid project ID is required before
                    evidence can be updated.
                  </p>
                )}
              </article>
            );
          })}
      </section>
    </main>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    padding: "32px 20px",
    background: "#f4f7fb",
    color: "#172033",
  },

  container: {
    width: "100%",
    maxWidth: "950px",
    margin: "0 auto",
  },

  header: {
    display: "flex",
    flexWrap: "wrap",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "16px",
    marginBottom: "16px",
  },

  brand: {
    margin: "0 0 8px",
    color: "#7c3aed",
    fontSize: "17px",
    fontWeight: 700,
  },

  heading: {
    margin: 0,
    fontSize: "30px",
  },

  description: {
    color: "#475569",
    lineHeight: 1.6,
  },

  card: {
    marginTop: "18px",
    padding: "24px",
    border: "1px solid #e2e8f0",
    borderRadius: "14px",
    background: "#ffffff",
    boxShadow: "0 6px 18px rgba(15, 23, 42, 0.05)",
  },

  projectHeader: {
    display: "flex",
    flexWrap: "wrap",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "12px",
  },

  projectTitle: {
    margin: "0 0 8px",
    fontSize: "23px",
  },

  statusBadge: {
    padding: "6px 10px",
    borderRadius: "20px",
    background: "#ede9fe",
    color: "#6d28d9",
    fontSize: "12px",
    fontWeight: 700,
  },

  detail: {
    color: "#475569",
    lineHeight: 1.6,
  },

  detailValue: {
    color: "#172033",
    fontWeight: 600,
  },

  subheading: {
    margin: "18px 0 10px",
    fontSize: "16px",
  },

  skillList: {
    display: "flex",
    flexWrap: "wrap",
    gap: "8px",
  },

  skillBadge: {
    padding: "6px 10px",
    borderRadius: "20px",
    background: "#eff6ff",
    color: "#1d4ed8",
    fontSize: "13px",
  },

  text: {
    margin: "8px 0",
    color: "#334155",
    lineHeight: 1.6,
    whiteSpace: "pre-wrap",
    overflowWrap: "anywhere",
  },

  evidenceBox: {
    marginTop: "18px",
    padding: "14px",
    border: "1px solid #dbeafe",
    borderRadius: "10px",
    background: "#f8fafc",
  },

  hint: {
    color: "#64748b",
    fontSize: "13px",
    lineHeight: 1.5,
  },

  links: {
    display: "flex",
    flexWrap: "wrap",
    gap: "16px",
    marginTop: "18px",
  },

  link: {
    color: "#2563eb",
    fontWeight: 600,
  },

  button: {
    padding: "10px 14px",
    border: "1px solid #cbd5e1",
    borderRadius: "8px",
    background: "#ffffff",
    color: "#334155",
    cursor: "pointer",
  },

  primaryButton: {
    marginTop: "18px",
    padding: "11px 16px",
    border: "none",
    borderRadius: "8px",
    background: "#7c3aed",
    color: "#ffffff",
    fontWeight: 600,
    cursor: "pointer",
  },

  disabledButton: {
    opacity: 0.55,
    cursor: "not-allowed",
  },

  error: {
    padding: "12px",
    borderRadius: "8px",
    background: "#fee2e2",
    color: "#b91c1c",
    lineHeight: 1.5,
  },
};