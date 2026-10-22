import { useState } from "react";
import useCareerQuery from "../../hooks/useCareerQuery";
import {
  careerErrorMessage,
  submitEvidenceReview,
} from "../../api/careerEvidenceApi";
import EvidenceDetails from "../../components/career/EvidenceDetails";
import {
  CareerPage,
  Notice,
  QueryState,
} from "../../components/career/CareerUI";

const emptyReview = {
  outcome: "NEEDS_REVIEW",
  supportedScope: "",
  reviewMethod: "",
  reviewNotes: "",
  limitations: "",
  reviewedArtifactReference: "",
  supersedesReviewId: "",
};

export default function EvidenceReviewQueue() {
  const queue = useCareerQuery(
    "/interviewer/evidence-reviews/assigned"
  );

  const [artifactId, setArtifactId] = useState("");
  const [form, setForm] = useState(emptyReview);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const detail = useCareerQuery(
    artifactId
      ? `/interviewer/evidence-reviews/artifacts/${artifactId}`
      : null
  );

  function change(event) {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));
  }

  function chooseArtifact(value) {
    setArtifactId(value);
    setForm(emptyReview);
    setError("");
    setMessage("");
  }

  async function submit(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (!artifactId) {
      setError("Choose an artifact first.");
      return;
    }

    setSaving(true);

    try {
      await submitEvidenceReview(Number(artifactId), {
        ...form,
        supportedScope: form.supportedScope.trim() || null,
        supersedesReviewId:
          form.supersedesReviewId === ""
            ? null
            : Number(form.supersedesReviewId),
      });

      setMessage(
        "Scoped review recorded. This does not establish unrestricted skill mastery."
      );
      setForm(emptyReview);
      detail.reload();
      queue.reload();
    } catch (requestError) {
      setError(careerErrorMessage(requestError));
    } finally {
      setSaving(false);
    }
  }

  const needsScope = [
    "SUPPORTED",
    "PARTIALLY_SUPPORTED",
  ].includes(form.outcome);

  return (
    <CareerPage
      title="Assigned evidence reviews"
      backTo="/interviewer"
      description="Review only assigned students and record what the material actually establishes."
    >
      <Notice type="warning">
        This page does not approve your reviewer account.
        Approval and assignment are enforced by the backend.
        Do not mark an artifact supported merely because its URL opens.
      </Notice>

      <section className="career-card">
        <div className="career-row">
          <h2>Review queue</h2>
          <button
            className="career-button career-button-secondary"
            onClick={queue.reload}
            disabled={queue.loading}
          >
            Refresh queue
          </button>
        </div>

        <QueryState loading={queue.loading} error={queue.error} />

        {queue.data?.length === 0 && (
          <p>No current artifacts are available for your assignments.</p>
        )}

        <label className="career-field">
          Select artifact
          <select
            value={artifactId}
            onChange={(event) =>
              chooseArtifact(event.target.value)
            }
            disabled={saving}
          >
            <option value="">Choose an assigned artifact</option>
            {queue.data?.map((item) => (
              <option
                key={item.artifactId}
                value={item.artifactId}
              >
                Artifact #{item.artifactId} — {item.skillName}:
                {" "}{item.claimText?.slice(0, 75)}
              </option>
            ))}
          </select>
        </label>
      </section>

      <QueryState loading={detail.loading} error={detail.error} />

      {detail.data && (
        <EvidenceDetails evidence={detail.data} />
      )}

      {detail.data && !detail.loading && !detail.error && (
        <section className="career-card">
          <h2>Record a scoped decision</h2>

          <Notice type="error">{error}</Notice>
          <Notice type="success">{message}</Notice>

          <form className="career-form" onSubmit={submit}>
            <label>
              Outcome
              <select
                name="outcome"
                value={form.outcome}
                onChange={change}
              >
                <option value="NEEDS_REVIEW">
                  Additional review required
                </option>
                <option value="SUPPORTED">
                  Supported within a specific scope
                </option>
                <option value="PARTIALLY_SUPPORTED">
                  Partially supported
                </option>
                <option value="INSUFFICIENT">
                  Insufficient evidence
                </option>
                <option value="CONTRADICTED">
                  Evidence conflicts with claim
                </option>
              </select>
            </label>

            <label>
              Exact supported scope
              <textarea
                name="supportedScope"
                rows="3"
                required={needsScope}
                minLength={needsScope ? 10 : undefined}
                maxLength={5000}
                value={form.supportedScope}
                onChange={change}
              />
            </label>

            <label>
              Review method
              <textarea
                name="reviewMethod"
                rows="3"
                minLength={10}
                maxLength={5000}
                required
                value={form.reviewMethod}
                onChange={change}
              />
            </label>

            <label>
              Review notes
              <textarea
                name="reviewNotes"
                rows="4"
                minLength={10}
                maxLength={10000}
                required
                value={form.reviewNotes}
                onChange={change}
              />
            </label>

            <label>
              Limitations — what was not established?
              <textarea
                name="limitations"
                rows="3"
                minLength={10}
                maxLength={5000}
                required
                value={form.limitations}
                onChange={change}
              />
            </label>

            <label>
              Exact material inspected
              <textarea
                name="reviewedArtifactReference"
                rows="3"
                minLength={10}
                maxLength={5000}
                required
                placeholder="Record the commit, files, pages, or recording timestamps actually inspected."
                value={form.reviewedArtifactReference}
                onChange={change}
              />
            </label>

            <label>
              Previous review ID — only to revise your own latest review
              <input
                name="supersedesReviewId"
                type="number"
                min="1"
                step="1"
                value={form.supersedesReviewId}
                onChange={change}
              />
            </label>

            <p className="career-muted">
              Existing review IDs appear above. The backend rejects
              revisions of another reviewer's decision.
            </p>

            <button className="career-button" disabled={saving}>
              {saving ? "Recording…" : "Record review decision"}
            </button>
          </form>
        </section>
      )}
    </CareerPage>
  );
}