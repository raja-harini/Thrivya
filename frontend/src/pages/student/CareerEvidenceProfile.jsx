import { Link } from "react-router-dom";
import useCareerQuery from "../../hooks/useCareerQuery";
import CareerProfileView from "../../components/career/CareerProfileView";
import {
  CareerPage,
  QueryState,
} from "../../components/career/CareerUI";

export default function CareerEvidenceProfile() {
  const query = useCareerQuery("/student/career-profile");

  return (
    <CareerPage
      title="My career evidence profile"
      description="Understand what you claim, what evidence exists, and what reviewers have actually checked."
    >
      <div className="career-actions">
        <Link
          className="career-button"
          to="/student/career-evidence/claims"
        >
          Manage claims and evidence
        </Link>

        <Link
          className="career-button career-button-secondary"
          to="/student/career-evidence/sharing"
        >
          Manage recruiter sharing
        </Link>

        <button
          className="career-button career-button-secondary"
          onClick={query.reload}
          disabled={query.loading}
        >
          Refresh profile
        </button>
      </div>

      <QueryState loading={query.loading} error={query.error} />

      {query.data && (
        <CareerProfileView
          profile={query.data}
          evidenceBasePath="/student/career-evidence/artifacts"
        />
      )}
    </CareerPage>
  );
}