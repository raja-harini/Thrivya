import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

import LogoutButton from "../../components/LogoutButton";
import { getRecruiterDashboard } from "../../api/dashboardApi";

import {
  CareerPage,
  Notice,
} from "../../components/career/CareerUI";

export default function RecruiterDashboard() {
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
        const data = await getRecruiterDashboard();

        if (active) {
          setDashboard(data);
        }
      } catch (requestError) {
        if (active) {
          setError(
            requestError.response?.data?.message ||
              "Unable to load recruiter dashboard. Please try again."
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

  function handleRetry() {
    setRetryCount((previous) => previous + 1);
  }

  return (
    <CareerPage
      title="Recruiter workspace"
      backTo="/home"
      description="Inspect student-shared career profiles with traceable evidence and review limitations."
    >
      {loading && (
        <section className="career-card" aria-busy="true">
          <Notice>Loading recruiter dashboard…</Notice>
        </section>
      )}

      {!loading && error && (
        <section className="career-card">
          <h2>Unable to load your workspace</h2>

          <Notice type="error">{error}</Notice>

          <div className="career-actions">
            <button
              type="button"
              className="career-button"
              onClick={handleRetry}
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
                {dashboard.title || "Recruiter Dashboard"}
              </h2>

              {dashboard.email && (
                <p className="career-muted">
                  Logged in as: {dashboard.email}
                </p>
              )}
            </div>

            <LogoutButton />
          </div>

          <p>
            Review candidate claims, supporting artifacts,
            and scoped reviewer decisions before assessing
            technical knowledge.
          </p>

          <Notice>
            Recruiter approval and active student sharing
            permission are both required. A recruiter role
            alone does not grant access to every student.
          </Notice>

          <Notice type="warning">
            Supporting evidence applies only to the scope
            recorded in each review. It does not guarantee
            complete skill mastery or replace a technical
            interview.
          </Notice>

          <div className="career-actions">
            <Link
              className="career-button"
              to="/recruiter/career-evidence"
            >
              Open a shared candidate profile
            </Link>
          </div>
        </section>
      )}
    </CareerPage>
  );
}