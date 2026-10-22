import { useParams } from "react-router-dom";
import useCareerQuery from "../../hooks/useCareerQuery";
import CareerProfileView from "../../components/career/CareerProfileView";
import {
  CareerPage,
  Notice,
  QueryState,
} from "../../components/career/CareerUI";

export default function SharedCareerProfile() {
  const { studentId } = useParams();

  const valid = /^[1-9]\d*$/.test(studentId || "");

  const query = useCareerQuery(
    valid
      ? `/recruiter/candidates/${studentId}/career-profile`
      : null
  );

  return (
    <CareerPage
      title="Shared candidate career profile"
      backTo="/recruiter/career-evidence"
      description="Review scoped claims, evidence, and limitations before assessing technical depth."
    >
      {!valid && (
        <Notice type="error">Invalid student ID.</Notice>
      )}

      {valid && (
        <button
          className="career-button career-button-secondary"
          onClick={query.reload}
          disabled={query.loading}
        >
          Refresh permissions and profile
        </button>
      )}

      <QueryState loading={query.loading} error={query.error} />

      {query.data && (
        <CareerProfileView
          profile={query.data}
          evidenceBasePath={
            `/recruiter/career-evidence/candidates/` +
            `${studentId}/artifacts`
          }
        />
      )}
    </CareerPage>
  );
}