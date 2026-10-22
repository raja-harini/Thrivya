import { useParams } from "react-router-dom";

import useCareerQuery from "../hooks/useCareerQuery";

import EvidenceDetails from "../components/career/EvidenceDetails";

import {
  CareerPage,
  Notice,
  QueryState,
} from "../components/career/CareerUI";

export default function CareerEvidenceDetail({
  audience = "student",
}) {
  const { artifactId, studentId } = useParams();

  const validArtifact = /^[1-9]\d*$/.test(
    artifactId || ""
  );

  const validStudent =
    audience !== "recruiter" ||
    /^[1-9]\d*$/.test(studentId || "");

  const valid = validArtifact && validStudent;

  const path = !valid
    ? null
    : audience === "recruiter"
    ? `/recruiter/candidates/${studentId}/evidence/${artifactId}`
    : `/student/career-profile/evidence/${artifactId}`;

  const query = useCareerQuery(path);

  return (
    <CareerPage
      title="Evidence and review details"
      description="Inspect the submitted material and the exact scope of any review."
      backTo={
        audience === "recruiter"
          ? `/recruiter/career-evidence/candidates/${studentId}`
          : "/student/career-evidence"
      }
    >
      {!valid && (
        <Notice type="error">
          Invalid student or artifact identifier.
        </Notice>
      )}

      <QueryState
        loading={query.loading}
        error={query.error}
      />

      {query.data && (
        <EvidenceDetails evidence={query.data} />
      )}
    </CareerPage>
  );
}