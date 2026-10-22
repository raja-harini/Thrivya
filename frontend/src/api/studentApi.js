import axiosClient from "./axiosClient";

// Career profile

export async function saveManualSkills(skillsText) {
  const response = await axiosClient.post(
    "/student/profile/manual-skills",
    { skillsText }
  );

  return response.data;
}

export async function uploadResume(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await axiosClient.post(
    "/student/profile/resume",
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    }
  );

  return response.data;
}

export async function getStudentProfile() {
  const response = await axiosClient.get(
    "/student/profile"
  );

  return response.data;
}

export async function getResumeHistory() {
  const response = await axiosClient.get(
    "/student/profile/resume-history"
  );

  return response.data;
}

// Career role selection

export async function getRoleSuggestions() {
  const response = await axiosClient.get(
    "/student/roles/suggestions"
  );

  return response.data;
}

export async function searchDesiredRole(desiredRole) {
  const response = await axiosClient.post(
    "/student/roles/desired",
    { desiredRole }
  );

  return response.data;
}

export async function selectRole(roleId) {
  const response = await axiosClient.post(
    "/student/roles/select",
    { roleId }
  );

  return response.data;
}

// Explainable ATS

export async function generateATSReport() {
  const response = await axiosClient.post(
    "/student/ats/generate"
  );

  return response.data;
}

export async function getLatestATSReport() {
  const response = await axiosClient.get(
    "/student/ats/latest"
  );

  return response.data;
}

export async function getATSHistory() {
  const response = await axiosClient.get(
    "/student/ats/history"
  );

  return response.data;
}

// Skill-gap analysis

export async function calculateSkillGaps() {
  const response = await axiosClient.post(
    "/student/skill-gaps/calculate"
  );

  return response.data;
}

export async function getSkillGaps() {
  const response = await axiosClient.get(
    "/student/skill-gaps"
  );

  return response.data;
}

export async function getSkillGapHistory() {
  const response = await axiosClient.get(
    "/student/skill-gaps/history"
  );

  return response.data;
}

// Learning progress

export async function updateSkillProgress(progressData) {
  const response = await axiosClient.post(
    "/student/progress",
    progressData
  );

  return response.data;
}

// Learning recommendations

export async function getRecommendations(
  skillName,
  type
) {
  const response = await axiosClient.get(
    "/student/recommendations",
    {
      params: {
        skill: skillName,
        type,
      },
    }
  );

  return response.data;
}

// Project-based learning

export async function getProjectSuggestions(skillGapId) {
  const response = await axiosClient.get(
    "/student/projects/suggestions",
    {
      params: {
        skillGapId,
      },
    }
  );

  return response.data;
}

export async function getRoleProjectSuggestions() {
  const response = await axiosClient.get(
    "/student/projects/suggestions/role"
  );

  return response.data;
}

export async function selectProject(suggestionId) {
  const response = await axiosClient.post(
    "/student/projects/select",
    { suggestionId }
  );

  return response.data;
}

export async function getMyProjects() {
  const response = await axiosClient.get(
    "/student/projects/my-projects"
  );

  return response.data;
}

export async function updateProjectEvidence(
  projectId,
  data
) {
  const id = Number(projectId);

  if (!Number.isSafeInteger(id) || id <= 0) {
    throw new Error(
      "A valid project ID is required to save project evidence."
    );
  }

  const response = await axiosClient.put(
    `/student/projects/${id}/evidence`,
    data
  );

  return response.data;
}

// Evidence-aware resume content

export async function generateResumeContent() {
  const response = await axiosClient.post(
    "/student/resume-content/generate"
  );

  return response.data;
}