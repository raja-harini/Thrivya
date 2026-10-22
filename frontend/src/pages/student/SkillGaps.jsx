import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Cell,
} from "recharts";

import {
  calculateSkillGaps,
  getSkillGaps,
} from "../../api/studentApi";

import RecommendationPanel from "../../components/RecommendationPanel";
import ProjectSuggestions from "../../components/ProjectSuggestions";

export default function SkillGaps() {
  const navigate = useNavigate();

  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const [expandedLearningSkillId, setExpandedLearningSkillId] =
    useState(null);

  const [expandedProjectGapId, setExpandedProjectGapId] =
    useState(null);

  async function loadExistingGaps() {
    try {
      setError("");

      const result = await getSkillGaps();

      setData(result);
    } catch (requestError) {
      const status = requestError.response?.status;

      if (status !== 404) {
        setError(
          requestError.response?.data?.message ||
            "Unable to load skill-gap analysis"
        );
      }
    }
  }

  useEffect(() => {
    loadExistingGaps();
  }, []);

  async function handleCalculate() {
    setError("");
    setLoading(true);

    try {
      const result = await calculateSkillGaps();

      setData(result);

      setExpandedLearningSkillId(null);
      setExpandedProjectGapId(null);
    } catch (requestError) {
      setError(
        requestError.response?.data?.message ||
          "Unable to calculate skill gaps"
      );
    } finally {
      setLoading(false);
    }
  }

  const gaps = data?.gaps || [];

  const chartData = gaps.map((gap) => ({
    name: gap.skillName,
    gap: Number(gap.gapPercentage || 0),
    priority: gap.priorityLevel,
  }));

  const totalRequiredSkills =
    data?.totalRequiredSkills ?? gaps.length;

  const missingSkills =
    data?.missingSkills ??
    gaps.filter(
      (gap) =>
        gap.status === "MISSING" ||
        Number(gap.gapPercentage) >= 75
    ).length;

  const developedSkills =
    data?.developedSkills ??
    gaps.filter(
      (gap) =>
        gap.status === "READY" ||
        Number(gap.gapPercentage) < 40
    ).length;

  const overallGapPercentage =
    Number(data?.overallGapPercentage ?? 0);

  const overallReadinessPercentage =
    Number(
      data?.overallReadinessPercentage ??
        100 - overallGapPercentage
    );

  return (
    <main style={styles.page}>
      <section style={styles.container}>
        <button
          onClick={() => navigate("/student")}
          style={styles.backButton}
        >
          Back to dashboard
        </button>

        <header style={styles.header}>
          <div>
            <p style={styles.brand}>Thrivya</p>

            <h1>Skill-gap analysis</h1>

            {data?.roleTitle && (
              <p style={styles.targetRole}>
                Target role:{" "}
                <strong>{data.roleTitle}</strong>
              </p>
            )}
          </div>

          <button
            onClick={handleCalculate}
            disabled={loading}
            style={styles.primaryButton}
          >
            {loading
              ? "Calculating..."
              : "Refresh analysis"}
          </button>
        </header>

        {error && (
          <p style={styles.error}>{error}</p>
        )}

        {!data && !error && (
          <section style={styles.emptyCard}>
            <h2>No skill-gap report yet</h2>

            <p>
              Upload a resume or update your skills, select a
              role, and calculate the analysis.
            </p>

            <button
              onClick={handleCalculate}
              disabled={loading}
              style={styles.primaryButton}
            >
              {loading
                ? "Calculating..."
                : "Calculate skill gaps"}
            </button>
          </section>
        )}

        {data && (
          <>
            <section style={styles.summaryGrid}>
              <article style={styles.summaryCard}>
                <p>Role readiness</p>

                <h2>
                  {Math.round(
                    overallReadinessPercentage
                  )}
                  %
                </h2>

                <span style={styles.summaryHint}>
                  Estimated current readiness
                </span>
              </article>

              <article style={styles.summaryCard}>
                <p>Overall skill gap</p>

                <h2>
                  {Math.round(
                    overallGapPercentage
                  )}
                  %
                </h2>

                <span style={styles.summaryHint}>
                  Average improvement required
                </span>
              </article>

              <article style={styles.summaryCard}>
                <p>Required skills</p>

                <h2>{totalRequiredSkills}</h2>

                <span style={styles.summaryHint}>
                  Skills mapped to this role
                </span>
              </article>

              <article style={styles.summaryCard}>
                <p>Missing skills</p>

                <h2>{missingSkills}</h2>

                <span style={styles.summaryHint}>
                  Skills needing attention
                </span>
              </article>
            </section>

            <section style={styles.chartCard}>
              <h2>Priority gap visualization</h2>

              <p style={styles.muted}>
                The largest gaps appear first. Red indicates
                urgent improvement areas, yellow indicates
                partial development, and green indicates
                stronger coverage.
              </p>

              {chartData.length > 0 ? (
                <ResponsiveContainer
                  width="100%"
                  height={380}
                >
                  <BarChart
                    data={chartData}
                    layout="vertical"
                    margin={{
                      top: 10,
                      right: 30,
                      left: 30,
                      bottom: 10,
                    }}
                  >
                    <CartesianGrid
                      strokeDasharray="3 3"
                    />

                    <XAxis
                      type="number"
                      domain={[0, 100]}
                      unit="%"
                    />

                    <YAxis
                      type="category"
                      dataKey="name"
                      width={150}
                    />

                    <Tooltip
                      formatter={(value) => [
                        `${value}%`,
                        "Skill gap",
                      ]}
                    />

                    <Bar
                      dataKey="gap"
                      name="Skill gap"
                      radius={[0, 8, 8, 0]}
                    >
                      {chartData.map((item, index) => (
                        <Cell
                          key={`${item.name}-${index}`}
                          fill={getBarColor(item.gap)}
                        />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <p style={styles.muted}>
                  No required skills are available for this
                  role yet.
                </p>
              )}
            </section>

            <section style={styles.tableCard}>
              <div style={styles.sectionHeader}>
                <div>
                  <h2>Ordered skill gaps</h2>

                  <p style={styles.muted}>
                    Skills are ordered from the highest gap to
                    the lowest gap.
                  </p>
                </div>

                <span style={styles.developedBadge}>
                  Developed: {developedSkills}
                </span>
              </div>

              {gaps.length === 0 && (
                <p style={styles.muted}>
                  No skill gaps found for the selected role.
                </p>
              )}

              {gaps.map((gap, index) => {
                const gapPercentage = Number(
                  gap.gapPercentage || 0
                );

                const skillKey =
                  gap.skillGapId ||
                  `${gap.skillName}-${index}`;

                const isLearningExpanded =
                  expandedLearningSkillId === skillKey;

                const isProjectExpanded =
                  expandedProjectGapId === skillKey;

                return (
                  <article
                    key={skillKey}
                    style={styles.gapRow}
                  >
                    <div style={styles.rank}>
                      {index + 1}
                    </div>

                    <div style={styles.gapMain}>
                      <div style={styles.gapTitleRow}>
                        <div>
                          <h3 style={styles.skillName}>
                            {gap.skillName}
                          </h3>

                          <p style={styles.muted}>
                            {gap.learningStage ||
                              "Learning stage not specified"}{" "}
                            · Role priority{" "}
                            {gap.priorityLevel ?? "—"}
                          </p>
                        </div>

                        <span
                          style={statusStyle(
                            gap.status,
                            gapPercentage
                          )}
                        >
                          {gap.status ||
                            getStatusFromGap(
                              gapPercentage
                            )}
                        </span>
                      </div>

                      <div
                        style={
                          styles.progressBackground
                        }
                      >
                        <div
                          style={{
                            ...styles.progressBar,
                            width: `${Math.min(
                              100,
                              Math.max(
                                0,
                                gapPercentage
                              )
                            )}%`,
                            background:
                              getBarColor(
                                gapPercentage
                              ),
                          }}
                        />
                      </div>

                      <div style={styles.gapMeta}>
                        <strong>
                          {Math.round(gapPercentage)}% gap
                        </strong>

                        <span>
                          Current level:{" "}
                          {formatLevel(
                            gap.currentLevel
                          )}
                        </span>
                      </div>

                      {gap.rationale && (
                        <p style={styles.rationale}>
                          {gap.rationale}
                        </p>
                      )}

                      {gap.evidenceText && (
                        <p style={styles.evidence}>
                          <strong>Evidence:</strong>{" "}
                          {gap.evidenceText}
                        </p>
                      )}

                      <div style={styles.actionButtons}>
                        <button
                          onClick={() =>
                            setExpandedLearningSkillId(
                              isLearningExpanded
                                ? null
                                : skillKey
                            )
                          }
                          style={styles.recommendButton}
                        >
                          {isLearningExpanded
                            ? "Hide learning options"
                            : "View learning options"}
                        </button>

                        <button
                          onClick={() =>
                            setExpandedProjectGapId(
                              isProjectExpanded
                                ? null
                                : skillKey
                            )
                          }
                          style={styles.projectButton}
                        >
                          {isProjectExpanded
                            ? "Hide project suggestions"
                            : "View project suggestions"}
                        </button>
                      </div>

                      {isLearningExpanded && (
                        <RecommendationPanel
                          skillName={gap.skillName}
                        />
                      )}

                      {isProjectExpanded && (
                        <ProjectSuggestions
                          skillGapId={gap.skillGapId}
                          skillName={gap.skillName}
                        />
                      )}
                    </div>
                  </article>
                );
              })}
            </section>
          </>
        )}
      </section>
    </main>
  );
}

function getBarColor(gap) {
  if (gap >= 75) {
    return "#dc2626";
  }

  if (gap >= 40) {
    return "#f59e0b";
  }

  return "#16a34a";
}

function getStatusFromGap(gap) {
  if (gap >= 75) {
    return "MISSING";
  }

  if (gap >= 40) {
    return "PARTIAL";
  }

  return "READY";
}

function statusStyle(status, gap) {
  const resolvedStatus =
    status || getStatusFromGap(gap);

  if (resolvedStatus === "READY") {
    return {
      ...styles.status,
      background: "#dcfce7",
      color: "#166534",
    };
  }

  if (
    resolvedStatus === "PARTIAL" ||
    resolvedStatus === "DEVELOPING"
  ) {
    return {
      ...styles.status,
      background: "#fef3c7",
      color: "#92400e",
    };
  }

  return {
    ...styles.status,
    background: "#fee2e2",
    color: "#991b1b",
  };
}

function formatLevel(level) {
  if (level === null || level === undefined) {
    return "Not detected";
  }

  return `${Math.round(Number(level) * 100)}%`;
}

const styles = {
  page: {
    minHeight: "100vh",
    padding: "32px",
    background: "#f4f7fb",
  },

  container: {
    maxWidth: "1100px",
    margin: "0 auto",
  },

  backButton: {
    padding: "9px 14px",
    border: "1px solid #cbd5e1",
    borderRadius: "8px",
    background: "white",
    color: "#334155",
    cursor: "pointer",
  },

  header: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "20px",
    margin: "28px 0",
  },

  brand: {
    margin: 0,
    color: "#2563eb",
    fontWeight: 700,
    letterSpacing: "0.2px",
  },

  targetRole: {
    color: "#475569",
  },

  primaryButton: {
    padding: "12px 18px",
    border: "none",
    borderRadius: "8px",
    background: "#2563eb",
    color: "white",
    cursor: "pointer",
    fontWeight: 600,
  },

  actionButtons: {
    display: "flex",
    flexWrap: "wrap",
    gap: "10px",
    marginTop: "12px",
  },

  recommendButton: {
    padding: "9px 14px",
    border: "none",
    borderRadius: "8px",
    background: "#0f766e",
    color: "white",
    cursor: "pointer",
  },

  projectButton: {
    padding: "9px 14px",
    border: "none",
    borderRadius: "8px",
    background: "#7c3aed",
    color: "white",
    cursor: "pointer",
  },

  summaryGrid: {
    display: "grid",
    gridTemplateColumns:
      "repeat(auto-fit, minmax(190px, 1fr))",
    gap: "16px",
  },

  summaryCard: {
    padding: "22px",
    borderRadius: "14px",
    background: "white",
    boxShadow:
      "0 5px 16px rgba(15, 23, 42, 0.08)",
  },

  summaryHint: {
    color: "#64748b",
    fontSize: "13px",
  },

  chartCard: {
    marginTop: "22px",
    padding: "24px",
    borderRadius: "14px",
    background: "white",
    boxShadow:
      "0 5px 16px rgba(15, 23, 42, 0.06)",
  },

  tableCard: {
    marginTop: "22px",
    padding: "24px",
    borderRadius: "14px",
    background: "white",
    boxShadow:
      "0 5px 16px rgba(15, 23, 42, 0.06)",
  },

  sectionHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "16px",
    marginBottom: "12px",
  },

  developedBadge: {
    padding: "8px 12px",
    borderRadius: "16px",
    background: "#dcfce7",
    color: "#166534",
    fontSize: "13px",
    fontWeight: 700,
  },

  gapRow: {
    display: "flex",
    gap: "16px",
    padding: "18px 0",
    borderBottom: "1px solid #e2e8f0",
  },

  rank: {
    flexShrink: 0,
    width: "34px",
    height: "34px",
    display: "grid",
    placeItems: "center",
    borderRadius: "50%",
    background: "#dbeafe",
    color: "#1d4ed8",
    fontWeight: 700,
  },

  gapMain: {
    flex: 1,
    minWidth: 0,
  },

  gapTitleRow: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-start",
    gap: "12px",
  },

  skillName: {
    margin: 0,
  },

  status: {
    flexShrink: 0,
    padding: "5px 9px",
    borderRadius: "14px",
    fontSize: "12px",
    fontWeight: 700,
  },

  progressBackground: {
    width: "100%",
    height: "10px",
    marginTop: "12px",
    borderRadius: "8px",
    background: "#e2e8f0",
    overflow: "hidden",
  },

  progressBar: {
    height: "100%",
    borderRadius: "8px",
    transition: "width 0.3s ease",
  },

  gapMeta: {
    display: "flex",
    justifyContent: "space-between",
    gap: "12px",
    marginTop: "8px",
    color: "#475569",
    fontSize: "13px",
  },

  rationale: {
    marginBottom: 0,
    color: "#334155",
    lineHeight: 1.5,
  },

  evidence: {
    padding: "10px",
    borderRadius: "8px",
    background: "#f8fafc",
    color: "#475569",
    fontSize: "13px",
    lineHeight: 1.5,
  },

  muted: {
    color: "#64748b",
  },

  error: {
    padding: "10px",
    borderRadius: "8px",
    background: "#fee2e2",
    color: "#991b1b",
  },

  emptyCard: {
    padding: "30px",
    borderRadius: "14px",
    background: "white",
    boxShadow:
      "0 5px 16px rgba(15, 23, 42, 0.06)",
  },
};