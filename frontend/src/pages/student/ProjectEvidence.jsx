import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import { updateProjectEvidence } from "../../api/studentApi";

export default function ProjectEvidence() {
  const { projectId } = useParams();
  const navigate = useNavigate();

  const numericProjectId = Number(projectId);

  const validProjectId =
    Number.isSafeInteger(numericProjectId) &&
    numericProjectId > 0;

  const [form, setForm] = useState({
    studentDescription: "",
    githubUrl: "",
    liveDemoUrl: "",
    evidenceText: "",
    status: "EVIDENCE_SUBMITTED",
  });

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    setMessage("");
  }

  async function handleSubmit(event) {
    event.preventDefault();

    setMessage("");
    setError("");

    if (!validProjectId) {
      setError(
        "Invalid project. Open My Projects and select a project first."
      );
      return;
    }

    const studentDescription =
      form.studentDescription.trim();

    if (!studentDescription) {
      setError("Please describe what you actually built.");
      return;
    }

    setLoading(true);

    try {
      const payload = {
        studentDescription,
        githubUrl: form.githubUrl.trim(),
        liveDemoUrl: form.liveDemoUrl.trim(),
        evidenceText: form.evidenceText.trim(),
        status: form.status,
      };

      const result = await updateProjectEvidence(
        numericProjectId,
        payload
      );

      setForm((previous) => ({
        ...previous,
        studentDescription:
          result.studentDescription ??
          payload.studentDescription,
        githubUrl:
          result.githubUrl ?? payload.githubUrl,
        liveDemoUrl:
          result.liveDemoUrl ?? payload.liveDemoUrl,
        evidenceText:
          result.evidenceText ?? payload.evidenceText,
        status:
          result.status ?? payload.status,
      }));

      setMessage(
        "Project details saved successfully. Submitted information is user-provided evidence; it is not automatically independently verified."
      );
    } catch (requestError) {
      const status = requestError.response?.status;

      if (status === 401) {
        localStorage.removeItem("thrivyaToken");
        localStorage.removeItem("thrivyaUser");

        navigate("/login", { replace: true });
        return;
      }

      if (status === 403) {
        setError(
          "You do not have permission to update this project."
        );
        return;
      }

      setError(
        requestError.response?.data?.message ||
          "Unable to save project evidence. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <main style={styles.page}>
      <section style={styles.card}>
        <button
          type="button"
          onClick={() => navigate("/student/my-projects")}
          style={styles.backButton}
        >
          Back to My Projects
        </button>

        <h1>Add project evidence</h1>

        <p style={styles.projectId}>
          Project ID: {projectId}
        </p>

        <p style={styles.note}>
          Add only your actual work and contribution. Do not
          claim deployment, user counts, performance metrics,
          certifications, or teamwork unless you have
          supporting evidence.
        </p>

        {!validProjectId && (
          <p role="alert" style={styles.error}>
            Invalid project ID. Return to My Projects and
            choose a project.
          </p>
        )}

        {message && (
          <p role="status" style={styles.success}>
            {message}
          </p>
        )}

        {error && (
          <p role="alert" style={styles.error}>
            {error}
          </p>
        )}

        <form
          onSubmit={handleSubmit}
          aria-busy={loading}
        >
          <fieldset
            disabled={loading || !validProjectId}
            style={styles.fieldset}
          >
            <label htmlFor="studentDescription">
              What did you build?
            </label>

            <textarea
              id="studentDescription"
              name="studentDescription"
              value={form.studentDescription}
              onChange={handleChange}
              rows={5}
              required
              placeholder="Example: Built a task-management REST API using Spring Boot and PostgreSQL."
              style={styles.input}
            />

            <label htmlFor="githubUrl">
              GitHub repository URL
            </label>

            <input
              id="githubUrl"
              type="url"
              name="githubUrl"
              value={form.githubUrl}
              onChange={handleChange}
              placeholder="https://github.com/your-name/project"
              style={styles.input}
            />

            <label htmlFor="liveDemoUrl">
              Live demo URL, if available
            </label>

            <input
              id="liveDemoUrl"
              type="url"
              name="liveDemoUrl"
              value={form.liveDemoUrl}
              onChange={handleChange}
              placeholder="https://your-project-demo.com"
              style={styles.input}
            />

            <label htmlFor="evidenceText">
              Evidence details
            </label>

            <textarea
              id="evidenceText"
              name="evidenceText"
              value={form.evidenceText}
              onChange={handleChange}
              rows={6}
              placeholder="Example: Created a Dockerfile, ran the API in a Docker container, documented setup steps in the README, and tested endpoints using Postman."
              style={styles.input}
            />

            <p style={styles.hint}>
              Describe your own contribution. You can refer
              to a README, Dockerfile, screenshots, API test
              results, documentation, or a live demo.
            </p>

            <label htmlFor="status">
              Current project status
            </label>

            <select
              id="status"
              name="status"
              value={form.status}
              onChange={handleChange}
              style={styles.input}
            >
              <option value="IN_PROGRESS">
                In progress
              </option>

              <option value="EVIDENCE_SUBMITTED">
                Evidence submitted
              </option>

              <option value="COMPLETED">
                Completed — self-reported
              </option>
            </select>

            <button
              type="submit"
              style={{
                ...styles.primaryButton,
                ...(loading || !validProjectId
                  ? styles.disabledButton
                  : {}),
              }}
            >
              {loading
                ? "Saving evidence..."
                : "Save project evidence"}
            </button>
          </fieldset>
        </form>
      </section>
    </main>
  );
}

const styles = {
  page: {
    minHeight: "100vh",
    padding: "32px 20px",
    background: "#f4f7fb",
  },

  card: {
    width: "100%",
    maxWidth: "760px",
    margin: "0 auto",
    padding: "32px",
    background: "#ffffff",
    borderRadius: "18px",
    boxShadow: "0 10px 28px rgba(15, 23, 42, 0.10)",
  },

  backButton: {
    marginBottom: "20px",
    padding: "9px 14px",
    border: "1px solid #cbd5e1",
    borderRadius: "8px",
    background: "#ffffff",
    color: "#334155",
    cursor: "pointer",
  },

  projectId: {
    color: "#64748b",
    fontSize: "14px",
  },

  note: {
    marginBottom: "20px",
    padding: "12px",
    borderRadius: "8px",
    background: "#eff6ff",
    color: "#1e3a8a",
    lineHeight: 1.5,
  },

  fieldset: {
    minWidth: 0,
    margin: 0,
    padding: 0,
    border: "none",
  },

  input: {
    boxSizing: "border-box",
    display: "block",
    width: "100%",
    margin: "8px 0 18px",
    padding: "12px",
    border: "1px solid #cbd5e1",
    borderRadius: "8px",
    fontSize: "15px",
    fontFamily: "inherit",
  },

  hint: {
    marginTop: "-8px",
    marginBottom: "18px",
    color: "#64748b",
    fontSize: "13px",
    lineHeight: 1.5,
  },

  primaryButton: {
    padding: "12px 18px",
    border: "none",
    borderRadius: "8px",
    background: "#2563eb",
    color: "#ffffff",
    cursor: "pointer",
    fontSize: "15px",
    fontWeight: 600,
  },

  disabledButton: {
    background: "#93c5fd",
    cursor: "not-allowed",
  },

  success: {
    marginBottom: "18px",
    padding: "10px",
    borderRadius: "8px",
    background: "#dcfce7",
    color: "#166534",
  },

  error: {
    marginBottom: "18px",
    padding: "10px",
    borderRadius: "8px",
    background: "#fee2e2",
    color: "#991b1b",
  },
};