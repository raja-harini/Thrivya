import { useState } from "react";
import { Link } from "react-router-dom";
import useCareerQuery from "../../hooks/useCareerQuery";
import {
  careerErrorMessage,
  createSkillClaim,
  submitSkillEvidence,
} from "../../api/careerEvidenceApi";
import {
  CareerPage,
  formatDate,
  humanize,
  Notice,
  QueryState,
} from "../../components/career/CareerUI";

const initialClaim = {
  skillId: "",
  claimText: "",
  sourceType: "MANUAL_INPUT",
  sourceRecordId: "",
  supersedesClaimId: "",
};

const initialArtifact = {
  artifactType: "REPOSITORY_REFERENCE",
  externalUrl: "",
  artifactDescription: "",
  claimedContribution: "",
  artifactLocator: "",
  commitSha: "",
  supersedesArtifactId: "",
};

function optionalId(value) {
  return value === "" ? null : Number(value);
}

export default function SkillEvidenceManager() {
  const claimsQuery = useCareerQuery(
    "/student/career-profile/claims"
  );

  const [claim, setClaim] = useState(initialClaim);
  const [artifact, setArtifact] = useState(initialArtifact);
  const [claimId, setClaimId] = useState("");
  const [busy, setBusy] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const artifactsQuery = useCareerQuery(
    claimId
      ? `/student/career-profile/claims/${claimId}/evidence`
      : null
  );

  function changeClaim(event) {
    const { name, value } = event.target;

    setClaim((previous) => ({
      ...previous,
      [name]: value,
      ...(name === "sourceType" && value === "MANUAL_INPUT"
        ? { sourceRecordId: "" }
        : {}),
    }));
  }

  function changeArtifact(event) {
    const { name, value } = event.target;

    setArtifact((previous) => ({
      ...previous,
      [name]: value,
    }));
  }

  async function handleClaimSubmit(event) {
    event.preventDefault();
    setError("");
    setMessage("");
    setBusy("claim");

    try {
      const saved = await createSkillClaim({
        skillId: Number(claim.skillId),
        claimText: claim.claimText.trim(),
        sourceType: claim.sourceType,
        sourceRecordId:
          claim.sourceType === "MANUAL_INPUT"
            ? null
            : optionalId(claim.sourceRecordId),
        supersedesClaimId: optionalId(claim.supersedesClaimId),
      });

      setClaimId(String(saved.claimId));
      setClaim(initialClaim);
      setMessage(
        `Claim #${saved.claimId} saved as self-reported. Submit evidence below.`
      );
      claimsQuery.reload();
    } catch (requestError) {
      setError(careerErrorMessage(requestError));
    } finally {
      setBusy("");
    }
  }

  async function handleEvidenceSubmit(event) {
    event.preventDefault();
    setError("");
    setMessage("");

    if (!claimId) {
      setError("Choose a claim first.");
      return;
    }

    setBusy("artifact");

    try {
      const saved = await submitSkillEvidence(
        Number(claimId),
        {
          ...artifact,
          commitSha: artifact.commitSha.trim() || null,
          artifactLocator: artifact.artifactLocator.trim() || null,
          supersedesArtifactId: optionalId(
            artifact.supersedesArtifactId
          ),
        }
      );

      setArtifact(initialArtifact);
      setMessage(
        `Artifact #${saved.artifactId} submitted. It is not automatically verified.`
      );
      artifactsQuery.reload();
    } catch (requestError) {
      setError(careerErrorMessage(requestError));
    } finally {
      setBusy("");
    }
  }

  return (
    <CareerPage
      title="Claims and supporting evidence"
      backTo="/student/career-evidence"
      description="Write a specific claim, then attach material that supports your actual contribution."
    >
      <Notice type="warning">
        Resume mentions and learning updates are self-reported.
        Submission does not automatically establish authorship or competence.
      </Notice>

      <Notice type="error">{error}</Notice>
      <Notice type="success">{message}</Notice>

      <section className="career-card">
        <h2>1. Create a skill claim</h2>

        <Notice>
          This test screen requires a real skill database ID.
          Your current backend does not provide a skill-catalog endpoint.
          Obtain the ID from your existing skill response if available,
          or from the skills table. Do not use a skill name as the ID.
        </Notice>

        <form className="career-form" onSubmit={handleClaimSubmit}>
          <label>
            Skill ID
            <input
              name="skillId"
              type="number"
              min="1"
              step="1"
              required
              value={claim.skillId}
              onChange={changeClaim}
            />
          </label>

          <label>
            What exactly can you demonstrate?
            <textarea
              name="claimText"
              rows="4"
              minLength={10}
              maxLength={3000}
              required
              placeholder="Implemented CRUD endpoints and request validation in a Spring Boot task API."
              value={claim.claimText}
              onChange={changeClaim}
            />
          </label>

          <label>
            Claim source
            <select
              name="sourceType"
              value={claim.sourceType}
              onChange={changeClaim}
            >
              <option value="MANUAL_INPUT">Direct statement</option>
              <option value="RESUME_TEXT">Resume version</option>
              <option value="MANUAL_PROGRESS">Learning update</option>
              <option value="STUDENT_PROJECT">Student project</option>
            </select>
          </label>

          {claim.sourceType !== "MANUAL_INPUT" && (
            <label>
              Source record ID
              <input
                name="sourceRecordId"
                type="number"
                min="1"
                step="1"
                required
                value={claim.sourceRecordId}
                onChange={changeClaim}
              />
            </label>
          )}

          <label>
            Previous claim ID — only when revising a claim
            <input
              name="supersedesClaimId"
              type="number"
              min="1"
              step="1"
              value={claim.supersedesClaimId}
              onChange={changeClaim}
            />
          </label>

          <button className="career-button" disabled={Boolean(busy)}>
            {busy === "claim" ? "Saving…" : "Save claim"}
          </button>
        </form>
      </section>

      <section className="career-card">
        <h2>2. Submit evidence for a claim</h2>

        <QueryState
          loading={claimsQuery.loading}
          error={claimsQuery.error}
        />

        <form
          className="career-form"
          onSubmit={handleEvidenceSubmit}
        >
          <label>
            Choose your claim
            <select
              value={claimId}
              onChange={(event) => {
                setClaimId(event.target.value);
                setError("");
                setMessage("");
              }}
              required
            >
              <option value="">Select a claim</option>
              {claimsQuery.data?.map((item) => (
                <option key={item.claimId} value={item.claimId}>
                  #{item.claimId} — {item.skillName}:{" "}
                  {item.claimText?.slice(0, 85)}
                </option>
              ))}
            </select>
          </label>

          <label>
            Evidence type
            <select
              name="artifactType"
              value={artifact.artifactType}
              onChange={changeArtifact}
            >
              <option value="REPOSITORY_REFERENCE">
                Repository reference
              </option>
              <option value="SOURCE_FILE">Source file</option>
              <option value="PROJECT_REPORT">Project report</option>
              <option value="DEMO_RECORDING">Demo recording</option>
              <option value="CERTIFICATE">Certificate</option>
              <option value="ASSESSMENT_RESULT">
                Assessment result
              </option>
            </select>
          </label>

          <label>
            HTTPS evidence URL
            <input
              name="externalUrl"
              type="url"
              maxLength={2048}
              required
              value={artifact.externalUrl}
              onChange={changeArtifact}
            />
          </label>

          <label>
            What does this artifact contain?
            <textarea
              name="artifactDescription"
              rows="3"
              minLength={10}
              maxLength={5000}
              required
              value={artifact.artifactDescription}
              onChange={changeArtifact}
            />
          </label>

          <label>
            Your own contribution
            <textarea
              name="claimedContribution"
              rows="3"
              minLength={10}
              maxLength={5000}
              required
              value={artifact.claimedContribution}
              onChange={changeArtifact}
            />
          </label>

          <label>
            Relevant files, pages, or timestamps
            <input
              name="artifactLocator"
              maxLength={3000}
              required={
                artifact.artifactType === "REPOSITORY_REFERENCE"
              }
              value={artifact.artifactLocator}
              onChange={changeArtifact}
            />
          </label>

          <label>
            Full commit SHA — required for repositories
            <input
              name="commitSha"
              pattern="([a-fA-F0-9]{40}|[a-fA-F0-9]{64})"
              required={
                artifact.artifactType === "REPOSITORY_REFERENCE"
              }
              value={artifact.commitSha}
              onChange={changeArtifact}
            />
          </label>

          <label>
            Previous artifact ID — only when revising
            <input
              name="supersedesArtifactId"
              type="number"
              min="1"
              step="1"
              value={artifact.supersedesArtifactId}
              onChange={changeArtifact}
            />
          </label>

          <button
            className="career-button"
            disabled={Boolean(busy) || !claimId}
          >
            {busy === "artifact" ? "Submitting…" : "Submit evidence"}
          </button>
        </form>
      </section>

      {claimId && (
        <section className="career-card">
          <h2>Evidence for claim #{claimId}</h2>

          <QueryState
            loading={artifactsQuery.loading}
            error={artifactsQuery.error}
          />

          {artifactsQuery.data?.length === 0 && (
            <p>No evidence submitted for this claim.</p>
          )}

          {artifactsQuery.data?.map((item) => (
            <article className="career-skill" key={item.artifactId}>
              <h3>Artifact #{item.artifactId}</h3>
              <p>{item.artifactDescription}</p>
              <p className="career-muted">
                {humanize(item.artifactType)} ·{" "}
                {formatDate(item.submittedAt)}
              </p>
              <Link
                className="career-link"
                to={`/student/career-evidence/artifacts/${item.artifactId}`}
              >
                Inspect evidence and reviews
              </Link>
            </article>
          ))}
        </section>
      )}
    </CareerPage>
  );
}