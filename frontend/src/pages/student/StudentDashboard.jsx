import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import DashboardCard from "../../components/DashboardCard";
import { getStudentDashboard } from "../../api/dashboardApi";
import { getStudentProfile } from "../../api/studentApi";

function getStoredUser() {
  try {
    const storedUser = localStorage.getItem("thrivyaUser");
    return storedUser ? JSON.parse(storedUser) : null;
  } catch {
    return null;
  }
}

export default function StudentDashboard() {
  const navigate = useNavigate();

  const [user] = useState(getStoredUser);
  const [dashboard, setDashboard] = useState(null);
  const [profile, setProfile] = useState(null);

  const [error, setError] = useState("");
  const [profileError, setProfileError] = useState("");
  const [profileLoading, setProfileLoading] = useState(true);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let isMounted = true;

    function handleUnauthorized(requestError) {
      if (requestError.response?.status !== 401) {
        return false;
      }

      localStorage.removeItem("thrivyaToken");
      localStorage.removeItem("thrivyaUser");

      navigate("/login", { replace: true });
      return true;
    }

    async function loadDashboardData() {
      setError("");
      setProfileError("");
      setProfileLoading(true);

      try {
        const dashboardData = await getStudentDashboard();

        if (!isMounted) {
          return;
        }

        setDashboard(dashboardData);
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        if (handleUnauthorized(requestError)) {
          return;
        }

        setError(
          requestError.response?.data?.message ||
            "Unable to load student dashboard."
        );

        setProfileLoading(false);
        return;
      }

      try {
        const profileData = await getStudentProfile();

        if (isMounted) {
          setProfile(profileData);
        }
      } catch (requestError) {
        if (!isMounted) {
          return;
        }

        if (handleUnauthorized(requestError)) {
          return;
        }

        setProfile(null);

        // A missing profile is expected for a new student.
        if (requestError.response?.status !== 404) {
          setProfileError(
            requestError.response?.data?.message ||
              "Unable to load your career profile. Please try again."
          );
        }
      } finally {
        if (isMounted) {
          setProfileLoading(false);
        }
      }
    }

    loadDashboardData();

    return () => {
      isMounted = false;
    };
  }, [navigate, reloadKey]);

  function handleLogout() {
    localStorage.removeItem("thrivyaToken");
    localStorage.removeItem("thrivyaUser");

    navigate("/login", { replace: true });
  }

  function handleRetry() {
    setReloadKey((previous) => previous + 1);
  }

  const studentName =
    user?.username || user?.userName || user?.name || "Student";

  const hasUploadedResume = Boolean(
    profile?.resumeId || profile?.fileName
  );

  const hasSelectedRole = Boolean(
    profile?.selectedRoleId || profile?.selectedRoleTitle
  );

  const hasSkills =
    Array.isArray(profile?.skills) && profile.skills.length > 0;

  const profileReady = !profileLoading && !profileError;

  const canGenerateATS =
    profileReady && hasUploadedResume && hasSelectedRole;

  const canViewSkillGaps =
    profileReady && hasSelectedRole && hasSkills;

  const canGenerateResumeContent =
    profileReady &&
    hasUploadedResume &&
    hasSelectedRole &&
    hasSkills;

  const profileUnavailableMessage = profileLoading
    ? "Checking your career profile..."
    : profileError
    ? "Your profile could not be checked. Retry loading it first."
    : "";

  const atsStatusMessage =
    profileUnavailableMessage ||
    (!hasUploadedResume && !hasSelectedRole
      ? "Upload a resume and select a target role before generating ATS."
      : !hasUploadedResume
      ? "Upload a resume before generating ATS."
      : !hasSelectedRole
      ? "Select a target role before generating ATS."
      : "Your ATS report will use your latest resume and selected role.");

  const skillGapStatusMessage =
    profileUnavailableMessage ||
    (!hasSkills
      ? "Add skills or upload a readable resume before viewing skill gaps."
      : !hasSelectedRole
      ? "Select a target role before viewing skill gaps."
      : "View ordered skill gaps and learning resources for your selected role.");

  const resumeContentStatusMessage =
    profileUnavailableMessage ||
    (!hasUploadedResume
      ? "Upload a resume before generating resume content."
      : !hasSkills
      ? "Add skills or upload a readable resume before generating content."
      : !hasSelectedRole
      ? "Select a target role before generating resume content."
      : "Generate copy-ready content from your resume and recorded learning evidence. Review every claim before using it.");

  const featureCards = [
    {
      title: "Explainable ATS analysis",
      description:
        "Analyze your latest uploaded resume against your selected career role with factor-wise explanations.",
      buttonLabel: profileLoading
        ? "Checking profile..."
        : "View ATS report",
      path: "/student/ats",
      enabled: canGenerateATS,
      statusMessage: atsStatusMessage,
      theme: "indigo",
    },
    {
      title: "Ordered skill-gap analysis",
      description:
        "Compare your current skill profile with your selected role and identify gaps to address.",
      buttonLabel: "View skill gaps",
      path: "/student/skill-gaps",
      enabled: canViewSkillGaps,
      statusMessage: skillGapStatusMessage,
      theme: "green",
    },
    {
      title: "Learning progress",
      description:
        "Record what you have learned, practiced, or built, along with supporting evidence.",
      buttonLabel: "Update learning progress",
      path: "/student/progress",
      enabled: true,
      theme: "purple",
    },
    {
      title: "Project-based learning",
      description:
        "View your selected projects, track your work, and submit evidence of the skills you applied.",
      buttonLabel: "My project-based learning",
      path: "/student/my-projects",
      enabled: true,
      theme: "teal",
    },
    {
      title: "Evidence-aware resume content",
      description:
        "Create resume wording from your current profile, selected role, and recorded learning or project evidence.",
      buttonLabel: profileLoading
        ? "Checking profile..."
        : "Generate resume content",
      path: "/student/resume-content",
      enabled: canGenerateResumeContent,
      statusMessage: resumeContentStatusMessage,
      theme: "amber",
    },
    {
      title: "Resume version history",
      description:
        "View your active resume and previous uploads to review changes over time.",
      buttonLabel: "View resume history",
      path: "/student/resume-versions",
      enabled: true,
      theme: "sky",
    },
  ];

  const evidenceActions = [
    {
      title: "My career evidence profile",
      description:
        "View your skill claims, supporting evidence, scoped review results, and role requirements without reviewed evidence.",
      buttonLabel: "My career evidence profile",
      path: "/student/career-evidence",
    },
    {
      title: "Skill claims and evidence",
      description:
        "Submit specific claims about your skills and attach references that show your actual contribution.",
      buttonLabel: "Manage skill claims and evidence",
      path: "/student/career-evidence/claims",
    },
    {
      title: "Recruiter sharing",
      description:
        "Choose which profile details an approved recruiter can access, set an expiry, and revoke permission when needed.",
      buttonLabel: "Manage recruiter sharing",
      path: "/student/career-evidence/sharing",
    },
  ];

  if (error) {
    return (
      <main style={styles.page}>
        <section style={styles.stateCard}>
          <header style={styles.header}>
            <div>
              <p style={styles.brand}>Thrivya</p>
              <h1>Student Dashboard</h1>
            </div>

            <button
              type="button"
              style={styles.logoutButton}
              onClick={handleLogout}
            >
              Logout
            </button>
          </header>

          <p role="alert" style={styles.error}>
            {error}
          </p>

          <button
            type="button"
            onClick={handleRetry}
            style={styles.primaryButton}
          >
            Try again
          </button>
        </section>
      </main>
    );
  }

  if (!dashboard) {
    return (
      <main style={styles.page}>
        <section style={styles.stateCard} aria-busy="true">
          <p style={styles.brand}>Thrivya</p>
          <h1>Student Dashboard</h1>
          <p role="status">Loading your career dashboard...</p>
        </section>
      </main>
    );
  }

  return (
    <DashboardCard
      title={dashboard.title || "Student Dashboard"}
      role={dashboard.role || "STUDENT"}
      email={dashboard.email || user?.email || "Student"}
      message={
        dashboard.message ||
        "Your Thrivya career journey starts here."
      }
    >
      <p style={styles.welcomeText}>
        Welcome, <strong>{studentName}</strong>
      </p>

      {profileError && (
        <section style={styles.profileErrorBox}>
          <p role="alert" style={styles.profileErrorText}>
            {profileError}
          </p>

          <button
            type="button"
            onClick={handleRetry}
            style={styles.secondaryButton}
          >
            Retry profile loading
          </button>
        </section>
      )}

      <section style={styles.infoBox}>
        <h2 style={styles.sectionTitle}>
          Build your career profile
        </h2>

        <p style={styles.description}>
          Upload or update your resume, enter skills manually,
          and choose a desired role or receive role suggestions.
        </p>

        <div style={styles.profileActions}>
          <button
            type="button"
            onClick={() => navigate("/student/setup")}
            style={styles.primaryButton}
          >
            Set up my career profile
          </button>

          <button
            type="button"
            onClick={() => navigate("/student/skills")}
            style={styles.secondaryButton}
          >
            Show my skills
          </button>
        </div>
      </section>

      <section style={styles.evidenceSection}>
        <h2 style={styles.sectionTitle}>
          Career evidence and recruiter visibility
        </h2>

        <p style={styles.description}>
          Keep your claims, supporting material, review decisions,
          and sharing permissions in one place.
        </p>

        <p style={styles.evidenceNotice}>
          Submitting a claim or evidence does not automatically
          verify a skill. Review results apply only to the scope
          actually checked.
        </p>

        <div style={styles.evidenceGrid}>
          {evidenceActions.map((action) => (
            <article
              key={action.path}
              style={styles.evidenceCard}
            >
              <h3 style={styles.evidenceCardTitle}>
                {action.title}
              </h3>

              <p style={styles.description}>
                {action.description}
              </p>

              <button
                type="button"
                onClick={() => navigate(action.path)}
                style={styles.evidenceButton}
              >
                {action.buttonLabel}
              </button>
            </article>
          ))}
        </div>
      </section>

      {featureCards.map((feature) => (
        <FeatureCard
          key={feature.path}
          {...feature}
          onNavigate={() => navigate(feature.path)}
        />
      ))}

      <section style={styles.statusBox}>
        <h2 style={styles.sectionTitle}>Profile status</h2>

        {profileLoading ? (
          <p role="status">Checking your profile status...</p>
        ) : profileError ? (
          <p style={styles.warningMessage}>
            Profile status is temporarily unavailable.
          </p>
        ) : (
          <>
            <ProfileStatusRow
              label="Resume uploaded"
              completed={hasUploadedResume}
            />

            <ProfileStatusRow
              label="Skills available"
              completed={hasSkills}
            />

            <ProfileStatusRow
              label="Target role selected"
              completed={hasSelectedRole}
            />

            {profile?.selectedRoleTitle && (
              <p style={styles.selectedRole}>
                Selected role:{" "}
                <strong>{profile.selectedRoleTitle}</strong>
              </p>
            )}

            <p style={styles.statusNote}>
              These indicators show profile setup status—not
              independent verification or knowledge depth.
            </p>
          </>
        )}
      </section>
    </DashboardCard>
  );
}

function FeatureCard({
  title,
  description,
  buttonLabel,
  enabled,
  statusMessage,
  theme,
  onNavigate,
}) {
  const colors = themes[theme] || themes.indigo;

  return (
    <section
      style={{
        ...styles.featureBox,
        background: colors.background,
        borderColor: colors.border,
      }}
    >
      <div style={styles.featureContent}>
        <h2
          style={{
            ...styles.sectionTitle,
            color: colors.text,
          }}
        >
          {title}
        </h2>

        <p style={styles.description}>{description}</p>

        {statusMessage && (
          <p
            style={
              enabled
                ? styles.readyMessage
                : styles.warningMessage
            }
          >
            {statusMessage}
          </p>
        )}
      </div>

      <button
        type="button"
        onClick={onNavigate}
        disabled={!enabled}
        title={!enabled ? statusMessage : title}
        style={{
          ...styles.featureButton,
          background: colors.button,
          ...(!enabled ? styles.disabledButton : {}),
        }}
      >
        {buttonLabel}
      </button>
    </section>
  );
}

function ProfileStatusRow({ label, completed }) {
  return (
    <div style={styles.statusRow}>
      <span>{label}</span>

      <span
        style={
          completed
            ? styles.completedStatus
            : styles.pendingStatus
        }
      >
        {completed ? "Completed" : "Pending"}
      </span>
    </div>
  );
}

const themes = {
  indigo: {
    background: "#eef2ff",
    border: "#c7d2fe",
    text: "#3730a3",
    button: "#4f46e5",
  },
  green: {
    background: "#ecfdf5",
    border: "#a7f3d0",
    text: "#047857",
    button: "#059669",
  },
  purple: {
    background: "#f5f3ff",
    border: "#ddd6fe",
    text: "#6d28d9",
    button: "#7c3aed",
  },
  teal: {
    background: "#f0fdfa",
    border: "#99f6e4",
    text: "#0f766e",
    button: "#0f766e",
  },
  amber: {
    background: "#fffbeb",
    border: "#fde68a",
    text: "#a16207",
    button: "#a16207",
  },
  sky: {
    background: "#f0f9ff",
    border: "#bae6fd",
    text: "#0369a1",
    button: "#0284c7",
  },
};

const styles = {
  page: {
    minHeight: "100vh",
    padding: "32px 20px",
    background: "#f4f7fb",
  },

  stateCard: {
    width: "100%",
    maxWidth: "850px",
    margin: "0 auto",
    padding: "32px",
    borderRadius: "18px",
    background: "#ffffff",
    boxShadow: "0 10px 30px rgba(15, 23, 42, 0.12)",
  },

  header: {
    display: "flex",
    flexWrap: "wrap",
    alignItems: "flex-start",
    justifyContent: "space-between",
    gap: "20px",
    marginBottom: "20px",
  },

  brand: {
    margin: "0 0 8px",
    color: "#2563eb",
    fontSize: "18px",
    fontWeight: 700,
  },

  welcomeText: {
    fontSize: "17px",
    color: "#334155",
  },

  logoutButton: {
    padding: "10px 16px",
    border: "none",
    borderRadius: "8px",
    background: "#dc2626",
    color: "#ffffff",
    fontSize: "14px",
    fontWeight: 600,
    cursor: "pointer",
  },

  infoBox: {
    marginTop: "28px",
    padding: "24px",
    border: "1px solid #dbeafe",
    borderRadius: "14px",
    background: "#eff6ff",
  },

  sectionTitle: {
    margin: "0 0 8px",
    fontSize: "21px",
  },

  description: {
    margin: "0 0 12px",
    color: "#475569",
    lineHeight: 1.6,
  },

  profileActions: {
    display: "flex",
    flexDirection: "column",
    alignItems: "flex-start",
    gap: "12px",
    marginTop: "16px",
  },

  primaryButton: {
    padding: "12px 18px",
    border: "none",
    borderRadius: "9px",
    background: "#2563eb",
    color: "#ffffff",
    fontSize: "15px",
    fontWeight: 600,
    cursor: "pointer",
  },

  secondaryButton: {
    padding: "12px 18px",
    border: "1px solid #2563eb",
    borderRadius: "9px",
    background: "#ffffff",
    color: "#2563eb",
    fontSize: "15px",
    fontWeight: 600,
    cursor: "pointer",
  },

  evidenceSection: {
    marginTop: "24px",
    padding: "24px",
    border: "1px solid #cbd5e1",
    borderRadius: "14px",
    background: "#ffffff",
  },

  evidenceNotice: {
    margin: "16px 0",
    padding: "12px 14px",
    border: "1px solid #fde68a",
    borderRadius: "9px",
    background: "#fffbeb",
    color: "#854d0e",
    fontSize: "14px",
    lineHeight: 1.6,
  },

  evidenceGrid: {
    display: "grid",
    gridTemplateColumns:
      "repeat(auto-fit, minmax(min(100%, 230px), 1fr))",
    gap: "16px",
    marginTop: "18px",
  },

  evidenceCard: {
    display: "flex",
    flexDirection: "column",
    alignItems: "flex-start",
    padding: "20px",
    border: "1px solid #e2e8f0",
    borderRadius: "12px",
    background: "#f8fafc",
    minWidth: 0,
  },

  evidenceCardTitle: {
    margin: "0 0 10px",
    fontSize: "18px",
    color: "#1e3a5f",
  },

  evidenceButton: {
    marginTop: "auto",
    maxWidth: "100%",
    padding: "11px 15px",
    border: "none",
    borderRadius: "8px",
    background: "#1e40af",
    color: "#ffffff",
    fontSize: "14px",
    fontWeight: 600,
    lineHeight: 1.5,
    textAlign: "left",
    cursor: "pointer",
  },

  featureBox: {
    display: "flex",
    flexWrap: "wrap",
    alignItems: "center",
    justifyContent: "space-between",
    gap: "20px",
    marginTop: "24px",
    padding: "24px",
    border: "1px solid",
    borderRadius: "14px",
  },

  featureContent: {
    flex: "1 1 280px",
    minWidth: 0,
  },

  featureButton: {
    maxWidth: "100%",
    padding: "12px 18px",
    border: "none",
    borderRadius: "9px",
    color: "#ffffff",
    fontSize: "15px",
    fontWeight: 600,
    cursor: "pointer",
  },

  disabledButton: {
    opacity: 0.55,
    cursor: "not-allowed",
  },

  readyMessage: {
    margin: 0,
    color: "#166534",
    fontSize: "14px",
    lineHeight: 1.5,
  },

  warningMessage: {
    margin: 0,
    color: "#92400e",
    fontSize: "14px",
    lineHeight: 1.5,
  },

  statusBox: {
    marginTop: "24px",
    padding: "24px",
    border: "1px solid #e2e8f0",
    borderRadius: "14px",
    background: "#f8fafc",
  },

  statusRow: {
    display: "flex",
    flexWrap: "wrap",
    alignItems: "center",
    justifyContent: "space-between",
    gap: "12px",
    padding: "12px 0",
    borderBottom: "1px solid #e2e8f0",
  },

  completedStatus: {
    padding: "5px 10px",
    borderRadius: "20px",
    background: "#dcfce7",
    color: "#166534",
    fontSize: "13px",
    fontWeight: 600,
  },

  pendingStatus: {
    padding: "5px 10px",
    borderRadius: "20px",
    background: "#fef3c7",
    color: "#92400e",
    fontSize: "13px",
    fontWeight: 600,
  },

  selectedRole: {
    margin: "16px 0 0",
    color: "#334155",
  },

  statusNote: {
    margin: "14px 0 0",
    color: "#64748b",
    fontSize: "13px",
    lineHeight: 1.6,
  },

  profileErrorBox: {
    marginTop: "20px",
    padding: "16px",
    border: "1px solid #fecaca",
    borderRadius: "10px",
    background: "#fff1f2",
  },

  profileErrorText: {
    margin: "0 0 12px",
    color: "#b91c1c",
  },

  error: {
    padding: "12px",
    borderRadius: "8px",
    background: "#fee2e2",
    color: "#b91c1c",
  },
};