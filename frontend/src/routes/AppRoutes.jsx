import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

// Public pages
import Home from "../pages/Home";
import Unauthorized from "../pages/Unauthorized";

// Authentication
import Login from "../auth/Login";
import Register from "../auth/Register";
import ProtectedRoute from "../auth/ProtectedRoute";
import RoleRedirect from "../auth/RoleRedirect";

// Student dashboard and profile setup
import StudentDashboard from "../pages/student/StudentDashboard";
import StudentProfileSetup from "../pages/student/StudentProfileSetup";
import InputPreference from "../pages/student/InputPreference";
import ResumeUpload from "../pages/student/ResumeUpload";
import ManualSkills from "../pages/student/ManualSkills";
import RoleSelection from "../pages/student/RoleSelection";
import MySkills from "../pages/student/MySkills";

// Student analysis
import ATSReport from "../pages/student/ATSReport";
import SkillGaps from "../pages/student/SkillGaps";
import SkillGapHistory from "../pages/student/SkillGapHistory";

// Student learning progress and resume management
import ProgressUpdate from "../pages/student/ProgressUpdate";
import ResumeVersions from "../pages/student/ResumeVersions";
import ResumeContent from "../pages/student/ResumeContent";

// Project-based learning
import MyProjects from "../pages/student/MyProjects";
import ProjectEvidence from "../pages/student/ProjectEvidence";

// Student career evidence
import CareerEvidenceProfile from "../pages/student/CareerEvidenceProfile";
import SkillEvidenceManager from "../pages/student/SkillEvidenceManager";
import CareerProfileSharing from "../pages/student/CareerProfileSharing";

// Placement cell
import PlacementCellDashboard from "../pages/placement/PlacementCellDashboard";

// Recruiter
import RecruiterDashboard from "../pages/recruiter/RecruiterDashboard";
import CareerProfileSearch from "../pages/recruiter/CareerProfileSearch";
import SharedCareerProfile from "../pages/recruiter/SharedCareerProfile";

// Technical interviewer
import InterviewerDashboard from "../pages/interviewer/InterviewerDashboard";
import EvidenceReviewQueue from "../pages/interviewer/EvidenceReviewQueue";

// Shared evidence-detail page
import CareerEvidenceDetail from "../pages/CareerEvidenceDetail";

function StudentOnly({ children }) {
  return (
    <ProtectedRoute allowedRoles={["STUDENT"]}>
      {children}
    </ProtectedRoute>
  );
}

function RecruiterOnly({ children }) {
  return (
    <ProtectedRoute allowedRoles={["RECRUITER"]}>
      {children}
    </ProtectedRoute>
  );
}

function InterviewerOnly({ children }) {
  return (
    <ProtectedRoute allowedRoles={["TECH_INTERVIEWER"]}>
      {children}
    </ProtectedRoute>
  );
}

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}
        <Route path="/" element={<RoleRedirect />} />
        <Route path="/home" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Student dashboard */}
        <Route
          path="/student"
          element={
            <StudentOnly>
              <StudentDashboard />
            </StudentOnly>
          }
        />

        {/* Student profile setup */}
        <Route
          path="/student/setup"
          element={
            <StudentOnly>
              <StudentProfileSetup />
            </StudentOnly>
          }
        />

        <Route
          path="/student/input-preference"
          element={
            <StudentOnly>
              <InputPreference />
            </StudentOnly>
          }
        />

        <Route
          path="/student/resume-upload"
          element={
            <StudentOnly>
              <ResumeUpload />
            </StudentOnly>
          }
        />

        <Route
          path="/student/manual-skills"
          element={
            <StudentOnly>
              <ManualSkills />
            </StudentOnly>
          }
        />

        <Route
          path="/student/role-selection"
          element={
            <StudentOnly>
              <RoleSelection />
            </StudentOnly>
          }
        />

        <Route
          path="/student/skills"
          element={
            <StudentOnly>
              <MySkills />
            </StudentOnly>
          }
        />

        {/* Student analysis */}
        <Route
          path="/student/ats"
          element={
            <StudentOnly>
              <ATSReport />
            </StudentOnly>
          }
        />

        <Route
          path="/student/skill-gaps"
          element={
            <StudentOnly>
              <SkillGaps />
            </StudentOnly>
          }
        />

        <Route
          path="/student/skill-gap-history"
          element={
            <StudentOnly>
              <SkillGapHistory />
            </StudentOnly>
          }
        />

        {/* Student learning progress and resume management */}
        <Route
          path="/student/progress"
          element={
            <StudentOnly>
              <ProgressUpdate />
            </StudentOnly>
          }
        />

        <Route
          path="/student/resume-versions"
          element={
            <StudentOnly>
              <ResumeVersions />
            </StudentOnly>
          }
        />

        <Route
          path="/student/resume-content"
          element={
            <StudentOnly>
              <ResumeContent />
            </StudentOnly>
          }
        />

        {/* Project-based learning */}
        <Route
          path="/student/my-projects"
          element={
            <StudentOnly>
              <MyProjects />
            </StudentOnly>
          }
        />

        <Route
          path="/student/projects/:projectId/evidence"
          element={
            <StudentOnly>
              <ProjectEvidence />
            </StudentOnly>
          }
        />

        {/* Preserve compatibility with the old project-evidence URL */}
        <Route
          path="/student/project-evidence"
          element={
            <StudentOnly>
              <Navigate
                to="/student/my-projects"
                replace
              />
            </StudentOnly>
          }
        />

        {/* Student career evidence */}
        <Route
          path="/student/career-evidence"
          element={
            <StudentOnly>
              <CareerEvidenceProfile />
            </StudentOnly>
          }
        />

        <Route
          path="/student/career-evidence/claims"
          element={
            <StudentOnly>
              <SkillEvidenceManager />
            </StudentOnly>
          }
        />

        <Route
          path="/student/career-evidence/sharing"
          element={
            <StudentOnly>
              <CareerProfileSharing />
            </StudentOnly>
          }
        />

        <Route
          path="/student/career-evidence/artifacts/:artifactId"
          element={
            <StudentOnly>
              <CareerEvidenceDetail audience="student" />
            </StudentOnly>
          }
        />

        {/* Placement cell dashboard */}
        <Route
          path="/placement"
          element={
            <ProtectedRoute allowedRoles={["PLACEMENT_CELL"]}>
              <PlacementCellDashboard />
            </ProtectedRoute>
          }
        />

        {/* Recruiter dashboard and shared career profiles */}
        <Route
          path="/recruiter"
          element={
            <RecruiterOnly>
              <RecruiterDashboard />
            </RecruiterOnly>
          }
        />

        <Route
          path="/recruiter/career-evidence"
          element={
            <RecruiterOnly>
              <CareerProfileSearch />
            </RecruiterOnly>
          }
        />

        <Route
          path="/recruiter/career-evidence/candidates/:studentId"
          element={
            <RecruiterOnly>
              <SharedCareerProfile />
            </RecruiterOnly>
          }
        />

        <Route
          path="/recruiter/career-evidence/candidates/:studentId/artifacts/:artifactId"
          element={
            <RecruiterOnly>
              <CareerEvidenceDetail audience="recruiter" />
            </RecruiterOnly>
          }
        />

        {/* Technical interviewer dashboard and evidence reviews */}
        <Route
          path="/interviewer"
          element={
            <InterviewerOnly>
              <InterviewerDashboard />
            </InterviewerOnly>
          }
        />

        <Route
          path="/interviewer/evidence-reviews"
          element={
            <InterviewerOnly>
              <EvidenceReviewQueue />
            </InterviewerOnly>
          }
        />

        {/* Unauthorized and fallback pages */}
        <Route
          path="/unauthorized"
          element={<Unauthorized />}
        />

        <Route
          path="*"
          element={<Navigate to="/home" replace />}
        />
      </Routes>
    </BrowserRouter>
  );
}