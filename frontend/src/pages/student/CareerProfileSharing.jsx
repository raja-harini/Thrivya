import { useState } from "react";
import useCareerQuery from "../../hooks/useCareerQuery";
import {
  careerErrorMessage,
  createCareerProfileShare,
  revokeCareerProfileShare,
} from "../../api/careerEvidenceApi";
import {
  CareerPage,
  formatDate,
  Notice,
  QueryState,
  StatusBadge,
} from "../../components/career/CareerUI";

export default function CareerProfileSharing() {
  const query = useCareerQuery(
    "/student/career-profile/shares"
  );

  const [form, setForm] = useState({
    recruiterId: "",
    days: "7",
    shareSkillClaims: true,
    shareEvidenceArtifacts: false,
    shareReviewResults: false,
  });

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  function change(event) {
    const { name, value, type, checked } = event.target;

    setForm((previous) => {
      const next = {
        ...previous,
        [name]: type === "checkbox" ? checked : value,
      };

      if (name === "shareSkillClaims" && !checked) {
        next.shareEvidenceArtifacts = false;
        next.shareReviewResults = false;
      }

      return next;
    });
  }

  async function submit(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (!form.shareSkillClaims) {
      setError("Enable skill-claim sharing before creating a grant.");
      return;
    }

    setBusy(true);

    try {
      const expiresAt = new Date(
        Date.now() + Number(form.days) * 24 * 60 * 60 * 1000
      ).toISOString();

      const saved = await createCareerProfileShare({
        recruiterId: Number(form.recruiterId),
        expiresAt,
        shareSkillClaims: form.shareSkillClaims,
        shareEvidenceArtifacts: form.shareEvidenceArtifacts,
        shareReviewResults: form.shareReviewResults,
      });

      setMessage(`Sharing grant #${saved.shareId} created.`);
      query.reload();
    } catch (requestError) {
      setError(careerErrorMessage(requestError));
    } finally {
      setBusy(false);
    }
  }

  async function revoke(shareId) {
    if (!window.confirm("Revoke this recruiter's access?")) {
      return;
    }

    setBusy(true);
    setError("");
    setMessage("");

    try {
      await revokeCareerProfileShare(shareId);
      setMessage("Sharing permission revoked.");
      query.reload();
    } catch (requestError) {
      setError(careerErrorMessage(requestError));
    } finally {
      setBusy(false);
    }
  }

  return (
    <CareerPage
      title="Recruiter sharing permissions"
      backTo="/student/career-evidence"
      description="Choose which parts of your current profile an approved recruiter can access."
    >
      <Notice type="warning">
        Sharing covers current permitted profile content, including
        later updates during the grant period. Revoking access blocks
        future requests but cannot erase material already copied.
      </Notice>

      <Notice type="error">{error}</Notice>
      <Notice type="success">{message}</Notice>

      <section className="career-card">
        <h2>Create or replace sharing permission</h2>

        <p className="career-muted">
          For this test version, enter the database ID of an
          approved recruiter. The backend checks approval.
        </p>

        <form className="career-form" onSubmit={submit}>
          <label>
            Approved recruiter ID
            <input
              name="recruiterId"
              type="number"
              min="1"
              step="1"
              required
              value={form.recruiterId}
              onChange={change}
            />
          </label>

          <label>
            Access duration in days
            <input
              name="days"
              type="number"
              min="1"
              max="30"
              step="1"
              required
              value={form.days}
              onChange={change}
            />
          </label>

          <label className="career-check">
            <input
              type="checkbox"
              name="shareSkillClaims"
              checked={form.shareSkillClaims}
              onChange={change}
            />
            Share skill claims
          </label>

          <label className="career-check">
            <input
              type="checkbox"
              name="shareEvidenceArtifacts"
              checked={form.shareEvidenceArtifacts}
              disabled={!form.shareSkillClaims}
              onChange={change}
            />
            Share supporting artifact references
          </label>

          <label className="career-check">
            <input
              type="checkbox"
              name="shareReviewResults"
              checked={form.shareReviewResults}
              disabled={!form.shareSkillClaims}
              onChange={change}
            />
            Share scoped review results
          </label>

          <button className="career-button" disabled={busy}>
            {busy ? "Saving…" : "Create sharing permission"}
          </button>
        </form>
      </section>

      <section className="career-card">
        <h2>Sharing history</h2>

        <QueryState loading={query.loading} error={query.error} />

        {query.data?.length === 0 && (
          <p>No profile-sharing grants yet.</p>
        )}

        {query.data?.map((share) => (
          <article className="career-skill" key={share.shareId}>
            <div className="career-row">
              <h3>
                {share.recruiterDisplayName ||
                  `Recruiter #${share.recruiterId}`}
              </h3>
              <StatusBadge value={share.status} />
            </div>

            <p>Grant #{share.shareId}</p>
            <p>Created: {formatDate(share.createdAt)}</p>
            <p>Expires: {formatDate(share.expiresAt)}</p>

            <ul>
              <li>
                Claims: {share.shareSkillClaims ? "Shared" : "Hidden"}
              </li>
              <li>
                Artifacts:{" "}
                {share.shareEvidenceArtifacts ? "Shared" : "Hidden"}
              </li>
              <li>
                Reviews:{" "}
                {share.shareReviewResults ? "Shared" : "Hidden"}
              </li>
            </ul>

            {share.active && (
              <button
                className="career-button career-button-danger"
                disabled={busy}
                onClick={() => revoke(share.shareId)}
              >
                Revoke access
              </button>
            )}
          </article>
        ))}
      </section>
    </CareerPage>
  );
}