import {
  formatDate,
  humanize,
  Notice,
  SafeExternalLink,
  StatusBadge,
} from "./CareerUI";

export default function EvidenceDetails({ evidence }) {
  if (!evidence) return null;

  return (
    <section className="career-card">
      <div className="career-row">
        <div>
          <h2>{evidence.skillName}</h2>
          <p className="career-muted">
            Claim #{evidence.claimId} · Artifact #
            {evidence.artifactId}
          </p>
        </div>

        <StatusBadge value={evidence.evidenceStatus} />
      </div>

      <h3>Original student claim</h3>
      <p className="career-claim">{evidence.claimText}</p>

      <dl className="career-details">
        <dt>Claim source</dt>
        <dd>{humanize(evidence.claimSourceType)}</dd>

        <dt>Artifact type</dt>
        <dd>{humanize(evidence.artifactType)}</dd>

        <dt>Description</dt>
        <dd>{evidence.artifactDescription}</dd>

        <dt>Claimed contribution</dt>
        <dd>{evidence.claimedContribution}</dd>

        <dt>Relevant files or location</dt>
        <dd>{evidence.artifactLocator || "Not supplied"}</dd>

        <dt>Commit reference</dt>
        <dd className="career-break">
          {evidence.commitSha || "Not supplied"}
        </dd>

        <dt>Submitted</dt>
        <dd>{formatDate(evidence.submittedAt)}</dd>

        <dt>Captured content hash</dt>
        <dd className="career-break">
          {evidence.contentSha256 ||
            "No captured-content hash recorded"}
        </dd>
      </dl>

      {evidence.externalUrl && (
        <SafeExternalLink url={evidence.externalUrl}>
          Open student-provided external reference ↗
        </SafeExternalLink>
      )}

      <Notice type="warning">
        {evidence.evidenceStatusExplanation}
      </Notice>

      <p className="career-muted">
        {evidence.hashExplanation}
      </p>

      <h3>Review decisions</h3>

      {!evidence.reviewResultsVisible ? (
        <Notice>
          Review details are not included in this sharing permission.
        </Notice>
      ) : !evidence.reviews?.length ? (
        <Notice>
          No current approved review is available. This is
          submitted evidence—not independently supported competence.
        </Notice>
      ) : (
        evidence.reviews.map((review) => (
          <article className="career-skill" key={review.reviewId}>
            <div className="career-row">
              <h4>Review #{review.reviewId}</h4>
              <StatusBadge value={review.outcome} />
            </div>

            <p>
              Reviewer: {review.reviewerDisplayName}{" "}
              <span className="career-muted">
                (User #{review.reviewerId})
              </span>
            </p>

            <p>Reviewed: {formatDate(review.reviewedAt)}</p>

            <h4>Supported scope</h4>
            <p className="career-prewrap">
              {review.supportedScope || "No supported scope recorded"}
            </p>

            <h4>Method</h4>
            <p className="career-prewrap">{review.reviewMethod}</p>

            <h4>Notes</h4>
            <p className="career-prewrap">{review.reviewNotes}</p>

            <h4>Limitations</h4>
            <p className="career-prewrap">{review.limitations}</p>

            <h4>Material inspected</h4>
            <p className="career-prewrap career-break">
              {review.reviewedArtifactReference}
            </p>

            {review.supersedesReviewId && (
              <p>
                Replaces review #{review.supersedesReviewId}
              </p>
            )}
          </article>
        ))
      )}

      <ul className="career-muted">
        {evidence.limitations?.map((item, index) => (
          <li key={index}>{item}</li>
        ))}
      </ul>
    </section>
  );
}