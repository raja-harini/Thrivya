import axiosClient from "./axiosClient";

export async function getCareerData(path, signal) {
  const response = await axiosClient.get(path, {
    signal,
  });

  return response.data;
}

export async function createSkillClaim(data) {
  const response = await axiosClient.post(
    "/student/career-profile/claims",
    data
  );

  return response.data;
}

export async function submitSkillEvidence(claimId, data) {
  const response = await axiosClient.post(
    `/student/career-profile/claims/${claimId}/evidence`,
    data
  );

  return response.data;
}

export async function createCareerProfileShare(data) {
  const response = await axiosClient.post(
    "/student/career-profile/shares",
    data
  );

  return response.data;
}

export async function revokeCareerProfileShare(shareId) {
  const response = await axiosClient.delete(
    `/student/career-profile/shares/${shareId}`
  );

  return response.data;
}

export async function submitEvidenceReview(artifactId, data) {
  const response = await axiosClient.post(
    `/interviewer/evidence-reviews/artifacts/${artifactId}`,
    data
  );

  return response.data;
}

export function careerErrorMessage(error) {
  if (!error.response) {
    return "Cannot reach the backend. Check that Spring Boot is running and CORS is configured.";
  }

  const data = error.response.data;

  if (typeof data?.message === "string") {
    return data.message;
  }

  if (data && typeof data === "object") {
    const messages = Object.values(data).filter(
      (value) => typeof value === "string"
    );

    if (messages.length > 0) {
      return messages.join(" ");
    }
  }

  return `Request failed (${error.response.status}).`;
}