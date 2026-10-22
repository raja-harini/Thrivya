import { Link } from "react-router-dom";
import {
  formatDate,
  humanize,
  Notice,
  StatusBadge,
} from "./CareerUI";

export default function CareerProfileView({
  profile,
  evidenceBasePath,
}) {
  if (!profile) return null;

  const coverage =
    profile.reviewedEvidenceCoveragePercentage;

  const groups = [
    ["Evidence-backed strengths", profile.strengths],
    ["Developing and unreviewed claims", profile.developing],
    ["Role evidence gaps", profile.roleEvidenceGaps],
    ["Additional skills", profile.additionalSkills],
  ];

  return (
    <>
      <section className="career-card">
        <div className="career-row">
          <div>
            <h2>{profile.studentName || "Student profile"}</h2>
            <p>
              Target role:{" "}
              {profile.selectedRoleTitle || "Not selected"}
            </p>
            <p className="career-muted">
              Student ID: {profile.studentId}
            </p>
          </div>

          <div className="career-muted">
            Generated: {formatDate(profile.generatedAt)}
          </div>
        </div>

        <Notice>{profile.profileBasis}</Notice>

        {profile.sharing && (
          <p>
            Sharing permission:{" "}
            <StatusBadge value={profile.sharing.status} />
            {" · "}Expires{" "}
            {formatDate(profile.sharing.expiresAt)}
          </p>
        )}
      </section>

      <section className="career-stats">
        <Stat
          title="Role requirements"
          value={profile.totalRoleRequirements}
        />

        <Stat
          title="Requirements with scoped support"
          value={profile.requirementsWithSupportedEvidence}
        />

        <Stat
          title="Reviewed evidence coverage"
          value={coverage == null ? null : `${coverage}%`}
        />
      </section>

      <section className="career-card">
        <h2>What the numbers mean</h2>

        {coverage != null && (
          <progress
            className="career-progress"
            max="100"
            value={Math.min(100, Math.max(0, coverage))}
            aria-label="Reviewed evidence coverage"
          />
        )}

        <p>{profile.coverageExplanation}</p>

        <Notice type="warning">
          Evidence coverage is not a proficiency score,
          employability prediction, or guarantee of mastery.
        </Notice>
      </section>

      {groups.map(([title, skills]) => (
        <section className="career-card" key={title}>
          <h2>{title}</h2>

          {!skills?.length ? (
            <p className="career-muted">
              No items available in this category or sharing scope.
            </p>
          ) : (
            <div className="career-grid">
              {skills.map((skill, index) => (
                <article
                  className="career-skill"
                  key={`${skill.skillId}-${skill.claimId || index}`}
                >
                  <div className="career-row">
                    <h3>{skill.skillName}</h3>
                    <StatusBadge value={skill.evidenceStatus} />
                  </div>

                  {skill.claimText && (
                    <p className="career-claim">
                      {skill.claimText}
                    </p>
                  )}

                  {skill.claimId && (
                    <p className="career-muted">
                      Claim #{skill.claimId} ·{" "}
                      {humanize(skill.claimSourceType)}
                    </p>
                  )}

                  <p>{skill.evidenceStatusExplanation}</p>

                  {skill.supportedScope && (
                    <>
                      <h4>Supported scope</h4>
                      <p className="career-prewrap">
                        {skill.supportedScope}
                      </p>
                    </>
                  )}

                  {skill.limitations && (
                    <>
                      <h4>Review limitations</h4>
                      <p className="career-prewrap">
                        {skill.limitations}
                      </p>
                    </>
                  )}

                  <p className="career-muted">
                    Knowledge depth:{" "}
                    {humanize(skill.knowledgeDepthStatus)}
                  </p>

                  {skill.submittedArtifactCount != null && (
                    <p className="career-muted">
                      Submitted artifacts:{" "}
                      {skill.submittedArtifactCount}
                    </p>
                  )}

                  {skill.latestReviewedAt && (
                    <p className="career-muted">
                      Last review:{" "}
                      {formatDate(skill.latestReviewedAt)}
                    </p>
                  )}

                  <div className="career-actions">
                    {skill.artifactIds?.map((artifactId) => (
                      <Link
                        key={artifactId}
                        className="career-button career-button-secondary"
                        to={`${evidenceBasePath}/${artifactId}`}
                      >
                        View artifact #{artifactId}
                      </Link>
                    ))}
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      ))}

      <section className="career-card">
        <h2>Profile limitations</h2>
        <ul>
          {profile.limitations?.map((item, index) => (
            <li key={index}>{item}</li>
          ))}
        </ul>
      </section>
    </>
  );
}

function Stat({ title, value }) {
  return (
    <article className="career-card">
      <p className="career-muted">{title}</p>
      <div className="career-stat-value">
        {value == null ? "Not shared / unavailable" : value}
      </div>
    </article>
  );
}