import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  CareerPage,
  Notice,
} from "../../components/career/CareerUI";

export default function CareerProfileSearch() {
  const navigate = useNavigate();
  const [studentId, setStudentId] = useState("");

  function submit(event) {
    event.preventDefault();

    navigate(
      `/recruiter/career-evidence/candidates/${studentId}`
    );
  }

  return (
    <CareerPage
      title="Open a shared career profile"
      backTo="/recruiter"
      description="Inspect evidence-backed claims only when the student has granted access."
    >
      <Notice>
        This is an authorized-profile lookup, not a public student
        directory. Knowing a student ID does not grant access.
      </Notice>

      <section className="career-card">
        <form className="career-form" onSubmit={submit}>
          <label>
            Student ID shared with you
            <input
              type="number"
              min="1"
              step="1"
              value={studentId}
              onChange={(event) =>
                setStudentId(event.target.value)
              }
              required
            />
          </label>

          <button className="career-button">
            Open shared profile
          </button>
        </form>
      </section>
    </CareerPage>
  );
}