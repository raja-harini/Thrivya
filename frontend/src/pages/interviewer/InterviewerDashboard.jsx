import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import LogoutButton from "../../components/LogoutButton";
import { getInterviewerDashboard } from "../../api/dashboardApi";

import {
  CareerPage,
  Notice,
} from "../../components/career/CareerUI";

export default function InterviewerDashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    let active = true;

    async function loadDashboard() {
      setLoading(true);
      setError("");
      setDashboard(null);

      try {
        const data = await getInterviewerDashboard();

        if (active) {
          setDashboard(data);
        }
      } catch (requestError) {
        if (active) {
          setError(
            requestError.response?.data?.message ||
              "Unable to load interviewer dashboard. Please try again."
          );
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    loadDashboard();

    return () => {
      active = false;
    };
  }, [retryCount]);

  return (
    <CareerPage
      title="Technical interviewer workspace"
      backTo="/home"
      description="Inspect assigned evidence and record accountable, scoped review decisions."
    >
      {loading && (
        <Notice>
          Loading interviewer dashboard…
        </Notice>
      )}

      {!loading && error && (
        <section className="career-card">
          <Notice type="error">{error}</Notice>

          <div className="career-actions">
            <button
              type="button"
              className="career-button"
              onClick={() =>
                setRetryCount((previous) => previous + 1)
              }
            >
              Try again
            </button>

            <LogoutButton />
          </div>
        </section>
      )}

      {!loading && !error && dashboard && (
        <section className="career-card">
          <div className="career-row">
            <div>
              <h2>
                {dashboard.title ||
                  "Technical Interviewer Dashboard"}
              </h2>

              {dashboard.email && (
                <p className="career-muted">
                  Logged in as: {dashboard.email}
                </p>
              )}
            </div>

            <LogoutButton />
          </div>

          {dashboard.message && (
            <p>{dashboard.message}</p>
          )}

          <Notice>
            Only approved interviewers with explicit student
            assignments can access the review queue.
          </Notice>

          <p>
            Inspect the student’s original claim, supporting
            artifacts, and contribution details. Record exactly
            what the evidence supports, how you reviewed it,
            and what remains unassessed.
          </p>

          <div className="career-actions">
            <Link
              className="career-button"
              to="/interviewer/evidence-reviews"
            >
              Open evidence review queue
            </Link>
          </div>
        </section>
      )}
    </CareerPage>
  );
}