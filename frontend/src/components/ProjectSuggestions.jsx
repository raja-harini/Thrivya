import { useState } from "react";
import {
  getProjectSuggestions,
  selectProject,
} from "../api/studentApi";

export default function ProjectSuggestions({
  skillGapId,
  skillName,
}) {
  const [projects, setProjects] =
    useState([]);

  const [message, setMessage] =
    useState("");

  const [error, setError] =
    useState("");

  const [loading, setLoading] =
    useState(false);

  async function loadProjects() {
    setLoading(true);
    setError("");

    try {
      const result =
        await getProjectSuggestions(skillGapId);

      setProjects(result);
    } catch (requestError) {
      setError(
        requestError.response?.data?.message ||
        "Unable to load project suggestions"
      );
    } finally {
      setLoading(false);
    }
  }

  async function handleSelectProject(
    suggestionId
  ) {
    setError("");
    setMessage("");

    try {
      const result =
        await selectProject(suggestionId);

      setMessage(
        `${result.title} was added to My Projects.`
      );
    } catch (requestError) {
      setError(
        requestError.response?.data?.message ||
        "Unable to select this project"
      );
    }
  }

  return (
    <section style={styles.panel}>
      <div style={styles.header}>
        <div>
          <h3>Project suggestions for {skillName}</h3>
          <p>
            Build a project to create stronger, verifiable
            evidence for this skill.
          </p>
        </div>

        <button
          onClick={loadProjects}
          disabled={loading}
          style={styles.loadButton}
        >
          {loading
            ? "Loading..."
            : "Find projects"}
        </button>
      </div>

      {error && (
        <p style={styles.error}>{error}</p>
      )}

      {message && (
        <p style={styles.success}>{message}</p>
      )}

      {projects.map((project) => (
        <article
          key={project.suggestionId}
          style={styles.card}
        >
          <h4>{project.title}</h4>

          <p>{project.shortDescription}</p>

          <p style={styles.reason}>
            <strong>Why Thrivya recommends it:</strong>{" "}
            {project.reason}
          </p>

          <p>
            <strong>Difficulty:</strong>{" "}
            {project.difficulty}
          </p>

          <p>
            <strong>Estimated time:</strong>{" "}
            {project.estimatedHours} hours
          </p>

          <p>
            <strong>Skills covered:</strong>{" "}
            {project.skillsCovered.join(", ")}
          </p>

          <details>
            <summary>
              View project plan
            </summary>

            <h5>Implementation steps</h5>

            <ol>
              {project.implementationSteps.map(
                (step, index) => (
                  <li key={index}>{step}</li>
                )
              )}
            </ol>

            <h5>Evidence to submit</h5>

            <ul>
              {project.evidenceRequired.map(
                (item, index) => (
                  <li key={index}>{item}</li>
                )
              )}
            </ul>

            <h5>Safe future resume template</h5>

            <pre style={styles.template}>
              {project.resumeTemplate}
            </pre>

            <p style={styles.trend}>
              <strong>Technology context:</strong>{" "}
              {project.technologyTrendNotes}
            </p>

            <a
              href={project.githubSearchUrl}
              target="_blank"
              rel="noreferrer"
              style={styles.link}
            >
              View similar public GitHub repositories
            </a>
          </details>

          <button
            onClick={() =>
              handleSelectProject(
                project.suggestionId
              )
            }
            style={styles.selectButton}
          >
            Add to My Projects
          </button>
        </article>
      ))}
    </section>
  );
}

const styles = {
  panel: {
    marginTop: "16px",
    padding: "18px",
    borderRadius: "12px",
    background: "#f5f3ff",
    border: "1px solid #ddd6fe",
  },
  header: {
    display: "flex",
    justifyContent: "space-between",
    gap: "16px",
  },
  loadButton: {
    height: "fit-content",
    padding: "9px 14px",
    border: "none",
    borderRadius: "8px",
    background: "#7c3aed",
    color: "white",
    cursor: "pointer",
  },
  card: {
    marginTop: "14px",
    padding: "16px",
    borderRadius: "10px",
    background: "white",
    border: "1px solid #e2e8f0",
  },
  reason: {
    padding: "10px",
    borderRadius: "8px",
    background: "#eff6ff",
  },
  template: {
    padding: "12px",
    whiteSpace: "pre-wrap",
    background: "#f8fafc",
    borderRadius: "8px",
  },
  selectButton: {
    marginTop: "14px",
    padding: "10px 14px",
    border: "none",
    borderRadius: "8px",
    background: "#2563eb",
    color: "white",
    cursor: "pointer",
  },
  link: {
    color: "#2563eb",
    fontWeight: 600,
  },
  trend: {
    color: "#475569",
  },
  error: {
    padding: "10px",
    background: "#fee2e2",
    color: "#991b1b",
  },
  success: {
    padding: "10px",
    background: "#dcfce7",
    color: "#166534",
  },
};